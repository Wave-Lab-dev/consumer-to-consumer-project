package com.example.yongeunmarket.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.yongeunmarket.dto.chat.ChatMessageResDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {

	@Qualifier("chatPubSub")
	private final StringRedisTemplate stringRedisTemplate;

	private final ObjectMapper objectMapper;

	public void publish(ChatMessageResDto message) {
		try {
			String messageJson = objectMapper.writeValueAsString(message);
			stringRedisTemplate.convertAndSend("chatroom", messageJson);
		} catch (Exception e) {
			log.error("메시지 발행 실패: {}", e.getMessage());
		}
	}
}