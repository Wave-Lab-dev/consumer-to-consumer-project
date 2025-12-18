package com.example.yongeunmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.chat.ChatRoomCloseResDto;
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

		// 사용자 ID 추출
		Long buyerId = userDetails.getUserId();
		CreateChatRoomResDto response = chatRoomService.createChatRoom(request, buyerId);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PatchMapping("/{roomId}/close")
	public ResponseEntity<ChatRoomCloseResDto> closeChatRoom(
		@PathVariable Long roomId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		// 사용자 ID 추출
		Long userId = userDetails.getUserId();
		ChatRoomCloseResDto response = chatRoomService.closeChatRoom(roomId, userId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}

