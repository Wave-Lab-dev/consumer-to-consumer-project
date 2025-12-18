package com.example.yongeunmarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.yongeunmarket.dto.chat.ChatMessageReqDto;
import com.example.yongeunmarket.dto.chat.ChatMessageResDto;
import com.example.yongeunmarket.entity.ChatMessage;
import com.example.yongeunmarket.entity.ChatRoom;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.ChatMessageRepository;
import com.example.yongeunmarket.repository.ChatRoomRepository;
import com.example.yongeunmarket.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	@Transactional
	public ChatMessageResDto saveMessage(ChatMessageReqDto reqDto) {

		// 1. 채팅방 조회
		ChatRoom chatRoom = chatRoomRepository.findById(reqDto.getRoomId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

		// 2. 유저 조회
		User user = userRepository.findById(reqDto.getSenderId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

		// 3. 엔티티 생성
		ChatMessage message = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(user)
			.content(reqDto.getContent())
			.build();

		// 4. 저장
		ChatMessage savedMessage = chatMessageRepository.save(message);

		// 5.Dto 변환
		return ChatMessageResDto.builder()
			.messageId(savedMessage.getId())
			.roomId(savedMessage.getChatRoom().getId())
			.senderId(savedMessage.getUser().getId())
			.content(savedMessage.getContent())
			.createdAt(savedMessage.getCreatedAt().toString())
			.build();
	}
}