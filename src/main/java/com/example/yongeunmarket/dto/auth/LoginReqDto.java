package com.example.yongeunmarket.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginReqDto {
	private String email;
	private String password;

	@Builder
	public LoginReqDto(String email, String password) {
		this.email = email;
		this.password = password;
	}
}
