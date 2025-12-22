package com.example.yongeunmarket.dto.adminChat;

import java.util.List;

import com.example.yongeunmarket.dto.chat.ChatRoomDetailResDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetAdminChatRoomDetailResDto {
	private Long roomId;
	private Long productId;
	private Long sellerId;
	private Long buyerId;
	private String status;
	private List<ChatRoomDetailResDto.ChatMessageDetailDto> messages; // 대화 내역 리스트

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
