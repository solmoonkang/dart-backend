package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.infrastructure.batch.ChatMessageSerializer;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRedisRepository {

	private final ZSetRedisRepository zSetRedisRepository;
	private final ChatMessageSerializer chatMessageSerializer;

	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		LocalDateTime createdAt = Optional.ofNullable(chatMessageCreateDto.createdAt())
			.orElse(LocalDateTime.now());

		ChatMessageReadDto chatMessageReadDto = ChatMessageReadDto.fromChatMessageCreateDto(
			chatRoomId, chatMessageCreateDto);
		String messageJSON = chatMessageSerializer.serializeMessage(chatMessageReadDto);

		double score = toEpochSecondScore(createdAt);

		zSetRedisRepository.addElementWithExpiry(
			generateChatMessageKey(chatRoomId), messageJSON, score, CHAT_MESSAGE_EXPIRY_SECONDS);
	}

	public List<Long> getActiveChatRoomIds() {
		Set<String> keys = zSetRedisRepository.getKeysByPattern(REDIS_CHAT_MESSAGE_PREFIX + "*");

		if (keys == null || keys.isEmpty()) {
			return Collections.emptyList();
		}

		return keys.stream()
			.map(key -> key.replace(REDIS_CHAT_MESSAGE_PREFIX, ""))
			.map(Long::valueOf)
			.collect(Collectors.toList());
	}

	public List<ChatMessageReadDto> getMessagesAscFromChatRoom(
		Long chatRoomId,
		LocalDateTime archiveStartTime,
		LocalDateTime archiveEndTime) {

		double archiveStartTimeScore = toEpochSecondScore(archiveStartTime);
		double toScore = toEpochSecondScore(archiveEndTime);

		Set<String> messageJSONs = zSetRedisRepository.getRangeByScoreAsStringSet(
			generateChatMessageKey(chatRoomId), archiveStartTimeScore, toScore);

		if (messageJSONs == null || messageJSONs.isEmpty())
			return Collections.emptyList();

		return messageJSONs.stream()
			.map(chatMessageSerializer::deserializeMessage)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	public void deleteChatMessages(Long chatRoomId) {
		zSetRedisRepository.deleteAllElements(generateChatMessageKey(chatRoomId));
	}

	private double toEpochSecondScore(LocalDateTime createdAt) {
		return createdAt.atZone(ZoneId.systemDefault()).toEpochSecond();
	}

	private String generateChatMessageKey(Long chatRoomId) {
		return REDIS_CHAT_MESSAGE_PREFIX + chatRoomId;
	}
}
