package com.dart.api.application.chat.message;

import org.springframework.stereotype.Service;

import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageWriteService {

	private final ChatMessageRedisRepository chatMessageRedisRepository;

	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		chatMessageRedisRepository.saveChatMessage(chatRoomId, chatMessageCreateDto);
	}
}
