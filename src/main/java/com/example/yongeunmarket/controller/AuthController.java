package com.example.yongeunmarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.auth.LoginReqDto;
import com.example.yongeunmarket.dto.auth.LoginResDto;
import com.example.yongeunmarket.dto.auth.SignupReqDto;
import com.example.yongeunmarket.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<Void> signup(@Valid @RequestBody SignupReqDto signupReqDto) {

		authService.signup(signupReqDto);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResDto> login(@Valid @RequestBody LoginReqDto loginReqDto) {

		LoginResDto loginResDto = authService.login(loginReqDto);
		return ResponseEntity.ok(loginResDto);
	}
}
