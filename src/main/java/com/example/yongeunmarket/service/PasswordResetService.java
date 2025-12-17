package com.example.yongeunmarket.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

	@Value("${password-reset.redis-prefix}")
	private String redisPrefix;

	@Value("${password-reset.expiry-minutes}")
	private int expiryMinutes;

	private final RedisTemplate<String, Object> redisTemplate;
	private final MailSender mailSender;
	private final UserRepository userRepository;

	public void sendResetCode(Long userId) {

		User user = getUserOrThrow(userId);
		String email = user.getEmail();

		SimpleMailMessage msg = new SimpleMailMessage();
		// 받는 사람 이메일
		msg.setTo(email);
		// 이메일 제목
		msg.setSubject("인증 코드 6자리 발송");
		// 이메일 내용
		String resetCode = generate6CharCode();
		msg.setText(resetCode);

		String redisKey = redisPrefix + userId;
		// Redis에 저장
		redisTemplate.opsForValue().set(redisKey, resetCode, expiryMinutes, TimeUnit.MINUTES);

		try {
			// 메일 보내기
			this.mailSender.send(msg);
			System.out.println("이메일 전송 성공!");
		} catch (MailException exception) {
			throw new IllegalStateException("이메일 전송 실패");
		}
	}

	private User getUserOrThrow(Long userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new EntityNotFoundException("user 가 존재하지 않음"));
	}

	/**
	 * 인증 코드 생성기
	 * 무작위 6가지 코드 (숫자+알파벳)
	 */
	private static String generate6CharCode() {
		final String charset = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
		final int length = 6;
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder(length);//가변적인 문자열
		for (int i = 0; i < length; i++) {
			code.append(charset.charAt(random.nextInt(charset.length())));
		}

		return code.toString();
	}
}