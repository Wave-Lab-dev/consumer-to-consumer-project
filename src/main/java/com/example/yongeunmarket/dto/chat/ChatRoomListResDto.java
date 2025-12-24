package com.example.yongeunmarket.dto.chat;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomListResDto {
	private Long roomId;
	private Long productId;
	private String productName;
	private Long buyerId;
	private Long sellerId;
	private String status;
	private String lastMessage;
	private String lastMessageTime;
	private int unreadCount;

	public ChatRoomListResDto(
		Long roomId,
		Long productId,
		String productName,
		Long buyerId,
		Long sellerId,
		String status,
		String lastMessage,
		LocalDateTime lastMessageTime,
		Long unreadCount
	) {
		this.roomId = roomId;
		this.productId = productId;
		this.productName = productName;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.status = status;
		this.lastMessage = lastMessage != null ? lastMessage : "";
		this.lastMessageTime = (lastMessageTime != null) ? lastMessageTime.toString() : "";
		this.unreadCount = (unreadCount != null) ? unreadCount.intValue() : 0;
	}
}