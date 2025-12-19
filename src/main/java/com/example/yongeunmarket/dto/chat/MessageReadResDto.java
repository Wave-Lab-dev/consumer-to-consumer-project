package com.example.yongeunmarket.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageReadResDto {
	private Long messageId;
	private Long roomId;
	private Long readerId;
	private boolean isRead;
}