package com.example.yongeunmarket.exception.upload;

public class FileSizeExceededException extends RuntimeException {

	public FileSizeExceededException(String message) {
		super(message);
	}
}
