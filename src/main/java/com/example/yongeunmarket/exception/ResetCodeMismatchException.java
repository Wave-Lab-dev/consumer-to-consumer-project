package com.example.yongeunmarket.exception;
/**
 * 재설정 코드가 일치하지 않을 때
 */
public class ResetCodeMismatchException extends RuntimeException {

	public ResetCodeMismatchException(String message) {
		super(message);
	}
}
