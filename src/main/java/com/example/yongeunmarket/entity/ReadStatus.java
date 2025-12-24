package com.example.yongeunmarket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "read_statuses")
@Getter
@NoArgsConstructor
public class ReadStatus extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "is_read", nullable = false)
	private Boolean isRead;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", nullable = false)
	private ChatMessage chatMessage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder
	public ReadStatus(Boolean isRead, ChatMessage chatMessage, User user) {
		this.isRead = isRead;
		this.chatMessage = chatMessage;
		this.user = user;
	}

	public void updateStatus(Boolean isRead) {
		this.isRead = isRead;
	}
}