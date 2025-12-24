package com.example.yongeunmarket.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;

import com.example.yongeunmarket.entity.UserRole;
import com.example.yongeunmarket.repository.UserRepository;
import com.example.yongeunmarket.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private UserRepository userRepository;

	private String testSecret;
	private long tokenValidityInSeconds;
	private CustomUserDetails testUser;

	@BeforeEach
	void setUp() {
		// 테스트용 설정값
		testSecret = "testSecretKeyForJwtTokenGenerationMustBeLongEnoughForHS512Algorithm";
		tokenValidityInSeconds = 3600; // 1시간

		// JwtTokenProvider 인스턴스 생성
		jwtTokenProvider = new JwtTokenProvider(testSecret, tokenValidityInSeconds);

		// 테스트용 사용자 생성
		testUser = mock(CustomUserDetails.class);

		when(testUser.getUsername()).thenReturn("test@example.com");
		when(testUser.getRole()).thenReturn(UserRole.BUYER);
		when(testUser.getUserId()).thenReturn(1L);
	}

	@Test
	@DisplayName("유효한 사용자 정보로 토큰을 생성하면 JWT형식의 문자열이 반환된다")
	void createToken_whenValidUser_thenReturnsJwtToken() {
		// when
		String token = jwtTokenProvider.createToken(testUser);

		// then
		assertThat(token).isNotNull();
		assertThat(token).isNotEmpty();
		assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
	}

	@Test
	@DisplayName("사용자 정보로 JWT 토큰을 생성하면 id,권한 클레임이 포함된다")
	void createToken_whenUserProvided_thenContainsUserClaims() {
		// when
		String token = jwtTokenProvider.createToken(testUser);

		// then
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.build()
			.parseClaimsJws(token)
			.getBody();

		assertThat(claims.getSubject()).isEqualTo("test@example.com");
		assertThat(claims.get("auth")).isEqualTo("ROLE_" + UserRole.BUYER.name());
		assertThat(claims.get("userId")).isEqualTo(1);
	}

	@Test
	@DisplayName("생성된 토큰의 만료 시간이 설정된 시간과 정확히 일치한다")
	void createToken_when_thenHasCorrectExpiration() {
		// given
		long beforeCreation = System.currentTimeMillis();

		// when
		String token = jwtTokenProvider.createToken(testUser);

		// then
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes()))
			.build()
			.parseClaimsJws(token)
			.getBody();

		Date expiration = claims.getExpiration();
		long expectedExpiration = beforeCreation + (tokenValidityInSeconds * 1000);

		assertThat(expiration.getTime()).isGreaterThan(beforeCreation); //jwt에서 꺼낸 만료 시간이 현재 시간보다 미래인가?
		assertThat(expiration.getTime()).isLessThanOrEqualTo(
			expectedExpiration + 1000); //jwt에서 꺼낸 만료 시간이 1초 뒤 보다 같거나 작은가?
	}

	@Test
	@DisplayName("유효한 토큰으로 인증 정보를 조회하면 Authentication이 생성된다")
	void getAuthentication_whenTokenIsValid_thenReturnAuthentication() {
		// given
		String token = jwtTokenProvider.createToken(testUser);

		// when
		Authentication authentication = jwtTokenProvider.getAuthentication(token);

		// then
		assertThat(authentication).isNotNull();
		// 인증 객체의 principal은 CustomUserDetails로 만들어져야 한다.
		assertThat(authentication.getPrincipal()).isInstanceOf(CustomUserDetails.class);

		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		//CustomUserDetails에 정보가 올바르게 저장되었는지 확인한다
		assertThat(userDetails.getUserId()).isEqualTo(1L);
		assertThat(userDetails.getRole()).isEqualTo(UserRole.BUYER);
	}

	@Test
	@DisplayName("올바른 사용자 정보로 토큰을 만들었다면 토큰 검증에 성공한다")
	void validateToken_ValidToken_Succeeds() {
		// given
		String token = jwtTokenProvider.createToken(testUser);

		// when
		boolean isValid = jwtTokenProvider.validateToken(token);

		// then
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("유효기간이 만료된 토큰을 검증한다면 실패한다")
	void validateToken_whenExpiredToken_thenFails() {
		// given
		JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(testSecret, -1);
		String expiredToken = expiredTokenProvider.createToken(testUser);

		// 토큰이 만료되도록 대기
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// when
		boolean isValid = jwtTokenProvider.validateToken(expiredToken);

		// then
		assertThat(isValid).isFalse();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@DisplayName("null 또는 빈 토큰이면 검증에 실패한다")
	@MockitoSettings(strictness = Strictness.LENIENT)
	void validateToken_whenInvalidToken_thenReturnFail(String token) {
		// when
		boolean isValid = jwtTokenProvider.validateToken(token);

		// then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("다른 사용자의 토큰을 생성한다면 해당 토큰에는 각각 다른 정보가 담겨있다")
	void createToken_WithDifferentUser_thenReturnDifferentToken() {
		// given
		CustomUserDetails anotherUser = mock(CustomUserDetails.class);
		when(anotherUser.getUsername()).thenReturn("another@example.com");
		when(anotherUser.getRole()).thenReturn(UserRole.SELLER);
		when(anotherUser.getUserId()).thenReturn(999L);

		// when
		String testUserToken = jwtTokenProvider.createToken(testUser);
		Authentication testAuthentication = jwtTokenProvider.getAuthentication(testUserToken);

		String anotherToken = jwtTokenProvider.createToken(anotherUser);
		Authentication anotherAuthentication = jwtTokenProvider.getAuthentication(anotherToken);

		// then
		CustomUserDetails extractedUser = (CustomUserDetails)anotherAuthentication.getPrincipal();
		CustomUserDetails testUser = (CustomUserDetails)testAuthentication.getPrincipal();

		assertThat(extractedUser.getUserId()).isNotEqualTo(testUser.getUserId());
		assertThat(extractedUser.getUserId()).isEqualTo(999L);
		assertThat(extractedUser.getRole()).isEqualTo(UserRole.SELLER);
	}
}
