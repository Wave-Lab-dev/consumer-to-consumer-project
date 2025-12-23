package com.example.yongeunmarket.dto.chat;

import lombok.Getter;

@Getter
public class ChatMessageReqDto {
	private Long roomId;
	private String content;
	private Long senderId;
}
