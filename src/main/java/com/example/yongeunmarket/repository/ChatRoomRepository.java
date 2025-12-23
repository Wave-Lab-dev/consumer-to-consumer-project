package com.example.yongeunmarket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.yongeunmarket.entity.ChatRoom;
import com.example.yongeunmarket.entity.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// "같은 상품 이름(방 이름)"을 가진 방 중에서, "해당 구매자(User)"가 참여하고 있는 방 찾기
	@Query("SELECT cr FROM ChatRoom cr " +
		"JOIN ChatParticipant cp ON cr.id = cp.chatRoom.id " +
		"WHERE cr.name = :roomName AND cp.user = :buyer")
	Optional<ChatRoom> findByNameAndBuyer(@Param("roomName") String roomName,
		@Param("buyer") User buyer);

	// 내가 참여 중인 모든 채팅방 조회
	@Query("SELECT cp.chatRoom FROM ChatParticipant cp WHERE cp.user.id = :userId")
	List<ChatRoom> findMyChatRooms(@Param("userId") Long userId);
}