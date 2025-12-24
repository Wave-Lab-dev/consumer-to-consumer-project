package com.example.yongeunmarket.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.yongeunmarket.dto.adminChat.GetAdminChatRoomDetailResDto;
import com.example.yongeunmarket.dto.adminChat.GetAdminChatRoomInfoResDto;
import com.example.yongeunmarket.dto.chat.ChatRoomCloseResDto;
import com.example.yongeunmarket.dto.chat.ChatRoomDetailResDto;
import com.example.yongeunmarket.dto.chat.ChatRoomListResDto;
import com.example.yongeunmarket.dto.chat.CreateChatRoomReqDto;
import com.example.yongeunmarket.dto.chat.CreateChatRoomResDto;
import com.example.yongeunmarket.entity.ChatMessage;
import com.example.yongeunmarket.entity.ChatParticipant;
import com.example.yongeunmarket.entity.ChatRoom;
import com.example.yongeunmarket.entity.ChatStatus;
import com.example.yongeunmarket.entity.CounselingInfo;
import com.example.yongeunmarket.entity.Product;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.ChatMessageRepository;
import com.example.yongeunmarket.repository.ChatParticipantRepository;
import com.example.yongeunmarket.repository.ChatRoomRepository;
import com.example.yongeunmarket.repository.CounselingInfoRepository;
import com.example.yongeunmarket.repository.ProductRepository;
import com.example.yongeunmarket.repository.ReadStatusRepository;
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
	private final CounselingInfoRepository counselingInfoRepository;
	private final ReadStatusRepository readStatusRepository;

	// 채팅방 생성
	@Transactional
	public CreateChatRoomResDto createChatRoom(CreateChatRoomReqDto request, Long buyerId) {
		// 1. 사용자(구매자) 조회
		User buyer = userRepository.findById(buyerId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 2. 상품 조회
		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

		User seller = product.getUser();

		// 3. 유효성 검사 (자신의 상품에 채팅 불가)
		if (buyer.getId().equals(seller.getId())) {
			throw new IllegalArgumentException("자신의 상품에는 채팅을 요청할 수 없습니다.");
		}

		// 4. 기존 방 확인
		return chatRoomRepository.findByProductAndBuyer(product, buyer)
			.map(existingRoom -> makeResponse(existingRoom, product, buyer, seller))
			.orElseGet(() -> createNewRoom(request, product, buyer, seller));
	}

	// 상담 종료
	@Transactional
	public ChatRoomCloseResDto closeChatRoom(Long roomId, Long userId) {
		// 1. 채팅방 조회
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

		// 2. 본인(구매자) 방인지 확인
		if (!chatRoom.getBuyer().getId().equals(userId)) {
			throw new IllegalArgumentException("본인의 상담방만 종료할 수 있습니다.");
		}

		// 3. 이미 종료된 방인지 체크
		if (chatRoom.getStatus() == ChatStatus.CLOSED) {
			throw new IllegalStateException("이미 종료된 상담방입니다.");
		}

		// 4. 통계 계산
		int messageCountInt = chatMessageRepository.countByChatRoomId(roomId);
		short messageCount = (short)messageCountInt;

		// 5. 경과 시간 계산
		LocalDateTime now = LocalDateTime.now();
		long durationMinutes = Duration.between(chatRoom.getCreatedAt(), now).toMinutes();

		// 6. ChatRoom 상태 변경
		chatRoom.chatRoomClose();

		// 7. CounselingInfo 엔티티 생성 및 저장
		CounselingInfo counselingInfo = CounselingInfo.builder()
			.chatRoom(chatRoom)
			.messageCount(messageCount)
			.elapsedTime((int)durationMinutes)
			.build();

		counselingInfoRepository.save(counselingInfo);

		// 8. 결과 반환
		return ChatRoomCloseResDto.builder()
			.roomId(chatRoom.getId())
			.status(chatRoom.getStatus().name())
			.closedAt(chatRoom.getClosedAt().toString())
			.summary(ChatRoomCloseResDto.ChatRoomSummary.builder()
				.totalMessages(messageCountInt)
				.durationMinutes((int)durationMinutes)
				.build())
			.build();
	}

	// 내 채팅방 목록 조회(리펙토링)
	public List<ChatRoomListResDto> findAllChatRooms(Long userId) {
		return chatRoomRepository.findAllMyChatRooms(userId);
	}

	// 채팅방 상세 조회
	public ChatRoomDetailResDto findChatRoomDetail(Long roomId, Long userId) {
		// 1. 채팅방 조회
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

		Product product = chatRoom.getProduct();
		User seller = chatRoom.getSeller();
		User buyer = chatRoom.getBuyer();

		// 2. 권한 검사
		if (!userId.equals(buyer.getId()) && !userId.equals(seller.getId())) {
			throw new IllegalArgumentException("해당 채팅방에 접근 권한이 없습니다.");
		}

		// 3. 메시지 목록 조회
		List<ChatMessage> messages = chatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(roomId);

		// 4. 메시지 DTO 리스트 변환
		List<ChatRoomDetailResDto.ChatMessageDetailDto> messageDtos = messages.stream()
			.map(msg -> {
				User otherUser = msg.getUser().getId().equals(buyer.getId()) ? seller : buyer;
				boolean isRead = readStatusRepository.findByChatMessageAndUser(msg, otherUser).isPresent();

				return ChatRoomDetailResDto.ChatMessageDetailDto.builder()
					.messageId(msg.getId())
					.senderId(msg.getUser().getId())
					.content(msg.getContent())
					.createdAt(msg.getCreatedAt().toString())
					.isRead(isRead)
					.build();
			})
			.collect(Collectors.toList());

		// 5. 결과 반환
		return ChatRoomDetailResDto.builder()
			.roomId(chatRoom.getId())
			.productId(product.getId())
			.productName(product.getName())
			.sellerId(seller.getId())
			.buyerId(buyer.getId())
			.status(chatRoom.getStatus().name())
			.messages(messageDtos)
			.build();
	}

	// --- 신규 방 생성 로직 (Private) ---
	private CreateChatRoomResDto createNewRoom(CreateChatRoomReqDto request, Product product, User buyer, User seller) {

		ChatRoom chatRoom = ChatRoom.builder()
			.roomName(product.getName())
			.product(product)
			.buyer(buyer)
			.seller(seller)
			.status(ChatStatus.OPEN)
			.build();

		chatRoomRepository.save(chatRoom);

		chatParticipantRepository.save(ChatParticipant.builder().chatRoom(chatRoom).user(buyer).build());
		chatParticipantRepository.save(ChatParticipant.builder().chatRoom(chatRoom).user(seller).build());

		ChatMessage message = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(buyer)
			.content(request.getContent())
			.build();
		chatMessageRepository.save(message);

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

	public List<GetAdminChatRoomInfoResDto> findAllChatRoomsByFilter(ChatStatus status) {
		// 1. 모든 채팅방 조회
		List<ChatRoom> myChatRooms;

		// 2. 필터링하여 채팅방 목록 가져오기
		if (status == ChatStatus.OPEN) {
			myChatRooms = chatRoomRepository.findAllByStatus(status.name());
		} else {
			myChatRooms = chatRoomRepository.findAll(Sort.by("status").ascending());
		}

		// 2. DTO 리스트로 변환
		return myChatRooms.stream().map(chatRoom -> {

			Product product = chatRoom.getProduct();
			User seller = chatRoom.getSeller();
			User buyer = chatRoom.getBuyer();

			return GetAdminChatRoomInfoResDto.builder()
				.roomId(chatRoom.getId())
				.productId(product.getId())
				.buyerId(buyer.getId())
				.sellerId(seller.getId())
				.status(chatRoom.getStatus())
				.createdAt(chatRoom.getCreatedAt())
				.build();
		}).collect(Collectors.toList());
	}

	public GetAdminChatRoomDetailResDto findChatRoomDetailByRoomId(Long roomId) {
		// 1. 채팅방 조회
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

		Product product = chatRoom.getProduct();
		User seller = chatRoom.getSeller();
		User buyer = chatRoom.getBuyer();

		// 4. 메시지 목록 조회
		List<ChatMessage> messages = chatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(roomId);

		// 5. 메시지 DTO 리스트 변환
		List<ChatRoomDetailResDto.ChatMessageDetailDto> messageDtos = messages.stream()
			.map(msg -> {
				User recipient = msg.getUser().getId().equals(buyer.getId()) ? seller : buyer;

				boolean isRead = readStatusRepository.findByChatMessageAndUser(msg, recipient).isPresent();

				return ChatRoomDetailResDto.ChatMessageDetailDto.builder()
					.messageId(msg.getId())
					.senderId(msg.getUser().getId())
					.content(msg.getContent())
					.createdAt(msg.getCreatedAt().toString())
					.isRead(isRead)
					.build();
			})
			.collect(Collectors.toList());

		// 6. 결과 반환
		return GetAdminChatRoomDetailResDto.builder()
			.roomId(chatRoom.getId())
			.productId(product.getId())
			.sellerId(seller.getId())
			.buyerId(buyer.getId())
			.status(chatRoom.getStatus().name())
			.messages(messageDtos)
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
