package com.example.yongeunmarket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.yongeunmarket.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	int countByChatRoomId(Long roomId);

	// 특정 채팅방의 가장 최근 메시지 1개 조회
	Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(Long roomId);

	// 특정 방의 모든 메시지를 시간 오름차순(옛날 것부터)으로 조회
	List<ChatMessage> findAllByChatRoomIdOrderByCreatedAtAsc(Long roomId);

	// 안 읽은 메시지 개수 카운트 쿼리
	@Query("SELECT COUNT(m) FROM ChatMessage m " +
		"WHERE m.chatRoom.id = :roomId " +
		"AND m.user.id != :userId " +  // 내가 보낸 메시지는 제외
		"AND NOT EXISTS (" +           // 읽음 처리 테이블에 내 기록이 없는 경우
		"   SELECT r FROM ReadStatus r " +
		"   WHERE r.chatMessage = m " +
		"   AND r.user.id = :userId " +
		")")
	int countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
