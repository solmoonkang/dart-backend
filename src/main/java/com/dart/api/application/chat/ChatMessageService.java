package com.dart.api.application.chat;

import static com.dart.global.error.model.ErrorCode.*;

import org.springframework.stereotype.Service;

import com.dart.api.application.chat.batch.ChatCacheService;
import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRedisRepository chatMessageRedisRepository;
	private final ChatCacheService chatCacheService;

	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		validateChatRoomExistsInCache(chatRoomId);
		validateMemberExistsInCache(chatMessageCreateDto.sender());
		chatMessageRedisRepository.saveChatMessage(chatRoomId, chatMessageCreateDto);
	}

	private void validateChatRoomExistsInCache(Long chatRoomId) {
		if (!chatCacheService.isChatRoomCached(chatRoomId)) {
			throw new NotFoundException(FAIL_CHAT_ROOM_NOT_FOUND);
		}
	}

	private void validateMemberExistsInCache(String nickname) {
		if (!chatCacheService.isMemberCached(nickname)) {
			throw new NotFoundException(FAIL_MEMBER_NOT_FOUND);
		}
	}
}
