package com.dart.api.application.chat.message;

import static com.dart.global.common.util.RedisConstant.*;
import static com.dart.global.error.model.ErrorCode.*;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dart.api.application.chat.message.batch.ChatMessageBatchService;
import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageBatchService chatMessageBatchService;
	private final ChatMessageRedisRepository chatMessageRedisRepository;

	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		validateChatRoomExistsInCache(chatRoomId);
		validateMemberExistsInCache(chatMessageCreateDto.sender());
		chatMessageRedisRepository.saveChatMessage(chatRoomId, chatMessageCreateDto);
	}

	@Scheduled(cron = "0 */10 * * * *")
	public void runBatchForAllRooms() {
		final long maxScore = System.currentTimeMillis();

		final Set<String> chatRoomKeys = chatMessageRedisRepository.findAllChatRoomKeysWithMessages();
		final List<Long> chatRoomIds = chatRoomKeys.stream()
			.map(key -> key.replace(REDIS_CHAT_MESSAGE_PREFIX, ""))
			.map(Long::parseLong)
			.toList();

		for (Long chatRoomId : chatRoomIds) {
			chatMessageBatchService.executeForChatRoom(chatRoomId, maxScore);
		}
	}

	public void validateChatRoomExistsInCache(Long chatRoomId) {
		if (!chatMessageRedisRepository.isChatRoomCached(chatRoomId)) {
			throw new NotFoundException(FAIL_CHAT_ROOM_NOT_FOUND);
		}
	}

	public void validateMemberExistsInCache(String nickname) {
		if (!chatMessageRedisRepository.isMemberCached(nickname)) {
			throw new NotFoundException(FAIL_MEMBER_NOT_FOUND);
		}
	}
}
