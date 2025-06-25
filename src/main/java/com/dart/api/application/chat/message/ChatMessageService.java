package com.dart.api.application.chat.message;

import static com.dart.global.error.model.ErrorCode.*;

import org.springframework.stereotype.Service;

import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRedisRepository chatMessageRedisRepository;

	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		validateChatRoomExistsInCache(chatRoomId);
		validateMemberExistsInCache(chatMessageCreateDto.sender());
		chatMessageRedisRepository.saveChatMessage(chatRoomId, chatMessageCreateDto);
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
