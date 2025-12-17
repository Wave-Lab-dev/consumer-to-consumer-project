package com.example.yongeunmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yongeunmarket.entity.ChatParticipant;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
}
