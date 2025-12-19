package com.example.yongeunmarket.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.yongeunmarket.dto.chat.CreateChatRoomReqDto;
import com.example.yongeunmarket.dto.chat.CreateChatRoomResDto;
import com.example.yongeunmarket.entity.ChatMessage;
import com.example.yongeunmarket.entity.ChatParticipant;
import com.example.yongeunmarket.entity.ChatRoom;
import com.example.yongeunmarket.entity.ChatStatus;
import com.example.yongeunmarket.entity.Product;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.ChatMessageRepository;
import com.example.yongeunmarket.repository.ChatParticipantRepository;
import com.example.yongeunmarket.repository.ChatRoomRepository;
import com.example.yongeunmarket.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatParticipantRepository chatParticipantRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ProductRepository productRepository;

	@Transactional
	public CreateChatRoomResDto createChatRoom(User buyer, CreateChatRoomReqDto request) {

		// 1. 상품 조회 (판매자 및 상품명 확인용)
		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
		User seller = product.getUser();
		String roomName = product.getName(); // 방 이름은 상품명으로 설정

		// 2. 중복 방 확인 (상품명 + 구매자 기준)
		Optional<ChatRoom> existingRoom = chatRoomRepository.findByNameAndBuyer(roomName, buyer);
		if (existingRoom.isPresent()) {
			// 이미 방이 있으면 기존 방 정보 리턴 (메시지는 null 처리)
			return mapToResDto(existingRoom.get(), buyer, seller, null, product.getId());
		}

		// 3. 채팅방 생성
		ChatRoom chatRoom = ChatRoom.builder()
			.name(roomName)
			.status(ChatStatus.OPEN)
			.build();
		ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

		// 4. 참여자(Participant) 등록 - 구매자와 판매자 모두 등록
		ChatParticipant buyerParticipant = ChatParticipant.builder()
			.chatRoom(savedRoom)
			.user(buyer)
			.build();
		ChatParticipant sellerParticipant = ChatParticipant.builder()
			.chatRoom(savedRoom)
			.user(seller)
			.build();

		chatParticipantRepository.save(buyerParticipant);
		chatParticipantRepository.save(sellerParticipant);

		// 5. 첫 번째 메시지 저장 (보낸 사람: 구매자)
		ChatMessage firstMessage = ChatMessage.builder()
			.chatRoom(savedRoom)
			.user(buyer) // 엔티티 필드명이 sender가 아니라 user임
			.content(request.getContent())
			.build();
		ChatMessage savedMessage = chatMessageRepository.save(firstMessage);

		// 6. 결과 반환
		return mapToResDto(savedRoom, buyer, seller, savedMessage, product.getId());
	}

	// 응답 DTO 변환 메서드
	private CreateChatRoomResDto mapToResDto(ChatRoom room, User buyer, User seller, ChatMessage message,
		Long productId) {
		CreateChatRoomResDto.FirstMessageDto messageDto = null;

		if (message != null) {
			messageDto = CreateChatRoomResDto.FirstMessageDto.builder()
				.messageId(message.getId())
				.senderId(message.getUser().getId())
				.content(message.getContent())
				.createdAt(message.getCreatedAt())
				.build();
		}

		return CreateChatRoomResDto.builder()
			.roomId(room.getId())
			.buyerId(buyer.getId())
			.sellerId(seller.getId())
			.productId(productId)
			.createdAt(room.getCreatedAt())
			.firstMessage(messageDto)
			.build();
	}
}