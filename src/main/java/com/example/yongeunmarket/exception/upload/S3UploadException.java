package com.example.yongeunmarket.exception.upload;

/**
 * S3 파일 업로드 실패
 */
public class S3UploadException extends RuntimeException {
	public S3UploadException(String message) {
		super(message);
	}
}
