package com.example.yongeunmarket.exception;

import static org.springframework.http.HttpStatus.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.yongeunmarket.dto.CommonErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * 1. @Valid 유효성 검사 실패 시 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonErrorResponse<Map<String, String>>> handleValidationExceptions(
		MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();

		// 각 필드별 오류 메시지를 Map에 저장
		ex.getBindingResult().getFieldErrors().forEach(error -> {
			errors.put(error.getField(), error.getDefaultMessage());
		});

		return ResponseEntity
			.status(BAD_REQUEST)
			.body(CommonErrorResponse.of(ErrorCode.VALIDATION_ERROR, errors));
	}

	/**
	 * 404 Not Found
	 * URL 자체가 없을 때
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<CommonErrorResponse<Void>> handleNoResourceFoundException(
		NoResourceFoundException ex) {

		return ResponseEntity
			.status(NOT_FOUND)
			.body(CommonErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonErrorResponse<Object>> handleServerError(Exception ex) {
		return ResponseEntity
			.status(INTERNAL_SERVER_ERROR)
			.body(CommonErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	/**
	 * 비즈니스 예외 처리 (도메인별 ErrorCode 모두 처리)
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<CommonErrorResponse<Void>> handleBusinessException(
		BusinessException ex) {

		return ResponseEntity
			.status(ex.getErrorCode().getStatus())
			.body(CommonErrorResponse.of(ex.getErrorCode()));
	}
}
