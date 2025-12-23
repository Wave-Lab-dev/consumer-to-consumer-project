package com.example.yongeunmarket.exception;

import static org.springframework.http.HttpStatus.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.yongeunmarket.dto.CommonResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * 1. @Valid 유효성 검사 실패 시 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse<Map<String, String>>> handleValidationExceptions(
		MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();

		// 각 필드별 오류 메시지를 Map에 저장
		ex.getBindingResult().getFieldErrors().forEach(error -> {
			errors.put(error.getField(), error.getDefaultMessage());
		});

		return ResponseEntity.status(BAD_REQUEST).body(CommonResponse.of(ErrorCode.VALIDATION_ERROR, errors));
	}

	/**
	 * 404 Not Found
	 * URL 자체가 없을 때
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<CommonResponse<Void>> handleNoResourceFoundException(
		NoResourceFoundException ex) {

		return ResponseEntity
			.status(NOT_FOUND)
			.body(CommonResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonResponse<Object>> handleServerError(Exception ex) {
		return ResponseEntity
			.status(INTERNAL_SERVER_ERROR)
			.body(CommonResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

}
