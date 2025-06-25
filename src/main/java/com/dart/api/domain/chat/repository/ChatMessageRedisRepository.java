package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Repository;

import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.infrastructure.redis.ValueRedisRepository;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;
import com.dart.global.common.util.JsonConverter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRedisRepository {

	private final ZSetRedisRepository zSetRedisRepository;
	private final ValueRedisRepository valueRedisRepository;
	private final JsonConverter jsonConverter;

	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		zSetRedisRepository.addElementWithExpiry(
			generateChatMessageKey(chatRoomId),
			jsonConverter.toJson(chatMessageCreateDto),
			convertToScore(chatMessageCreateDto.createdAt()),
			CHAT_MESSAGE_EXPIRY_SECONDS);
	}

	public void cacheChatRoom(Long chatRoomId) {
		valueRedisRepository.saveValueWithExpiry(
			generateChatRoomCacheKey(chatRoomId), "TURE", CACHE_EXPIRY_HOURS.getSeconds());
	}

	public void cacheMember(String nickname) {
		valueRedisRepository.saveValueWithExpiry(
			generateMemberCacheKey(nickname), "TRUE", CACHE_EXPIRY_HOURS.getSeconds());
	}

	public boolean isChatRoomCached(Long chatRoomId) {
		return valueRedisRepository.isValueExists(generateChatRoomCacheKey(chatRoomId));
	}

	public boolean isMemberCached(String nickname) {
		return valueRedisRepository.isValueExists(generateMemberCacheKey(nickname));
	}

	private long convertToScore(LocalDateTime createdAt) {
		return createdAt.atZone(ZoneId.systemDefault())
			.toInstant()
			.toEpochMilli();
	}

	private String generateChatMessageKey(Long chatRoomId) {
		return REDIS_CHAT_MESSAGE_PREFIX + chatRoomId;
	}

	private String generateChatRoomCacheKey(Long chatRoomId) {
		return REDIS_CHAT_ROOM_CACHE_PREFIX + chatRoomId;
	}

	private String generateMemberCacheKey(String nickname) {
		return REDIS_MEMBER_NICKNAME_CACHE_PREFIX + nickname;
	}
}
