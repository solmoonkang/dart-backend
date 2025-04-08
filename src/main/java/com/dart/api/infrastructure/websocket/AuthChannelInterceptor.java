package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class AuthChannelInterceptor implements ChannelInterceptor {

	private final JwtProviderService jwtProviderService;

	@Value("${spring.profiles.active:}")
	private String activeProfile;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (isConnectCommand(accessor)) {

			// ✅ 테스트 환경에서는 인증 우회
			if ("local".equals(activeProfile) || "test".equals(activeProfile)) {
				log.warn("[✅ LOGGER] WebSocket 인증 우회: profile = {}", activeProfile);
				return message;
			}

			// 🔐 실제 인증 로직
			String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				log.warn("[✅ LOGGER] INVALID OR MISSING AUTHORIZATION HEADER");
				throw new NotFoundException(ErrorCode.FAIL_TOKEN_NOT_FOUND);
			}

			String accessToken = extractToken(authorizationHeader);
			validateAccessToken(accessToken);

			AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);

			accessor.getSessionAttributes().computeIfAbsent(CHAT_SESSION_USER, key -> new HashMap<>());
			accessor.getSessionAttributes().put(CHAT_SESSION_USER, authUser);
		}

		return message;
	}

	private boolean isConnectCommand(StompHeaderAccessor accessor) {
		return StompCommand.CONNECT.equals(accessor.getCommand());
	}

	private String extractToken(String header) {
		return header.substring("Bearer ".length()).trim();
	}

	private void validateAccessToken(String token) {
		if (token.isEmpty()) {
			log.warn("[✅ LOGGER] TOKEN IS EMPTY");
			throw new NotFoundException(ErrorCode.FAIL_TOKEN_NOT_FOUND);
		}

		if (!jwtProviderService.isUsable(token)) {
			log.warn("[✅ LOGGER] JWT TOKEN IS NOT USABLE");
			throw new NotFoundException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}
}
