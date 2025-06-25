package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dart.api.application.chat.cache.ChatMessageCacheService;
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
	private final ChatMessageCacheService chatMessageCacheService;

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionSubscribeEvent);
		final String destination = extractDestinationFromHeaderAccessor(sessionSubscribeEvent);

		validateSessionIdPresent(sessionId);
		validateDestinationPresent(destination);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionSubscribeEvent);
		if (authUser == null) {
			log.warn("[✅ LOGGER] 캐싱 처리로 인한 테스트로 인해 현재 사용자 인증을 무시합니다.");
			return;
		}

		final Long chatRoomId = extractChatRoomIdFromDestination(destination);

		// TODO: 적은 유저 수라면 제일 단순하고 실용적일 수 있다. 예를 들어 1,000명 미만의 트래픽과 사내/폐쇄형 커뮤니티 등에 어울린다.
		//  하지만 추후 성능 상 문제가 발생할 수 있어 프로필 이미지만 Redis에 별도 캐싱을 해서 캐시 히트 시 RDB 접근이 없어 채팅방 입장이 더 빨라질 수 있다.
		//  그렇게 되면 getMemberByEmail를 없앨 수 있다.
		final Member member = getMemberByEmail(authUser.email());

		chatMessageCacheService.cacheChatRoom(chatRoomId);
		chatMessageCacheService.cacheMember(authUser.nickname());

		log.info("[✅ LOGGER] MEMBER {} IS JOIN CHATROOM", authUser.nickname());
		memberSessionRegistry.removeSessionByNickname(member.getNickname());
		memberSessionRegistry.addSession(member.getNickname(), sessionId, destination, member.getProfileImageUrl());
	}

	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent sessionDisconnectEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionDisconnectEvent);
		validateSessionIdPresent(sessionId);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionDisconnectEvent);
		if (authUser == null)
			return;

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
