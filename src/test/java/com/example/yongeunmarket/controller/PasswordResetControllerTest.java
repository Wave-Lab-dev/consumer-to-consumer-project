package com.example.yongeunmarket.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.yongeunmarket.dto.user.VerifyPasswordReqDto;
import com.example.yongeunmarket.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PasswordResetController.class) //controller 레이어만 컨테이너에 추가
	//@Import(SecurityConfig.class)
class PasswordResetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PasswordResetService passwordResetService;

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
			.newPassword("test1234")
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