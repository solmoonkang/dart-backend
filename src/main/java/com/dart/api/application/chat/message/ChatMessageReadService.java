package com.dart.api.application.chat.message;

import static com.dart.global.error.model.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.application.chat.cache.ChatMessageCacheService;
import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageReadService {

	private final ChatMessageRedisRepository chatMessageRedisRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatMessageCacheService chatMessageCacheService;

	@Transactional(readOnly = true)
	public PageResponse<ChatMessageReadDto> findChatMessages(Long chatRoomId, int page, int size) {
		final List<ChatMessageReadDto> cachedMessages =
			chatMessageRedisRepository.readAllMessages(chatRoomId, page, size);

		if (cachedMessages.size() >= size) {
			return toPageResponse(cachedMessages, page, size);
		}

		final List<ChatMessageReadDto> RDBMessages =
			findChatMessageDtosFromRDB(chatRoomId, page, size, cachedMessages.size());
		cacheChatMessages(chatRoomId, RDBMessages);

		final List<ChatMessageReadDto> combinedMessages = new ArrayList<>(cachedMessages);
		combinedMessages.addAll(RDBMessages);

		return toPageResponse(combinedMessages, page, size);
	}

	private List<ChatMessageReadDto> findChatMessageDtosFromRDB(Long chatRoomId, int page, int size, int cachedSize) {
		int remaining = size - cachedSize;
		int offset = page * size + cachedSize;

		final Pageable pageable = PageRequest.of(offset / size, remaining, Sort.by(Sort.Direction.DESC, "createdAt"));
		final List<ChatMessage> chatMessages =
			chatMessageRepository.findMessagesByChatRoomWithOffset(chatRoomId, pageable);

		return chatMessages.stream()
			.map(ChatMessageReadDto::createMessageReadDto)
			.toList();
	}

	private void cacheChatMessages(Long chatRoomId, List<ChatMessageReadDto> messages) {
		messages.stream()
			.map(ChatMessageCreateDto::createMessageCreateDto)
			.forEach(dto -> chatMessageRedisRepository.saveChatMessage(chatRoomId, dto));
	}

	private PageResponse<ChatMessageReadDto> toPageResponse(List<ChatMessageReadDto> chatMessages, int page, int size) {
		final boolean isDone = chatMessages.size() < size;
		final PageInfo pageInfo = new PageInfo(page, isDone);

		return new PageResponse<>(chatMessages, pageInfo);
	}

	public Set<String> findAllChatRoomKeysWithMessages() {
		return chatMessageRedisRepository.findAllChatRoomKeysWithMessages();
	}

	public void validateChatRoomExistsInCache(Long chatRoomId) {
		if (chatMessageCacheService.isChatRoomNotCached(chatRoomId)) {
			throw new NotFoundException(FAIL_CHAT_ROOM_NOT_FOUND);
		}
	}

	public void validateMemberExistsInCache(String nickname) {
		if (chatMessageCacheService.isMemberNotCached(nickname)) {
			throw new NotFoundException(FAIL_MEMBER_NOT_FOUND);
		}
	}
}
