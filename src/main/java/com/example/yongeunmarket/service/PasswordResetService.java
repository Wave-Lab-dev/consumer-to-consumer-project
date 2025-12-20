package com.example.yongeunmarket.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.yongeunmarket.dto.user.VerifyPasswordReqDto;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "PasswordResetService")
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	@Value("${password-reset.redis-prefix}")
	private String redisPrefix;

	@Value("${password-reset.expiry-minutes}")
	private Long expiryMinutes;

	private static int RESET_CODE_LENGTH = 6;// 재설정 코드 길이

	public static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";

	private final RedisTemplate<String, String> redisTemplate;
	private final MailSender mailSender;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void sendResetCode(Long userId) {

		User user = getUserOrThrow(userId);
		String email = user.getEmail();

		SimpleMailMessage msg = new SimpleMailMessage();

		msg.setTo(email); 	// 받는 사람 이메일
		msg.setSubject("인증 코드 6자리 발송"); // 메일 제목
		String resetCode = generate6CharCode();
		msg.setText(resetCode); // 메일 content

		try {
			this.mailSender.send(msg);
			cacheResetCode(userId, resetCode);
		} catch (MailException exception) {
			throw new IllegalStateException("이메일 전송 실패");
		}
	}

	@Transactional
	public void resetPassword(VerifyPasswordReqDto verifyPasswordReqDto, Long userId) {

		User user = getUserOrThrow(userId);
		String redisKey = redisPrefix + userId;
		// redisKey 유효성 검사
		String clientResetCode = redisTemplate.opsForValue().get(redisKey); //Object -> String으로 형변환
		String serverResetCode = verifyPasswordReqDto.getResetCode();

		if(clientResetCode == null) { //만료 혹은 진짜 없음 410
			throw new IllegalStateException("재설정 코드가 만료되었거나, 존재하지 않습니다");
		}
		//key 는 있는데 재설정 코드가 서로 다르다 404
		if(!clientResetCode.equals(serverResetCode)) {
			throw new IllegalStateException("재설정 코드가 일치하지 않습니다");
		}
		try{
			String newPassword = verifyPasswordReqDto.getNewPassword();
			user.updatePassword(passwordEncoder.encode(newPassword));
		} catch (Exception ex) {
			log.error("DB 트랜잭션 실패 오류 , 재설정 코드를 다시 발급받아 주세요. {}", ex.getMessage());
			throw new IllegalStateException("database error {} !!", ex);
		} finally {
			redisTemplate.delete(redisKey);
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

		final String charset = CHARSET;
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder(RESET_CODE_LENGTH);//가변적인 문자열
		for (int i = 0; i < RESET_CODE_LENGTH; i++) {
			code.append(charset.charAt(random.nextInt(charset.length())));
		}

		return code.toString();
	}

	/**
	 * 레디스에 TTL 로 resetCode 를 저장하는 메서드
	 * @param userId
	 * @param resetCode
	 */
	private void cacheResetCode(Long userId, String resetCode) {

		String redisKey = redisPrefix + userId;
		redisTemplate.opsForValue().set(redisKey, resetCode, expiryMinutes, TimeUnit.MINUTES);
	}

}