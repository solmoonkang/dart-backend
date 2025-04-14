package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dart.api.application.chat.batch.ChatCacheService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final MemberSessionRegistry memberSessionRegistry;
	private final MemberRepository memberRepository;
	private final ChatCacheService chatCacheService;

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionSubscribeEvent);
		final String destination = extractDestinationFromHeaderAccessor(sessionSubscribeEvent);

		validateSessionIdPresent(sessionId);
		validateDestinationPresent(destination);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionSubscribeEvent);
		if (authUser == null) return;

		final Long chatRoomId = extractChatRoomIdFromDestination(destination);
		final String nickname = authUser.nickname();

		chatCacheService.ensureChatRoomExistsInCache(chatRoomId);
		chatCacheService.ensureMemberExistsInCache(nickname);

		final Member member = getMemberByEmail(authUser.email());
		log.info("[✅ LOGGER] MEMBER {} IS JOIN CHATROOM", authUser.nickname());
		memberSessionRegistry.removeSessionByNickname(member.getNickname());
		memberSessionRegistry.addSession(member.getNickname(), sessionId, destination, member.getProfileImageUrl());
	}

	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent sessionDisconnectEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionDisconnectEvent);
		validateSessionIdPresent(sessionId);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionDisconnectEvent);
		if (authUser == null) {
			return;
		}

		log.info("[✅ LOGGER] MEMBER {} IS LEFT CHATROOM", authUser.nickname());
		memberSessionRegistry.removeSession(sessionId);
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
			throw new BadRequestException(ErrorCode.FAIL_INVALID_SESSION_ID);
		}
	}

	private void validateDestinationPresent(String destination) {
		if (destination == null || destination.isEmpty()) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_DESTINATION);
		}
	}

	private Member getMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED));
	}
}
