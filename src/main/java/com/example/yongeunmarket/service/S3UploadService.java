package com.example.yongeunmarket.service;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
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

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	@Transactional
	public void saveFile(MultipartFile multipartFile, Long requestedUserId, Long currentUserId) {    //예외 바꾸기
		// 권한 체크
		User user = getUserOrThrow(requestedUserId);

		if (!currentUserId.equals(user.getId())) {
			throw new IllegalStateException("해당 user 에 권한이 없습니다");    // 로그인한 user 가 아닌경우 403
		}

		if (multipartFile.isEmpty() || multipartFile.getOriginalFilename() == null) {
			throw new IllegalArgumentException("Image file is empty or invalid"); //404
		}

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
			throw new IllegalStateException("multipart file upload failed !!");
		} catch (Exception ex) {
			deleteObject(key);  //보상 트랜잭션 처리
			log.error("DB 트랜잭션 실패 오류 {}", ex.getMessage());
			throw new IllegalStateException("database error !!");
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
			throw new IllegalArgumentException("Invalid file extension");
		}
		String extension = filename.substring(lastDotIndex + 1);
		if (!ALLOWED_EXTENSIONS.contains(extension)) // 확장자가 jpg, jpeg, png 가 아닌 경우
			throw new IllegalArgumentException("Invalid file extension");
		return extension;
}

private User getUserOrThrow(Long userId) {

	return userRepository.findById(userId).orElseThrow(
		() -> new EntityNotFoundException("user 가 존재하지 않음"));
}
}
