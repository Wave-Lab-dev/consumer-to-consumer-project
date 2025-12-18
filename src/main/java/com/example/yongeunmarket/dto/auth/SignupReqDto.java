package com.example.yongeunmarket.dto.auth;

import com.example.yongeunmarket.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupReqDto {
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 아닙니다.")
	private String email;
	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;
	private UserRole role;
}
