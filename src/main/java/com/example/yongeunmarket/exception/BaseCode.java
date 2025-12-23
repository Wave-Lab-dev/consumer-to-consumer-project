package com.example.yongeunmarket.exception;

import org.springframework.http.HttpStatus;

public interface BaseCode {
	HttpStatus getStatus();

	String getMessage();
}
