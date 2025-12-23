package com.example.yongeunmarket.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.yongeunmarket.dto.auth.LoginReqDto;
import com.example.yongeunmarket.dto.auth.LoginResDto;
import com.example.yongeunmarket.dto.auth.SignupReqDto;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.exception.AuthErrorCode;
import com.example.yongeunmarket.exception.BusinessException;
import com.example.yongeunmarket.jwt.JwtTokenProvider;
import com.example.yongeunmarket.repository.UserRepository;
import com.example.yongeunmarket.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;

	public LoginResDto login(LoginReqDto loginReqDto) {

		// 인증 시도
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				loginReqDto.getEmail(),
				loginReqDto.getPassword()
			)
		);

		// 유저 정보 가져오기
		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

		// JWT 토큰 생성
		String jwt = jwtTokenProvider.createToken(userDetails);

		return new LoginResDto(jwt);
	}

	public void signup(@Valid SignupReqDto signupReqDto) {
		// 중복 검증
		boolean existsByEmail = userRepository.existsByEmail(signupReqDto.getEmail());

		if (existsByEmail) {
			throw new BusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}

		//비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(signupReqDto.getPassword());

		User user = User.builder()
			.email(signupReqDto.getEmail())
			.password(encodedPassword)
			.role(signupReqDto.getRole())
			.build();

		userRepository.save(user);
	}
}
