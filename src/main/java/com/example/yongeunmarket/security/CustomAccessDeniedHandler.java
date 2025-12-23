package com.example.yongeunmarket.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.yongeunmarket.dto.CommonErrorResponse;
import com.example.yongeunmarket.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException {

		log.error("Access denied: {}", accessDeniedException.getMessage());

		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		CommonErrorResponse<Object> errorResponse = CommonErrorResponse.of(ErrorCode.FORBIDDEN);

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
