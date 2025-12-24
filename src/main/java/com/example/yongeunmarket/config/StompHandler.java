package com.example.yongeunmarket.config;

import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.yongeunmarket.jwt.JwtTokenProvider;
import com.example.yongeunmarket.repository.ChatRoomRepository;
import com.example.yongeunmarket.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final ChatRoomRepository chatRoomRepository;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// 연결 요청
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String token = accessor.getFirstNativeHeader("Authorization");

			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);
			}

			if (token != null && jwtTokenProvider.validateToken(token)) {
				Authentication auth = jwtTokenProvider.getAuthentication(token);

				// 1) 기본 저장: 스프링 시큐리티 컨텍스트에 유저 정보를 넣어줌(일반적인 방식)
				accessor.setUser(auth);

				// 2) 세션 저장소에 강제로 저장
				Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
				if (sessionAttributes != null) {
					sessionAttributes.put("USER_AUTH", auth);
				}

			} else {
				throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
			}
		}

		// 구독 요청
		else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

			// 1) 기본 저장소
			Authentication auth = (Authentication)accessor.getUser();
			// 1-1) accessor.getUser()가 null이면, 아까 저장해둔 세션에서 꺼내오기
			if (auth == null) {
				Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
				if (sessionAttributes != null) {
					auth = (Authentication)sessionAttributes.get("USER_AUTH");
				}
			}

			if (auth == null) {
				throw new IllegalArgumentException("로그인이 필요합니다.");
			}

			// 2) 유저 정보 꺼내기
			CustomUserDetails userDetails = (CustomUserDetails)auth.getPrincipal();
			Long userId = userDetails.getUserId();

			// 3) 방 번호 파싱
			String destination = accessor.getDestination();
			if (destination != null && destination.startsWith("/sub/chat/room/")) {
				String roomIdStr = destination.substring(destination.lastIndexOf("/") + 1);
				Long roomId;
				try {
					roomId = Long.parseLong(roomIdStr);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("잘못된 방 번호 형식입니다.");
				}

				// 4) DB 권한 검사
				boolean isParticipant = chatRoomRepository.existsByRoomIdAndUser(roomId, userId);

				if (!isParticipant) {
					throw new IllegalArgumentException("해당 채팅방에 참여할 권한이 없습니다.");
				}
			}
		}

		return message;
	}
}