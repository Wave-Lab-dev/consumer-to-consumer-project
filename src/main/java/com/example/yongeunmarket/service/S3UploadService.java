package com.example.yongeunmarket.service;

import static com.example.yongeunmarket.exception.ErrorCode.*;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.exception.BusinessException;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j(topic = "S3UploadService")
@Service
@RequiredArgsConstructor
public class S3UploadService {

	private final S3Client s3Client;

	private final UserRepository userRepository;

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
	private static final String IMAGES_PREFIX = "images/";
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	@Transactional
	public void saveFile(MultipartFile multipartFile, Long requestedUserId, Long currentUserId) {    //예외 바꾸기
		// 권한 체크
		User user = getUserOrThrow(requestedUserId);

		if (!currentUserId.equals(user.getId())) {
			throw new BusinessException(ACCESS_DENIED);    // 로그인한 user 가 아닌경우 403
		}

		verifyInputFileOrThrow(multipartFile);

		if (StringUtils.hasText(user.getImageUrl())) {
			deleteObject(user.getImageUrl());
		}

		String originalFilename = multipartFile.getOriginalFilename();

		String extension = getExtension(originalFilename);
		String key = IMAGES_PREFIX + UUID.randomUUID() + "." + extension;

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType(multipartFile.getContentType())
			.build();
		try {
			s3Client.putObject(
				putObjectRequest,
				RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
			);
			user.updateImageUrl(key);    //서버가 bucket 을 저장하고 있으으로, pull path 가 아닌 key 를 저장한다.
		} catch (IOException ex) {
			log.error("InputStream 실패 오류 {}", ex.getMessage());
			throw new BusinessException(INVALID_FILE_INPUT);
		} catch (Exception ex) {
			deleteObject(key);  //보상 트랜잭션 처리
			log.error("DB 트랜잭션 실패 오류 {}", ex.getMessage());
			throw new BusinessException(INTERNAL_SERVER_ERROR);
		}
	}

	public void deleteObject(String key) {

		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();
		s3Client.deleteObject(deleteObjectRequest);  // 동기 삭제 (blocking)
	}

	/**
	 * 확장자를 가지고 오는 헬퍼 메서드
	 * 확장자가 png, jpg, jpeg 가 아닌경우 예외를 던진다.
	 * @param filename : 가지고 오는 originalFilename
	 * @return : 파일 확장자 (예: jpg, png)
	 * @throws IllegalArgumentException 확장자가 없는 경우
	 */
	private String getExtension(String filename) {

		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1) {
			throw new BusinessException(INVALID_FILE_INPUT);
		}
		String extension = filename.substring(lastDotIndex + 1);
		if (!ALLOWED_EXTENSIONS.contains(extension)) { // 확장자가 jpg, jpeg, png 가 아닌 경우
			log.error("확장자가 일치하지 않습니다 !{}", extension);
			throw new BusinessException(INVALID_FILE_INPUT);
		}
		return extension;
	}

	/**
	 * multipartFile
	 * @param multipartFile : multpart
	 */
	private static void verifyInputFileOrThrow(MultipartFile multipartFile) {
		if (multipartFile.isEmpty() || multipartFile.getOriginalFilename() == null) {
			log.error("이미지 파일이 비어있거나, 타당하지 않음!");
			throw new BusinessException(INVALID_FILE_INPUT); //404
		}
		// 파일 크기 검증
		if (multipartFile.getSize() > MAX_FILE_SIZE) {
			log.error("파일 크기는 10MB를 초과할 수 없습니다. (현재: {}MB)", (multipartFile.getSize() / 1024 / 1024));
			throw new BusinessException(FILE_SIZE_EXCEEDED);
		}
	}

	private User getUserOrThrow(Long userId) {

		return userRepository.findById(userId).orElseThrow(
			() -> new BusinessException(RESOURCE_NOT_FOUND));
	}
}
