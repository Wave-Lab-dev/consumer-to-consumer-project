package com.example.yongeunmarket.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.adminChat.GetAdminChatRoomDetailResDto;
import com.example.yongeunmarket.dto.adminChat.GetAdminChatRoomInfoResDto;
import com.example.yongeunmarket.entity.ChatStatus;
import com.example.yongeunmarket.service.ChatRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/admin/chatrooms")
@RequiredArgsConstructor
public class AdminChatController {

	private final ChatRoomService chatRoomService;

	@GetMapping
	public ResponseEntity<List<GetAdminChatRoomInfoResDto>> getAllChatRooms(
		@RequestParam(required = false) ChatStatus status) {
		List<GetAdminChatRoomInfoResDto> chatRooms = chatRoomService.findAllChatRoomsByFilter(status);

		return ResponseEntity.ok(chatRooms);
	}

	@GetMapping("/{roomId}")
	public ResponseEntity<GetAdminChatRoomDetailResDto> getChatRooms(
		@PathVariable Long roomId) {
		GetAdminChatRoomDetailResDto chatRoomDetail = chatRoomService.findChatRoomDetailByRoomId(roomId);

		return ResponseEntity.ok(chatRoomDetail);
	}
}
