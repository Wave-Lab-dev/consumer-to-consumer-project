package com.example.yongeunmarket.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.yongeunmarket.exception.CustomJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * HTTP 요청 하나당 딱 한 번 실행하는 필터로 모든 요청이 Controller로 가기 전에 반드시 통과한다.
	 * 인증된 사용자 요청인지 판별하여 SecurityContextHolder에 저장한다
	 * 이를 통해 권한 검증이 필요한 곳에서 사용자 정보를 확인할 수 있게 한다
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		try {
			// 1. 토큰 추출
			String jwt = resolveToken(request);

			// 2. 토큰이 있는 경우 검증 및 인증 설정
			if (StringUtils.hasText(jwt)) {
				jwtTokenProvider.validateToken(jwt);
				// 검증 성공 시 인증 정보 설정
				Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

			// 3. 다음 필터로 진행
			filterChain.doFilter(request, response);

		} catch (CustomJwtException e) {
			// JWT 예외 발생 시 request에 예외 정보 저장
			request.setAttribute("exception", e.getErrorCode());

			// 필터 체인 계속 진행 (AuthenticationEntryPoint에서 처리)
			filterChain.doFilter(request, response);
		}
	}

	/**
	 클라이언트가 보낸 HTTP 요청에서 Authorization 헤더에 담긴 토큰을 추출
	 @return Bearer 접두어를 제거한 JWT 토큰만 추출하여 반환
	 */
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}


