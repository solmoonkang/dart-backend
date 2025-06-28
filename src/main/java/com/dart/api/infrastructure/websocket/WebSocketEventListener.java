package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.error.model.ErrorCode.*;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dart.api.application.chat.cache.CacheKeyMemoryStore;
import com.dart.api.application.chat.cache.ChatMessageCacheService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.request.cache.ChatRoomCacheDto;
import com.dart.api.dto.chat.request.cache.MemberCacheDto;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private static final String TEST_EMAIL = "thfans0521@naver.com";
	private static final String TEST_NICKNAME = "solmoon";

	private final MemberSessionRegistry memberSessionRegistry;
	private final MemberRepository memberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageCacheService chatMessageCacheService;
	private final CacheKeyMemoryStore cacheKeyMemoryStore;

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionSubscribeEvent);
		final String destination = extractDestinationFromHeaderAccessor(sessionSubscribeEvent);

		validateSessionIdPresent(sessionId);
		validateDestinationPresent(destination);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionSubscribeEvent);
		final Long chatRoomId = extractChatRoomIdFromDestination(destination);

		// ✅ 로그인한 사용자면 그대로 처리
		if (authUser != null) {
			if (chatMessageCacheService.isChatRoomNotCached(chatRoomId)) {
				final ChatRoom chatRoom = getChatRoomById(chatRoomId);
				chatMessageCacheService.cacheChatRoom(ChatRoomCacheDto.createChatRoomCacheDto(chatRoom));
			}

			if (chatMessageCacheService.isMemberNotCached(authUser.nickname())) {
				final Member member = getMemberByEmail(authUser.email());
				chatMessageCacheService.cacheMember(MemberCacheDto.createMemberCacheDto(member));
			}

			final MemberCacheDto memberCacheDto = chatMessageCacheService.getMemberCache(authUser.nickname());

			log.info("[✅ LOGGER] MEMBER {} IS JOIN CHATROOM", authUser.nickname());
			memberSessionRegistry.removeSessionByNickname(authUser.nickname());
			memberSessionRegistry.addSession(authUser.nickname(), sessionId, destination,
				memberCacheDto.profileImageURI());
			return;
		}

		// ✅ 테스트용 비인증 유저 처리 (임시)
		if (chatMessageCacheService.isChatRoomNotCached(chatRoomId)) {
			chatMessageCacheService.cacheChatRoom(new ChatRoomCacheDto(chatRoomId, "JMeter 테스트 채팅방"));
		}

		if (chatMessageCacheService.isMemberNotCached(TEST_NICKNAME)) {
			chatMessageCacheService.cacheMember(new MemberCacheDto(1L, TEST_NICKNAME, null));
		}

		final MemberCacheDto memberCacheDto = chatMessageCacheService.getMemberCache(TEST_NICKNAME);
		log.info("[⚠️ LOGGER] TEST USER {} IS JOIN CHATROOM WITHOUT LOGIN", TEST_NICKNAME);

		memberSessionRegistry.removeSessionByNickname(TEST_NICKNAME);
		memberSessionRegistry.addSession(TEST_NICKNAME, sessionId, destination, memberCacheDto.profileImageURI());
	}

	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent sessionDisconnectEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionDisconnectEvent);
		validateSessionIdPresent(sessionId);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionDisconnectEvent);

		// ✅ 로그인한 사용자면 그대로 처리
		if (authUser != null) {
			if (chatMessageCacheService.isMemberNotCached(authUser.nickname())) {
				log.warn("[⚠️ LOGGER] MEMBER {} DISCONNECTED BUT NOT CACHED", authUser.nickname());
			}

			log.info("[✅ LOGGER] MEMBER {} IS LEFT CHATROOM", authUser.nickname());
			memberSessionRegistry.removeSessionByNickname(authUser.nickname());
			return;
		}

		// ✅ 테스트용 비인증 유저 처리 (임시)
		log.info("[⚠️ LOGGER] TEST USER {} IS LEFT CHATROOM WITHOUT LOGIN", TEST_NICKNAME);
		memberSessionRegistry.removeSessionByNickname(TEST_NICKNAME);
	}

	private String extractSessionIdFromHeaderAccessor(AbstractSubProtocolEvent event) {
		return SimpMessageHeaderAccessor.wrap(event.getMessage()).getSessionId();
	}

	private String extractDestinationFromHeaderAccessor(AbstractSubProtocolEvent event) {
		return SimpMessageHeaderAccessor.wrap(event.getMessage()).getDestination();
	}

	private AuthUser extractAuthUserFromAttributes(AbstractSubProtocolEvent event) {
		return (AuthUser)Objects.requireNonNull(SimpMessageHeaderAccessor.wrap(event.getMessage())
			.getSessionAttributes()).get(CHAT_SESSION_USER);
	}

	private Long extractChatRoomIdFromDestination(String destination) {
		String[] parts = destination.split("/");
		String lastSegment = parts[parts.length - 1];
		return Long.valueOf(lastSegment);
	}

	private void validateSessionIdPresent(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			throw new BadRequestException(FAIL_INVALID_SESSION_ID);
		}
	}

	private void validateDestinationPresent(String destination) {
		if (destination == null || destination.isEmpty()) {
			throw new BadRequestException(FAIL_INVALID_DESTINATION);
		}
	}

	private Member getMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedException(FAIL_LOGIN_REQUIRED));
	}

	private ChatRoom getChatRoomById(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new NotFoundException(FAIL_CHAT_ROOM_NOT_FOUND));
	}
}
