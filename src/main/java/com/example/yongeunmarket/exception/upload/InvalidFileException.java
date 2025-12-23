package com.example.yongeunmarket.exception.upload;

/**
 * 잘못된 파일 입력
 */
public class InvalidFileException extends RuntimeException {
	public InvalidFileException(String message) {
		super(message);
	}
}