package com.example.yongeunmarket.exception.user;

/**
 * 재설정 코드 입력 시도 횟수 초과
 */
public class TooManyAttemptsException extends RuntimeException {

	public TooManyAttemptsException(String message) {
		super(message);
	}
}
