package com.example.yongeunmarket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yongeunmarket.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	int countByChatRoomId(Long roomId);

	// 특정 채팅방의 가장 최근 메시지 1개 조회
	Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(Long roomId);

	// 안 읽은 메시지 개수 (임시)
	int countByChatRoomIdAndUserIdNot(Long roomId, Long userId);
}
