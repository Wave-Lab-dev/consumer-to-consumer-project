package com.example.yongeunmarket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.example.yongeunmarket.dto.chat.ChatMessageReqDto;
import com.example.yongeunmarket.dto.chat.ChatMessageResDto;
import com.example.yongeunmarket.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

	private final ChatMessageService chatMessageService;

	// 클라이언트가 /pub/chat/message 로 JSON을 쏘면 여기가 실행됨
	@MessageMapping("/chat/message")
	public void sendMessage(ChatMessageReqDto messageReqDto) {

		// DB에 저장하고, 날짜/ID가 포함된 응답용 객체(ResDto)를 돌려받음
		ChatMessageResDto responseDto = chatMessageService.saveMessage(messageReqDto);
	}
}

