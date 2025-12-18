package com.example.yongeunmarket.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResDto {
	private Long messageId;
	private Long roomId;
	private Long senderId;
	private String content;
	private String createdAt;
}
