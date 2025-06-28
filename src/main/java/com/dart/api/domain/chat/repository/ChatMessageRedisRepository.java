package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.cache.ChatRoomCacheDto;
import com.dart.api.dto.chat.request.cache.MemberCacheDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.infrastructure.redis.HashRedisRepository;
import com.dart.api.infrastructure.redis.ListRedisRepository;
import com.dart.api.infrastructure.redis.ValueRedisRepository;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;
import com.dart.global.common.util.MessagePackConverter;
import com.dart.global.common.util.ObjectMapConverter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRedisRepository {

	private final ZSetRedisRepository zSetRedisRepository;
	private final ListRedisRepository listRedisRepository;
	private final HashRedisRepository hashRedisRepository;
	private final ValueRedisRepository valueRedisRepository;
	private final MessagePackConverter messagePackConverter;
	private final ObjectMapConverter objectMapConverter;

	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		byte[] packedMessage = messagePackConverter.serialize(chatMessageCreateDto);

		zSetRedisRepository.addElementWithExpiry(
			generateChatMessageReadKey(chatRoomId),
			packedMessage,
			convertToScore(chatMessageCreateDto.createdAt()),
			CHAT_MESSAGE_EXPIRY_SECONDS
		);

		listRedisRepository.addElementWithExpiry(
			generateChatMessageStoreKey(chatRoomId),
			packedMessage,
			CHAT_MESSAGE_EXPIRY_SECONDS
		);
	}

	public void cacheChatRoom(ChatRoomCacheDto chatRoomCacheDto) {
		Map<String, String> cacheData = objectMapConverter.toMap(chatRoomCacheDto);

		hashRedisRepository.saveHashEntriesWithExpiry(
			generateChatRoomCacheKey(chatRoomCacheDto.chatRoomId()),
			cacheData,
			CACHE_EXPIRY_HOURS.getSeconds()
		);
	}

	public void cacheMember(MemberCacheDto memberCacheDto) {
		Map<String, String> cacheData = objectMapConverter.toMap(memberCacheDto);

		hashRedisRepository.saveHashEntriesWithExpiry(
			generateMemberCacheKey(memberCacheDto.nickname()),
			cacheData,
			CACHE_EXPIRY_HOURS.getSeconds()
		);
	}

	public List<ChatMessageReadDto> readAllMessages(Long chatRoomId, int page, int size) {
		int start = page * size;

		Set<Object> messages = zSetRedisRepository.getRecentRange(generateChatMessageReadKey(chatRoomId), start, size);

		return messages.stream()
			.map(o -> (byte[])o)
			.map(bytes -> messagePackConverter.deserialize(bytes, ChatMessageReadDto.class))
			.toList();
	}

	public List<ChatMessageCreateDto> readAllMessagesForBatch(Long chatRoomId, long maxScore) {
		List<Object> messages = listRedisRepository.getRange(generateChatMessageStoreKey(chatRoomId), 0, -1);

		return messages.stream()
			.map(o -> (byte[])o)
			.map(bytes -> messagePackConverter.deserialize(bytes, ChatMessageCreateDto.class))
			.filter(createDto -> convertToScore(createDto.createdAt()) <= maxScore)
			.toList();
	}

	public Set<String> findAllChatRoomKeysWithMessages() {
		return valueRedisRepository.getKeysByPatten(REDIS_CHAT_MESSAGE_STORE_PREFIX + "*");
	}

	public ChatRoomCacheDto getChatRoomCache(Long chatRoomId) {
		Map<String, String> cacheData = hashRedisRepository.getAllHashEntries(generateChatRoomCacheKey(chatRoomId));
		return objectMapConverter.fromMap(cacheData, ChatRoomCacheDto.class);
	}

	public MemberCacheDto getMemberCache(String nickname) {
		Map<String, String> cacheData = hashRedisRepository.getAllHashEntries(generateMemberCacheKey(nickname));
		return objectMapConverter.fromMap(cacheData, MemberCacheDto.class);
	}

	public void deleteChatMessages(Long chatRoomId) {
		zSetRedisRepository.deleteAllElements(generateChatMessageStoreKey(chatRoomId));
	}

	private long convertToScore(LocalDateTime createdAt) {
		return createdAt.atZone(ZoneId.systemDefault())
			.toInstant()
			.toEpochMilli();
	}

	private String generateChatMessageStoreKey(Long chatRoomId) {
		return REDIS_CHAT_MESSAGE_STORE_PREFIX + chatRoomId;
	}

	private String generateChatMessageReadKey(Long chatRoomId) {
		return REDIS_CHAT_MESSAGE_READ_PREFIX + chatRoomId;
	}

	private String generateChatRoomCacheKey(Long chatRoomId) {
		return REDIS_CHAT_ROOM_CACHE_PREFIX + chatRoomId;
	}

	private String generateMemberCacheKey(String nickname) {
		return REDIS_MEMBER_CACHE_PREFIX + nickname;
	}
}
