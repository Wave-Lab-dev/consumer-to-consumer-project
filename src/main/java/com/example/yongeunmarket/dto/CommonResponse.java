package com.example.yongeunmarket.dto;

import org.springframework.http.HttpStatus;

import com.example.yongeunmarket.exception.BaseCode;

import lombok.Getter;

/**
 * 공통 응답 처리 DTO
 * 성공 이라면 status, message, data 를 반환
 * 실패 라면 status, message 를 반환
 */
@Getter
public class CommonResponse<T> {

	private final HttpStatus status;
	private final String message;
	private final T data;

	public CommonResponse(HttpStatus status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	/**
	 * 성공 응답 처리
	 * @param code : 성공 BaseCode Enum 객체
	 * @param data : 어떤 응답 dto 인지
	 */
	public static <T> CommonResponse<T> of(BaseCode code, T data) {
		return new CommonResponse<>(code.getStatus(), code.getMessage(), data);
	}

	/**
	 * 실패 응답 처리
	 * @param code : 실패 BaseCode Enum 객체
	 */
	public static <T> CommonResponse<T> of(BaseCode code) {
		return of(code, null);
	}
}
