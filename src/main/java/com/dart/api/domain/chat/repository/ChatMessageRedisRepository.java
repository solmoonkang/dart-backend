package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.cache.ChatRoomCacheDto;
import com.dart.api.dto.chat.request.cache.MemberCacheDto;
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

	public void cacheChatRoom(ChatRoomCacheDto chatRoomCacheDto) {
		valueRedisRepository.saveValueWithExpiry(
			generateChatRoomCacheKey(chatRoomCacheDto.chatRoomId()),
			jsonConverter.toJson(chatRoomCacheDto),
			CACHE_EXPIRY_HOURS.getSeconds());
	}

	public void cacheMember(MemberCacheDto memberCacheDto) {
		valueRedisRepository.saveValueWithExpiry(
			generateMemberCacheKey(memberCacheDto.nickname()),
			jsonConverter.toJson(memberCacheDto),
			CACHE_EXPIRY_HOURS.getSeconds());
	}

	public List<ChatMessageCreateDto> readAllMessagesForBatch(Long chatRoomId, long maxScore) {
		Set<Object> messages = zSetRedisRepository.getElementByScoreLessThanEqual(
			generateChatMessageKey(chatRoomId), maxScore);

		return messages.stream()
			.map(element -> jsonConverter.fromJson((String)element, ChatMessageCreateDto.class))
			.toList();
	}

	public Set<String> findAllChatRoomKeysWithMessages() {
		return valueRedisRepository.getKeysByPatten(REDIS_CHAT_MESSAGE_PREFIX + "*");
	}

	public ChatRoomCacheDto getChatRoomCache(Long chatRoomId) {
		return jsonConverter.fromJson(
			valueRedisRepository.getValue(generateChatRoomCacheKey(chatRoomId)), ChatRoomCacheDto.class);
	}

	public MemberCacheDto getMemberCache(String nickname) {
		return jsonConverter.fromJson(
			valueRedisRepository.getValue(generateMemberCacheKey(nickname)), MemberCacheDto.class);
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
