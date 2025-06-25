package com.dart.api.application.chat.cache;

import org.springframework.stereotype.Service;

import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageCacheService {

	private final ChatMessageRedisRepository chatMessageRedisRepository;

	public void cacheChatRoom(Long chatRoomId) {
		chatMessageRedisRepository.cacheChatRoom(chatRoomId);
	}

	public void cacheMember(String nickname) {
		chatMessageRedisRepository.cacheMember(nickname);
	}
}
