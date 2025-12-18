package com.example.yongeunmarket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yongeunmarket.entity.ChatMessage;
import com.example.yongeunmarket.entity.ReadStatus;
import com.example.yongeunmarket.entity.User;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {

	// 이미 읽음 처리가 되어 있는지 확인하기 위해 조회
	Optional<ReadStatus> findByChatMessageAndUser(ChatMessage chatMessage, User user);
}