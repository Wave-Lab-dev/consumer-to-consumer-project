package com.example.yongeunmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.yongeunmarket.security.CustomUserDetails;
import com.example.yongeunmarket.service.S3UploadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class S3UploadController {

	private final S3UploadService s3UploadService;

	@PostMapping("/{userId}/upload")
	public ResponseEntity<Void> uploadFile(@RequestPart("multipartFile") MultipartFile multipartFile,
		@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		Long currentUserId = customUserDetails.getUserId();
		s3UploadService.saveFile(multipartFile, userId, currentUserId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
