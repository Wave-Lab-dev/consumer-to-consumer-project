package com.example.yongeunmarket.exception;

/**
 * DB 트랜잭션 처리 예외
 */
public class DataProcessingException extends RuntimeException {
	public DataProcessingException(String message) {
		super(message);
	}
}
