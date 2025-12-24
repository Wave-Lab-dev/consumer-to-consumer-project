package com.example.yongeunmarket.dto.chat;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomDetailResDto {
	private Long roomId;
	private Long productId;
	private String productName;
	private Long sellerId;
	private Long buyerId;
	private String status;
	private List<ChatMessageDetailDto> messages;

	@Getter
	@Builder
	public static class ChatMessageDetailDto {
		private Long messageId;
		private Long senderId;
		private String content;
		private String createdAt;
		private boolean isRead;
	}
}