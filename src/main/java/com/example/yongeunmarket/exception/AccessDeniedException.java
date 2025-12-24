package com.example.yongeunmarket.exception;

/**
 * 권한 없음
 */
public class AccessDeniedException extends RuntimeException {
	public AccessDeniedException(String message) {
		super(message);
	}
}