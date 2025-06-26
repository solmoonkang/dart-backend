package com.dart.api.application.chat.cache;

import org.springframework.stereotype.Service;

import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.cache.ChatRoomCacheDto;
import com.dart.api.dto.chat.request.cache.MemberCacheDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageCacheService {

	private final ChatMessageRedisRepository chatMessageRedisRepository;

	public void cacheChatRoom(ChatRoomCacheDto chatRoomCacheDto) {
		chatMessageRedisRepository.cacheChatRoom(chatRoomCacheDto);
	}

	public void cacheMember(MemberCacheDto memberCacheDto) {
		chatMessageRedisRepository.cacheMember(memberCacheDto);
	}
}
