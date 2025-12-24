package com.example.yongeunmarket.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.yongeunmarket.config.SecurityConfig;
import com.example.yongeunmarket.dto.user.VerifyPasswordReqDto;
import com.example.yongeunmarket.jwt.JwtTokenProvider;
import com.example.yongeunmarket.security.CustomAccessDeniedHandler;
import com.example.yongeunmarket.security.CustomAuthenticationEntryPoint;
import com.example.yongeunmarket.security.CustomUserDetails;
import com.example.yongeunmarket.security.CustomUserDetailsService;
import com.example.yongeunmarket.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PasswordResetController.class) //controller 레이어만 컨테이너에 추가
@Import(SecurityConfig.class)
class PasswordResetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JwtTokenProvider  jwtTokenProvider;

	@MockitoBean
	private CustomUserDetails customUserDetails;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@MockitoBean
	private PasswordResetService passwordResetService;

	@MockitoBean  // 추가
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@MockitoBean
	private CustomAccessDeniedHandler customAccessDeniedHandler;

	@BeforeEach
	void setUp() {
		CustomUserDetails userDetails = mock(CustomUserDetails.class);
		given(userDetails.getUserId()).willReturn(1L);

		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				Collections.emptyList()
			)
		);
	}

	@Test
	@DisplayName("성공: 이메일 발송 및 Redis 저장")
	void givenValidUser_whenSendResetCode_thenSendEmailAndSaveToRedis() throws Exception {

		//given
		Long userId = 1L;
		willDoNothing()
			.given(passwordResetService)
			.sendResetCode(eq(userId));
		//when+then
		mockMvc.perform(post("/api/user/password/reset-code"))
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("성공: 비밀번호 재설정")
	void givenValidResetCodeAndUser_whenResetPassword_thenSuccess() throws Exception {

		//given
		VerifyPasswordReqDto reqDto = VerifyPasswordReqDto.builder()
			.resetCode("123456")
			.newPassword("test1234!@")
			.build();

		Long userId = 1L;
		willDoNothing()
			.given(passwordResetService)
			.resetPassword(eq(reqDto), eq(userId));
		//when+then
		mockMvc.perform(post("/api/user/password/reset-password")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(reqDto)))
			.andDo(print())
			.andExpect(status().isOk());

	}
}