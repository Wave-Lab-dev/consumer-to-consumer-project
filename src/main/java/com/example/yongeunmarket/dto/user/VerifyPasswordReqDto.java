package com.example.yongeunmarket.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPasswordReqDto {

	private String resetCode;
	private String newPassword;
}
