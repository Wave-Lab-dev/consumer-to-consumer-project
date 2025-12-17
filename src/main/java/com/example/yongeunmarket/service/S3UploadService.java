package com.example.yongeunmarket.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3UploadService {

	/**
	 * AmazonS3 는 AmazonS3Client의 인터페이스 이므로
	 * 등록된 빈(AmazonS3Client의)이 자동 매핑된다.
	 */
	private final AmazonS3 amazonS3;

	private final UserRepository userRepository;

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
			deleteImage(user.getImageUrl());
		}

		String originalFilename = multipartFile.getOriginalFilename();

		String extension = getExtension(originalFilename);
		String key = "images/" + UUID.randomUUID() + "." + extension;

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getSize());
		metadata.setContentType(multipartFile.getContentType());

		try {
			InputStream inputStream = multipartFile.getInputStream();
			amazonS3.putObject(bucket, key, inputStream, metadata);
			user.updateImageUrl(key);    //서버가 bucket 을 저장하고 있으으로, pull path 가 아닌 key 를 저장한다.
		} catch (IOException e) {
			throw new IllegalStateException("multipart file upload failed !!");
		}
	}

	/**
	 * s3 에서 image file 삭제 하기
	 * @param imageUrl
	 */
	public void deleteImage(String imageUrl) {

		amazonS3.deleteObject(bucket, imageUrl);
	}

	/**
	 * 확장자를 가지고 오는 헬퍼 메서드
	 * @param filename : 가지고 오는 originalFilename
	 * @return : 파일 확장자 (예: jpg, png)
	 * @throws IllegalArgumentException 확장자가 없는 경우
	 */
	private String getExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1) {
			throw new IllegalArgumentException("Invalid file extension");
		}
		return filename.substring(lastDotIndex + 1);
	}

	private User getUserOrThrow(Long userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new EntityNotFoundException("user 가 존재하지 않음"));
	}
}
