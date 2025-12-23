package com.example.yongeunmarket.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private final BaseCode errorCode;

	public BusinessException(BaseCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BusinessException(BaseCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
