package com.example.yongeunmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.chat.MessageReadResDto;
import com.example.yongeunmarket.security.CustomUserDetails;
import com.example.yongeunmarket.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MessageController {

	private final ChatMessageService chatMessageService;

	@PostMapping("/api/messages/{messageId}/read")
	public ResponseEntity<MessageReadResDto> readMessage(
		@PathVariable Long messageId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		Long userId = userDetails.getUserId();
		MessageReadResDto response = chatMessageService.readMessage(messageId, userId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
