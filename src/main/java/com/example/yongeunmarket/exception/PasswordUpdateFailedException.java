package com.example.yongeunmarket.exception;
/**
 * 비밀번호 업데이트 실패
 */

public class PasswordUpdateFailedException extends RuntimeException {

	public PasswordUpdateFailedException(String message) {
		super(message);
	}
}
