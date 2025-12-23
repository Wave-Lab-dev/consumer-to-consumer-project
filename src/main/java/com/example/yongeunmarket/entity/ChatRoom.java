package com.example.yongeunmarket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor
public class ChatRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 더 이상 name에 productId를 억지로 넣지 않습니다.
	// 필요하다면 화면 표시용 이름으로 쓰거나, 제거해도 무방합니다.
	@Column(nullable = false)
	private String roomName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChatStatus status = ChatStatus.OPEN;

	@Column(name = "close_at")
	private LocalDateTime closedAt;

	// ✅ [핵심 변경] Product와 직접 연관관계 매핑
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	// ✅ [핵심 변경] Seller 명시
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id", nullable = false)
	private User seller;

	// ✅ [핵심 변경] Buyer 명시
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;

	@Builder
	public ChatRoom(String roomName, ChatStatus status, Product product, User seller, User buyer) {
		this.roomName = roomName;
		this.status = status;
		this.product = product;
		this.seller = seller;
		this.buyer = buyer;
	}

	public void chatRoomClose() {
		this.status = ChatStatus.CLOSED;
		this.closedAt = LocalDateTime.now();
	}
}