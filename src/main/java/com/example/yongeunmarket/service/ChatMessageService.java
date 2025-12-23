package com.example.yongeunmarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.yongeunmarket.dto.chat.ChatMessageReqDto;
import com.example.yongeunmarket.dto.chat.ChatMessageResDto;
import com.example.yongeunmarket.dto.chat.MessageReadResDto;
import com.example.yongeunmarket.entity.ChatMessage;
import com.example.yongeunmarket.entity.ChatRoom;
import com.example.yongeunmarket.entity.ChatStatus;
import com.example.yongeunmarket.entity.ReadStatus;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.ChatMessageRepository;
import com.example.yongeunmarket.repository.ChatRoomRepository;
import com.example.yongeunmarket.repository.ReadStatusRepository;
import com.example.yongeunmarket.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final ReadStatusRepository readStatusRepository;

	@Transactional
	public ChatMessageResDto saveMessage(ChatMessageReqDto reqDto) {

		// 1. 채팅방 조회
		ChatRoom chatRoom = chatRoomRepository.findById(reqDto.getRoomId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

		// 채팅방 상태 검사 (CLOSED면 메시지 전송 차단)
		if (chatRoom.getStatus() == ChatStatus.CLOSED) {
			throw new IllegalStateException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
		}

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

	@Transactional
	public MessageReadResDto readMessage(Long messageId, Long userId) {
		// 1. 메시지 조회
		ChatMessage message = chatMessageRepository.findById(messageId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));

		// 2. 읽은 사람 조회
		User reader = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 3. 기존에 읽음 처리 데이터가 있는지 확인
		ReadStatus readStatus = readStatusRepository.findByChatMessageAndUser(message, reader)
			.orElseGet(() -> ReadStatus.builder()
				.chatMessage(message)
				.user(reader)
				.isRead(true)
				.build());

		// 혹시 기존 데이터가 isRead=false 였다면 true로 확실하게 변경
		if (!readStatus.getIsRead()) {
			readStatus.updateStatus(true);
		}

		readStatusRepository.save(readStatus);

		// 4. DTO 반환
		return MessageReadResDto.builder()
			.messageId(message.getId())
			.roomId(message.getChatRoom().getId())
			.readerId(reader.getId())
			.isRead(readStatus.getIsRead())
			.build();
	}
}