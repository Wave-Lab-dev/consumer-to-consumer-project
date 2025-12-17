package com.example.yongeunmarket;

import org.springframework.stereotype.Component;

import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 인증, 인가 미구현으로 임시 데이터
 */
@Component
@RequiredArgsConstructor
public class TestDataInit {

	private final UserRepository userRepository;

	@PostConstruct
	public void init(){

		User user1 = User.builder()
			.email("juwqq1234@naver.com")
			.password("1234")
			.build();

		User user2 = User.builder()
			.email("test2@naver.com")
			.password("1234")
			.build();

		userRepository.save(user1);
		userRepository.save(user2);
	}
}
