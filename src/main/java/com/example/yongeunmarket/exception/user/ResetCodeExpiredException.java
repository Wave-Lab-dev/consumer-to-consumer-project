package com.example.yongeunmarket.exception.user;

/**
 * 재설정 코드가 만료되었을 때
 */
public class ResetCodeExpiredException extends RuntimeException {

	public ResetCodeExpiredException(String message) {
		super(message);
	}
}
