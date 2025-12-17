package com.example.yongeunmarket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChatStatus status = ChatStatus.OPEN;

	@Column(name = "close_at", nullable = true)
	private LocalDateTime closedAt;

	// @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE) 보류
	// private List<ChatParticipant> participants = new ArrayList<>();
	//
	// @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
	// private List<ChatMessage> messages = new ArrayList<>();

	@Builder
	public ChatRoom(String name, ChatStatus status) {
		this.name = name;
		this.status = status;
	}
}
