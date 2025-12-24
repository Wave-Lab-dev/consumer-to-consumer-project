package com.example.yongeunmarket.controller;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.example.yongeunmarket.dto.chat.ChatMessageReqDto;
import com.example.yongeunmarket.security.CustomUserDetails;
import com.example.yongeunmarket.service.ChatMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

	private final ChatMessageService chatMessageService;

	@MessageMapping("/chat/message")
	public void sendMessage(ChatMessageReqDto messageReqDto,
		SimpMessageHeaderAccessor headerAccessor) {

		// 1. 세션 저장소에서 인증 정보 꺼내기
		// StompHandler에서 해둔 것을 여기서 찾음
		Authentication auth = null;
		Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

		if (sessionAttributes != null) {
			auth = (Authentication)sessionAttributes.get("USER_AUTH");
		}

		// 2. 인증 정보가 없으면 에러 처리
		if (auth == null) {
			log.error("❌ Controller: 인증 정보가 없습니다. (Session Attributes is null)");
			return;
		}

		// 3. 사용자 정보 추출
		CustomUserDetails userDetails = (CustomUserDetails)auth.getPrincipal();

		// 4. ID 강제 주입
		// 서버는 클라이언트가 보낸 데이터를 받지 않고, 서버가 가진 토큰 정보로 강제 수정
		messageReqDto.setSenderId(userDetails.getUserId());

		// 5. DB 저장 및 전송
		log.info("✅ Controller: 메시지 전송 성공 SenderID: {}", userDetails.getUserId());
		chatMessageService.saveMessage(messageReqDto);
	}
}