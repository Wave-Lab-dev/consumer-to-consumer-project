package com.example.yongeunmarket.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements BaseCode {

	//@Validation 유효성 검사 실패시 처리
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),

	//common
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "입력값을 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 user 에 권한이 없습니다"),

	//jwt
	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
	TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "토큰이 없습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 실패"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

	//upload
	FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 업로드가 실패 했습니다."),
	INVALID_FILE_INPUT(HttpStatus.BAD_REQUEST, "파일 형식이 일치하지 않습니다."),

	//password-reset
	EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE,"이메일 전송에 실패 했습니다 !"),
	RESET_CODE_MISMATCH(HttpStatus.NOT_FOUND, "재설정 코드가 일치하지 않습니다"),
	RESET_CODE_EXPIRED(HttpStatus.GONE, "재설정 코드가 만료되었거나, 존재하지 않습니다"),
	ATTEMPT_EXPIRED(HttpStatus.GONE, "재설정 코드 유효 시간이 만료되었습니다. 코드를 다시 발급받아 주세요."),
	TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "재설정 코드 입력 횟수를 초과했습니다.");



	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public HttpStatus getStatus() {
		return this.status;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
