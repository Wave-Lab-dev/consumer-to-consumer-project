package com.example.yongeunmarket.dto.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateChatRoomReqDto {
	private Long productId;
	private String content; // 첫 번째 메시지 내용
}
