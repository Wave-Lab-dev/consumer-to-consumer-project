package com.example.yongeunmarket.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.yongeunmarket.dto.CommonErrorResponse;
import com.example.yongeunmarket.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		
		ErrorCode errorCode = (ErrorCode)request.getAttribute("exception");

		// 예외 정보가 없으면 기본 UNAUTHORIZED
		if (errorCode == null) {
			errorCode = ErrorCode.UNAUTHORIZED;
		}

		// 응답 설정
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		// JSON 응답 생성
		CommonErrorResponse<Object> errorResponse = CommonErrorResponse.of(errorCode);

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
