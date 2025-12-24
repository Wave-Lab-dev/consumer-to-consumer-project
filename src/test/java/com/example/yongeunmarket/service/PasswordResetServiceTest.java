package com.example.yongeunmarket.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.yongeunmarket.dto.user.VerifyPasswordReqDto;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.exception.user.AttemptExpiredException;
import com.example.yongeunmarket.exception.user.EmailSendFailedException;
import com.example.yongeunmarket.exception.user.ResetCodeExpiredException;
import com.example.yongeunmarket.exception.user.ResetCodeMismatchException;
import com.example.yongeunmarket.exception.user.TooManyAttemptsException;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private MailSender mailSender;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private PasswordResetService passwordResetService;

	private static final String REDIS_CODE_PREFIX = "password:reset:";
	private static final String ATTEMPT_PREFIX = "password:reset:attempt:";
	private static final Long EXPIRY_MINUTES = 5L;

	private static final Long USER_ID = 1L;
	private static final Long INVALID_USER_ID = 999L;

	private static final Integer RESET_CODE_LENGTH = 6;
	private static final String RESET_CODE = "ABC123";
	private static final String INVALID_RESET_CODE = "123456";

	private static final String NEW_PASSWORD = "newPassword123!";
	private static final String ENCODED_PASSWORD = "encodedPassword123!";

	@BeforeEach
	void setUp() {
		// @Value 주입을 위한 ReflectionTestUtils 사용
		ReflectionTestUtils.setField(passwordResetService, "resetCodePrefix", REDIS_CODE_PREFIX);
		ReflectionTestUtils.setField(passwordResetService, "attemptPrefix", ATTEMPT_PREFIX);
		ReflectionTestUtils.setField(passwordResetService, "expiryMinutes", EXPIRY_MINUTES);
	}

	@Nested
	@DisplayName("sendResetCode 테스트")
	class SendResetCodeTest {

		@Test
		@DisplayName("성공: 이메일 발송 및 Redis 저장")
		void givenValidUser_whenSendResetCode_thenSendEmailAndSaveToRedis() {
			// given
			User user = User.builder()
				.email("test@naver.com")
				.password("1234")
				.build();
			ReflectionTestUtils.setField(user, "id", 1L);

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			doNothing().when(mailSender).send(any(SimpleMailMessage.class));
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			// when
			passwordResetService.sendResetCode(USER_ID);

			// then
			// 1. 메일 발송 검증
			ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
			verify(mailSender, times(1)).send(messageCaptor.capture());

			SimpleMailMessage sentMessage = messageCaptor.getValue();
			assertThat(sentMessage.getTo()).containsExactly("test@naver.com");
			assertThat(sentMessage.getSubject()).isEqualTo("인증 코드 6자리 발송");
			assertThat(sentMessage.getText()).hasSize(6);
			assertThat(sentMessage.getText()).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ123456789]{6}");

			// 2. Redis 저장 검증
			/**
			 * 해당 코드의 인자의 값이 로직에서 의도한 대로 세팅이 되었는지 확인을 필수로 해야하는 경우 ArgumentCaptor 를 사용한다.
			 * ArgumentCaptor 는 verify 와 함께 사용되어야 한다.
			 * capture() 실제 파라메터에 들어간 값을 바인딩하는데 사용된다.
			 */
			ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
			ArgumentCaptor<TimeUnit> timeUnitCaptor = ArgumentCaptor.forClass(TimeUnit.class);

			verify(valueOperations, times(2)).set(
				keyCaptor.capture(),
				valueCaptor.capture(),
				ttlCaptor.capture(),
				timeUnitCaptor.capture()
			);

			// resetCode 검증
			assertThat(keyCaptor.getAllValues().get(0)).isEqualTo(REDIS_CODE_PREFIX + USER_ID);
			assertThat(valueCaptor.getAllValues().get(0)).hasSize(RESET_CODE_LENGTH);

			// attempt 검증
			assertThat(keyCaptor.getAllValues().get(1)).isEqualTo(ATTEMPT_PREFIX + USER_ID);
			assertThat(valueCaptor.getAllValues().get(1)).isEqualTo(PasswordResetService.INIT_ATTEMPT);

			// ttl 검증
			assertThat(ttlCaptor.getValue()).isEqualTo(EXPIRY_MINUTES);
			assertThat(timeUnitCaptor.getValue()).isEqualTo(TimeUnit.MINUTES);
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자")
		void givenNonExistingUser_whenSendResetCode_thenThrowEntityNotFoundException() {
			// given
			when(userRepository.findById(INVALID_USER_ID)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> passwordResetService.sendResetCode(INVALID_USER_ID))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("user 가 존재하지 않음");

			verify(mailSender, never()).send(any(SimpleMailMessage.class));
			verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
		}

		@Test
		@DisplayName("실패: 이메일 발송 실패")
		void givenMailSendFailure_whenSendResetCode_thenThrowIllegalStateException() {
			// given
			User user = User.builder()
				.email("test@naver.com")
				.password("1234")
				.build();
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

			doThrow(new MailException("메일 서버 오류") {
			}).when(mailSender).send(any(SimpleMailMessage.class));

			// when & then
			assertThatThrownBy(() -> passwordResetService.sendResetCode(USER_ID))
				.isInstanceOf(EmailSendFailedException.class)
				.hasMessage("이메일 전송에 실패 했습니다 !");

			verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
		}
	}

	@Nested
	@DisplayName("resetPassword 테스트")
	class ResetPasswordTest {

		private String resetCodeKey;
		private String attemptKey;

		@BeforeEach
		void setUpResetPasswordTest() {
			resetCodeKey = REDIS_CODE_PREFIX + USER_ID;
			attemptKey = ATTEMPT_PREFIX + USER_ID;
		}

		@Test
		@DisplayName("성공: 비밀번호 재설정")
		void givenValidResetCodeAndUser_whenResetPassword_thenSuccess() {
			// given
			User user = User.builder()
				.email("test@naver.com")
				.password("1234")
				.build();
			ReflectionTestUtils.setField(user, "id", 1L);

			VerifyPasswordReqDto dto = VerifyPasswordReqDto.builder()
				.resetCode(RESET_CODE)
				.newPassword(NEW_PASSWORD)
				.build();

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(attemptKey)).thenReturn("1");
			when(valueOperations.get(resetCodeKey)).thenReturn(RESET_CODE);
			when(redisTemplate.delete(attemptKey)).thenReturn(true);
			when(redisTemplate.delete(resetCodeKey)).thenReturn(true);
			when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
			// when
			passwordResetService.resetPassword(dto, USER_ID);

			// then
			verify(valueOperations).get(attemptKey);
			verify(valueOperations).get(resetCodeKey);
			verify(passwordEncoder).encode(NEW_PASSWORD);
			verify(redisTemplate).delete(resetCodeKey);
			verify(redisTemplate).delete(attemptKey);

			// 비밀번호가 실제로 변경되었는지 검증
			assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자")
		void givenNonExistingUser_whenResetPassword_thenThrowEntityNotFoundException() {
			// given
			Long userId = 999L;
			VerifyPasswordReqDto dto = VerifyPasswordReqDto.builder()
				.resetCode(RESET_CODE)
				.newPassword(NEW_PASSWORD)
				.build();

			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> passwordResetService.resetPassword(dto, userId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("user 가 존재하지 않음");

			verify(valueOperations, never()).get(anyString());
			verify(redisTemplate, never()).delete(anyString());
		}

		@Test
		@DisplayName("실패: 최대 시도 횟수 초과 (5번 이상)")
		void givenTooManyAttempts_whenResetPassword_thenThrowTooManyAttemptsException() {
			// given
			User user = User.builder()
				.email("test@naver.com")
				.password("1234")
				.build();
			VerifyPasswordReqDto dto = VerifyPasswordReqDto.builder()
				.resetCode(RESET_CODE)
				.newPassword(NEW_PASSWORD)
				.build();

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(attemptKey)).thenReturn("5");

			// when & then
			assertThatThrownBy(() -> passwordResetService.resetPassword(dto, USER_ID))
				.isInstanceOf(TooManyAttemptsException.class)
				.hasMessageContaining("재설정 코드 입력 횟수를 초과했습니다")
				.hasMessageContaining(EXPIRY_MINUTES + "분 후에 다시 시도해주세요");

			verify(valueOperations, never()).get(resetCodeKey);
			verify(redisTemplate, never()).delete(anyString());
		}

		@Test
		@DisplayName("실패: attempt 정보 만료")
		void givenExpiredAttempt_whenResetPassword_thenThrowAttemptExpiredException() {
			// given
			User user = User.builder()
				.email("test@naver.com")
				.password("1234")
				.build();
			VerifyPasswordReqDto dto = VerifyPasswordReqDto.builder()
				.resetCode(RESET_CODE)
				.newPassword(NEW_PASSWORD)
				.build();

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(attemptKey)).thenReturn(null);

			// when & then
			assertThatThrownBy(() -> passwordResetService.resetPassword(dto, USER_ID))
				.isInstanceOf(AttemptExpiredException.class)
				.hasMessage("재설정 코드 유효 시간이 만료되었습니다. 코드를 다시 발급받아 주세요.");

			verify(valueOperations, never()).get(resetCodeKey);
			verify(redisTemplate, never()).delete(anyString());
		}

		@Test
		@DisplayName("실패: Redis에 코드가 없음 (만료 또는 미존재)")	// 410 case
		void givenExpiredOrMissingCode_whenResetPassword_thenThrowIllegalStateException() {
			// given
			User user = User.builder()
				.email("test@naver.com")
				.password("1234")
				.build();
			VerifyPasswordReqDto reqDto = VerifyPasswordReqDto.builder()
				.resetCode(RESET_CODE)
				.newPassword(NEW_PASSWORD)
				.build();
			String redisKey = REDIS_CODE_PREFIX + USER_ID;

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(attemptKey)).thenReturn(PasswordResetService.INIT_ATTEMPT);
			when(valueOperations.get(redisKey)).thenReturn(null);

			// when & then
			assertThatThrownBy(() -> passwordResetService.resetPassword(reqDto, USER_ID))
				.isInstanceOf(ResetCodeExpiredException.class)
				.hasMessage("재설정 코드가 만료되었거나, 존재하지 않습니다");

			verify(redisTemplate, never()).delete(anyString());
		}

		@Test
		@DisplayName("실패: 코드 불일치")	// 404 case
		void givenMismatchedResetCode_whenResetPassword_thenThrowIllegalStateException() {
			// given
			String redisKey = REDIS_CODE_PREFIX + USER_ID;

			User user = User.builder()
				.email("test@naver.com")
				.password("1234")
				.build();
			VerifyPasswordReqDto reqDto = VerifyPasswordReqDto.builder()
				.resetCode(INVALID_RESET_CODE)
				.newPassword(NEW_PASSWORD)
				.build();

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(attemptKey)).thenReturn(PasswordResetService.INIT_ATTEMPT);
			when(valueOperations.get(redisKey)).thenReturn(RESET_CODE);

			// when & then
			assertThatThrownBy(() -> passwordResetService.resetPassword(reqDto, USER_ID))
				.isInstanceOf(
					ResetCodeMismatchException.class)
				.hasMessage("재설정 코드가 일치하지 않습니다");

			verify(redisTemplate, never()).delete(anyString());
		}
	}
}