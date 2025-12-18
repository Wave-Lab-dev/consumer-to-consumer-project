package com.example.yongeunmarket.dto.chat;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateChatRoomResDto {
	private Long roomId;
	private Long buyerId;
	private Long sellerId;
	private Long productId;
	private LocalDateTime createdAt;

	private FirstMessageDto firstMessage;

	@Getter
	@Builder
	public static class FirstMessageDto {
		private Long messageId;
		private Long senderId;
		private String content;
		private LocalDateTime createdAt;
	}
}