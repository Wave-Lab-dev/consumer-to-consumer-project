package com.example.yongeunmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yongeunmarket.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	int countByChatRoomId(Long roomId);
}
