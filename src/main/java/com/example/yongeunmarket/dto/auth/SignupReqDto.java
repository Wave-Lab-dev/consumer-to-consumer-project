package com.example.yongeunmarket.dto.auth;

import com.example.yongeunmarket.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupReqDto {
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 아닙니다.")
	private String email;
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 16, message = "비밀번호는 8~16자여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,16}$",
		message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
	)
	private String password;
	private UserRole role;
}
