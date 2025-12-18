package com.example.yongeunmarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 클라이언트가 처음 연결할 URL (예: ws://localhost:8080/ws)
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*")
			.withSockJS(); // 낮은 버전 브라우저 지원을 위한 설정
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 메시지 구독 요청 URL prefix (예: /sub/chat/room/1)
		registry.enableSimpleBroker("/sub");

		// 메시지 발행 요청 URL prefix (예: /pub/chat/message)
		registry.setApplicationDestinationPrefixes("/pub");
	}
}
