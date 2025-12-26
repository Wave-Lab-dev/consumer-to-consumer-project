package com.example.yongeunmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.user.VerifyPasswordReqDto;
import com.example.yongeunmarket.security.CustomUserDetails;
import com.example.yongeunmarket.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/password")
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	@PostMapping("/reset-code")
	public ResponseEntity<Void> sendResetCode(
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		Long userId = customUserDetails.getUserId();
		passwordResetService.sendResetCode(userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Void> resetPassword(
		@RequestBody @Valid VerifyPasswordReqDto verifyPasswordReqDto,
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		Long userId = customUserDetails.getUserId();
		passwordResetService.resetPassword(verifyPasswordReqDto, userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
