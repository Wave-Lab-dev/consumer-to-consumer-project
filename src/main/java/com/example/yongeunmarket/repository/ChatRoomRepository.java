package com.example.yongeunmarket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.yongeunmarket.dto.chat.ChatRoomListResDto;
import com.example.yongeunmarket.entity.ChatRoom;
import com.example.yongeunmarket.entity.Product;
import com.example.yongeunmarket.entity.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// 방 생성 시 확인용
	Optional<ChatRoom> findByProductAndBuyer(Product product, User buyer);

	// DTO 직접 조회 (31번 쿼리 -> 1번 쿼리로 단축)
	@Query("SELECT new com.example.yongeunmarket.dto.chat.ChatRoomListResDto(" +
		"  c.id, " +
		"  c.product.id, " +
		"  c.product.name, " +
		"  c.buyer.id, " +
		"  c.seller.id, " +
		"  CAST(c.status AS string), " +
		"  (SELECT m.content FROM ChatMessage m WHERE m.chatRoom.id = c.id ORDER BY m.createdAt DESC LIMIT 1), " +
		"  (SELECT m.createdAt FROM ChatMessage m WHERE m.chatRoom.id = c.id ORDER BY m.createdAt DESC LIMIT 1), " +
		"  (SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = c.id AND m.user.id != :userId " +
		"   AND NOT EXISTS (SELECT r FROM ReadStatus r WHERE r.chatMessage.id = m.id AND r.user.id = :userId)) " +
		") " +
		"FROM ChatRoom c " +
		"JOIN c.product p " +
		"WHERE c.buyer.id = :userId OR c.seller.id = :userId " +
		"ORDER BY (SELECT MAX(m.createdAt) FROM ChatMessage m WHERE m.chatRoom.id = c.id) DESC")
	List<ChatRoomListResDto> findAllMyChatRooms(@Param("userId") Long userId);
}