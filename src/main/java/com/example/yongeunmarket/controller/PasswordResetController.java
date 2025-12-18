package com.example.yongeunmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.user.VerifyPasswordReqDto;
import com.example.yongeunmarket.service.PasswordResetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/password")
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	@PostMapping("/reset-code")
	public ResponseEntity<Void> sendResetCode() {
		//@AuthenticationPrincipal CustomUserDetails
		Long userId = 1L; // userDetails.getUsername() 인증 인가 미구현으로 인한 하드코딩
		passwordResetService.sendResetCode(userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Void> resetPassword(
		@RequestBody VerifyPasswordReqDto verifyPasswordReqDto) {
		//@AuthenticationPrincipal CustomUserDetails
		Long userId = 1L; // userDetails.getUsername() 인증 인가 미구현으로 인한 하드코딩
		passwordResetService.resetPassword(verifyPasswordReqDto, userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
