package com.example.yongeunmarket.dto.adminChat;

import java.time.LocalDateTime;

import com.example.yongeunmarket.entity.ChatStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetAdminChatRoomDetailResDto {
	private Long roomId;
	private Long productId;
	private Long buyerId;
	private Long sellerId;
	private ChatStatus status;
	private LocalDateTime createdAt;
}
