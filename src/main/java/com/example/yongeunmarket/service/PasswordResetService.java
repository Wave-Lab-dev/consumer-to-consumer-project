package com.example.yongeunmarket.service;

import static com.example.yongeunmarket.exception.ErrorCode.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
import com.example.yongeunmarket.exception.BusinessException;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "PasswordResetService")
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	@Value("${password-reset.redis-prefix}")
	private String resetCodePrefix;

	@Value("${password-reset.attempt.redis-prefix}")
	private String attemptPrefix;

	@Value("${password-reset.expiry-minutes}")
	private Long expiryMinutes;

	private static final int MAX_ATTEMPTS = 5; // 최대 시도 횟수
	private static final int RESET_CODE_LENGTH = 6;// 재설정 코드 길이
	public static final String INIT_ATTEMPT = "0";

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

		msg.setTo(email);    // 받는 사람 이메일
		msg.setSubject("인증 코드 6자리 발송"); // 메일 제목
		String resetCode = generate6CharCode();
		msg.setText(resetCode); // 메일 content
		String resetCodeKey = resetCodePrefix + userId;
		String attemptKey = attemptPrefix + userId;
		try {
			this.mailSender.send(msg);
			cacheWithTTL(resetCodeKey, resetCode);
			cacheWithTTL(attemptKey, INIT_ATTEMPT);
		} catch (MailException exception) {
			log.error("이메일 전송에 실패 했습니다 {}", exception.getMessage());
			throw new BusinessException(EMAIL_SEND_FAILED);
		}
	}

	@Transactional
	public void resetPassword(VerifyPasswordReqDto verifyPasswordReqDto, Long userId) {

		User user = getUserOrThrow(userId);
		String resetCodeKey = resetCodePrefix + userId;
		String attemptKey = attemptPrefix + userId;

		int attempts = validateResetCodeAttempts(attemptKey);
		verifyResetCodeOrThrow(verifyPasswordReqDto, resetCodeKey, attemptKey, attempts);

		try {
			String newPassword = verifyPasswordReqDto.getNewPassword();
			user.updatePassword(passwordEncoder.encode(newPassword));
		} catch (Exception ex) {
			log.error("DB 트랜잭션 실패 오류 , 재설정 코드를 다시 발급받아 주세요. {}", ex.getMessage());
			throw new BusinessException(INTERNAL_SERVER_ERROR);
		} finally {
			redisTemplate.delete(resetCodeKey);
			redisTemplate.delete(attemptKey);
		}
	}

	/**
	 * 재설정 코드 검증
	 * Timing attack 방지를 위해서 MessageDigest.isEqual() 사용
	 *
	 * @param verifyPasswordReqDto 사용자가 입력한 재설정 코드 DTO
	 * @param resetCodeKey Redis에 저장된 재설정 코드 키
	 * @param attemptKey Redis에 저장된 시도 횟수 키
	 * @param attempts 현재 시도 횟수
	 */
	private void verifyResetCodeOrThrow(VerifyPasswordReqDto verifyPasswordReqDto, String resetCodeKey,
		String attemptKey, int attempts) {
		// redisKey 유효성 검사
		String clientResetCode = verifyPasswordReqDto.getResetCode();
		String serverResetCode = redisTemplate.opsForValue().get(resetCodeKey);

		if (serverResetCode == null) { //만료 혹은 진짜 없음 410
			throw new BusinessException(RESET_CODE_EXPIRED);
		}
		//key 는 있는데 재설정 코드가 서로 다르다 404
		if (!MessageDigest.isEqual(
			clientResetCode.getBytes(StandardCharsets.UTF_8),
			serverResetCode.getBytes(StandardCharsets.UTF_8)
		)) {
			cacheWithTTL(attemptKey, String.valueOf(attempts + 1));
			log.error("재시도 코드가 일치하지 않습니다 (현재 시도횟수: {})",attempts+1);
			throw new BusinessException(RESET_CODE_MISMATCH);
		}
	}

	/**
	 * 현재 재발급 시도 횟수 가지고오기 & 검증
	 * @param attemptKey Redis에 저장된 시도 횟수 키
	 * @return
	 */
	private int validateResetCodeAttempts(String attemptKey) {
		String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
		if (attemptsStr == null) {
			log.error("시도 횟수 정보 만료: attemptKey={}", attemptKey);
			throw new BusinessException(ATTEMPT_EXPIRED);
		}

		int attempts = Integer.parseInt(attemptsStr);

		if (attempts >= MAX_ATTEMPTS) { //시도 횟수 >= 시도 허용횟수
			log.error("재설정 코드 입력 횟수를 초과 했습니다. {} 분후에 다시 시도해주세요",  expiryMinutes);
			throw new BusinessException(TOO_MANY_ATTEMPTS);
		}
		return attempts;
	}

	private User getUserOrThrow(Long userId) {

		return userRepository.findById(userId).orElseThrow(
			() -> new BusinessException(RESOURCE_NOT_FOUND));
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
	 * Redis에 TTL을 가진 key-value를 저장하는 공용 헬퍼 메서드
	 *
	 * @param key Redis key
	 * @param value Redis value
	 */
	private void cacheWithTTL(String key, String value) {

		redisTemplate.opsForValue().set(key, value, expiryMinutes, TimeUnit.MINUTES);
	}

}