package com.example.yongeunmarket.exception.user;

/**
 * 재설정 코드 시도 횟수 정보가 만료되었을 때
 */
public class AttemptExpiredException extends RuntimeException {

	public AttemptExpiredException(String message) {
		super(message);
	}
}
