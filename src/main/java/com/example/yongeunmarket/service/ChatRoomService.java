package com.example.yongeunmarket.service;

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
import com.example.yongeunmarket.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatParticipantRepository chatParticipantRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	@Transactional
	public CreateChatRoomResDto createChatRoom(CreateChatRoomReqDto request, Long buyerId) {
		// 1. 사용자(구매자) 조회
		User buyer = userRepository.findById(buyerId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

		User seller = product.getUser();

		// 2. 유효성 검사 (자신의 상품에 채팅 불가)
		if (buyer.getId().equals(seller.getId())) {
			throw new IllegalArgumentException("자신의 상품에는 채팅을 요청할 수 없습니다.");
		}

		// 3. 기존 방 확인
		String roomName = String.valueOf(product.getId());

		return chatRoomRepository.findByNameAndBuyer(roomName, buyer)
			.map(existingRoom -> makeResponse(existingRoom, product, buyer, seller)) // 기존 방 있으면 반환
			.orElseGet(() -> createNewRoom(request, product, buyer, seller, roomName)); // 없으면 생성
	}

	// --- 신규 방 생성 로직 ---
	private CreateChatRoomResDto createNewRoom(CreateChatRoomReqDto request, Product product, User buyer, User seller,
		String roomName) {
		// 1. 방 생성
		ChatRoom chatRoom = ChatRoom.builder()
			.name(roomName)
			.status(ChatStatus.OPEN)
			.build();
		chatRoomRepository.save(chatRoom);

		// 2. 참여자 등록
		chatParticipantRepository.save(ChatParticipant.builder().chatRoom(chatRoom).user(buyer).build());
		chatParticipantRepository.save(ChatParticipant.builder().chatRoom(chatRoom).user(seller).build());

		// 3. 첫 메시지 저장
		ChatMessage message = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(buyer)
			.content(request.getContent())
			.build();
		chatMessageRepository.save(message);

		// 4. 응답 생성
		return CreateChatRoomResDto.builder()
			.roomId(chatRoom.getId())
			.buyerId(buyer.getId())
			.sellerId(seller.getId())
			.productId(product.getId())
			.createdAt(chatRoom.getCreatedAt())
			.firstMessage(CreateChatRoomResDto.FirstMessageDto.builder()
				.messageId(message.getId())
				.senderId(buyer.getId())
				.content(message.getContent())
				.createdAt(message.getCreatedAt())
				.build())
			.build();
	}

	// --- 기존 방 응답 변환 로직 ---
	private CreateChatRoomResDto makeResponse(ChatRoom chatRoom, Product product, User buyer, User seller) {

		return CreateChatRoomResDto.builder()
			.roomId(chatRoom.getId())
			.buyerId(buyer.getId())
			.sellerId(seller.getId())
			.productId(product.getId())
			.createdAt(chatRoom.getCreatedAt())
			.firstMessage(null)
			.build();
	}
}