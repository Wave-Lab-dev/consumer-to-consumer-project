package com.example.yongeunmarket.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageReqDto {
	private Long roomId;
	private String content;
	private Long senderId;
}