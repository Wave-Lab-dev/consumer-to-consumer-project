package com.example.yongeunmarket.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomListResDto {
	private Long roomId;
	private Long productId;
	private String productName;
	private Long buyerId;
	private Long sellerId;
	private String lastMessage;
	private String lastMessageTime;
	private String status;
	private int unreadCount;
}