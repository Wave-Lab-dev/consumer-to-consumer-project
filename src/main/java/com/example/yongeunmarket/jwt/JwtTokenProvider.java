package com.example.yongeunmarket.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.yongeunmarket.entity.UserRole;
import com.example.yongeunmarket.repository.UserRepository;
import com.example.yongeunmarket.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

	private final Key key;
	private final long tokenValidityInMilliseconds;

	public JwtTokenProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds, UserRepository userRepository) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
	}

	/**
	 * 사용자 정보를 이용해 JWT 토큰을 생성합니다.
	 */
	public String createToken(CustomUserDetails user) {
		String authority = "ROLE_" + user.getRole().name();

		long now = (new Date()).getTime();
		Date validity = new Date(now + this.tokenValidityInMilliseconds);

		return Jwts.builder()
			.setSubject(user.getUsername())
			.claim("auth", authority)
			.claim("userId", user.getUserId())
			.claim("email", user.getUsername())
			.signWith(key, SignatureAlgorithm.HS512)
			.setExpiration(validity)
			.compact();
	}

	/**
	 * JWT 토큰에서 인증 정보를 추출합니다.
	 * Claims: JWT 안에 담겨 있는 데이터(payload)를 의미하며 토큰이 증명하는 사용자 정보 조각이다.
	 * authorities: Spring Security가 사용하는 권한 객체 모음
	 * principal: SecurityContextHolder에 저장될 사용자 정보
	 * @return UsernamePasswordAuthenticationToken 사용자의 정보를 담은 Authentication객체
	 */
	public Authentication getAuthentication(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();

		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(claims.get("auth").toString().split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		Long userId = Long.parseLong(claims.get("userId").toString());
		UserRole userRole = UserRole.valueOf(claims.get("auth").toString().replace("ROLE_", ""));
		CustomUserDetails userDetails = new CustomUserDetails(userId, userRole);

		return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
	}

	/**
	 * JWT 토큰의 유효성을 검증합니다.
	 * 설정된 서명 키(Signing Key)를 사용하여 토큰을 파싱하고
	 * 서명 위조 여부, 만료 여부, 형식 오류 등을 검증합니다.
	 * 파싱 과정에서 예외가 발생하면 해당 토큰은 유효하지 않은 것으로 간주하여 에러를 던집니다.
	 * @throws JwtException JWT 서명 에러, 만료, 형식 오류, 클레임 문제 등 JWT 관련 모든 에러의 상위 타입.
	 * @throws IllegalArgumentException token이 null, 빈 문자열 등 JJWT 라이브러리 내부에서 발생시킨다
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("잘못된 JWT 토큰: {}", e.getMessage());
			return false;
		}
	}
}
