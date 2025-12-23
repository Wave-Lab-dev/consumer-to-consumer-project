package com.example.yongeunmarket.exception;
/**
 * 이메일 전송 실패
 */
public class EmailSendFailedException extends RuntimeException {

	public EmailSendFailedException(String message) {
		super(message);
	}
}
