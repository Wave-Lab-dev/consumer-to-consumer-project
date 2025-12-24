package com.example.yongeunmarket.service;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.example.yongeunmarket.dto.chat.ChatMessageResDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber {

	private final ObjectMapper objectMapper;
	private final SimpMessageSendingOperations messagingTemplate;

	/**
	 * Redis에서 메시지가 발행(Publish)되면 대기하고 있던 RedisSubscriber가 해당 메시지를 받아 처리
	 * RedisConfig에서 listenerAdapter를 통해 "sendMessage" 메서드를 호출하도록 설정
	 */
	public void sendMessage(String publishMessage) {
		try {
			// 1. Redis에서 받은 JSON 문자열(String)을 DTO 객체로 변환
			ChatMessageResDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageResDto.class);

			// 2. WebSocket 구독자들에게 채팅 메시지 Send
			// 해당 채팅방(/sub/chat/room/{roomId})을 구독 중인 모든 클라이언트에게 메시지 전달
			messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getRoomId(), chatMessage);

		} catch (Exception e) {
			log.error("Exception while sending message: {}", e.getMessage());
		}
	}
}