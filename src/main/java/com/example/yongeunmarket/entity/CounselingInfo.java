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
@Table(name = "counseling_info")
@Getter
@NoArgsConstructor
public class CounselingInfo extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@Column(name = "message_count", nullable = false)
	private short messageCount;

	@Column(name = "elapsed_time", nullable = false)
	private Integer elapsedTime;

	@Builder
	public CounselingInfo(ChatRoom chatRoom, short messageCount, Integer elapsedTime) {
		this.chatRoom = chatRoom;
		this.messageCount = messageCount;
		this.elapsedTime = elapsedTime;
	}
}
