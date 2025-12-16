package com.example.yongeunmarket.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.yongeunmarket.dto.auth.LoginReqDto;
import com.example.yongeunmarket.dto.auth.LoginResDto;
import com.example.yongeunmarket.dto.auth.SignupReqDto;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.jwt.JwtTokenProvider;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public LoginResDto login(LoginReqDto loginReqDto) {

		// 이메일 검증
		User user = userRepository.findByEmail(loginReqDto.getEmail()).orElseThrow(
			() -> new IllegalStateException("not found id")
		);

		//비밀번호 검증
		if (!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
			//throw new IllegalStateException("invalid credentials");
			//회원가입 api 구현 이후 처리를 위해 비활성화
		}

		// JWT 토큰 생성
		String jwt = jwtTokenProvider.createToken(user);

		return new LoginResDto(jwt);
	}

	public void signup(@Valid SignupReqDto signupReqDto) {
		// 중복 검증
		boolean existsByEmail = userRepository.existsByEmail(signupReqDto.getEmail());

		if (existsByEmail) {
			throw new IllegalStateException("email exists");
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
