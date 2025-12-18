package com.example.yongeunmarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.chat.CreateChatRoomReqDto;
import com.example.yongeunmarket.dto.chat.CreateChatRoomResDto;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.UserRepository;
import com.example.yongeunmarket.service.ChatRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;
	private final UserRepository userRepository; // 임시 테스트용

	@PostMapping
	public ResponseEntity<CreateChatRoomResDto> createChatRoom(@RequestBody CreateChatRoomReqDto request) {

		// 임시 로그인
		Long tempBuyerId = 1L;
		User buyer = userRepository.findById(tempBuyerId)
			.orElseThrow(() -> new IllegalArgumentException("임시 테스트용 유저(ID:1)가 DB에 없습니다."));
		// -----------------------------------------------------------------

		CreateChatRoomResDto response = chatRoomService.createChatRoom(buyer, request);
		return ResponseEntity.ok(response);
	}
}

