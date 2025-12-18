package com.example.yongeunmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.chat.CreateChatRoomReqDto;
import com.example.yongeunmarket.dto.chat.CreateChatRoomResDto;
import com.example.yongeunmarket.security.CustomUserDetails;
import com.example.yongeunmarket.service.ChatRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	@PostMapping
	public ResponseEntity<CreateChatRoomResDto> createChatRoom(
		@RequestBody CreateChatRoomReqDto request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		// CustomUserDetails에서 userId를 직접 추출
		Long buyerId = userDetails.getUserId();

		// 서비스에 이메일 대신 ID를 전달
		CreateChatRoomResDto response = chatRoomService.createChatRoom(request, buyerId);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}

