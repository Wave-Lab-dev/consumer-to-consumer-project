package com.example.yongeunmarket.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomCloseResDto {
	private Long roomId;
	private String status;
	private String closedAt;
	private ChatRoomSummary summary;

	@Getter
	@Builder
	public static class ChatRoomSummary {
		private int totalMessages;
		private int durationMinutes;
	}
}