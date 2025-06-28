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
	private final CacheKeyMemoryStore cacheKeyMemoryStore;

	public boolean isChatRoomNotCached(Long chatRoomId) {
		return !cacheKeyMemoryStore.isChatRoomCached(chatRoomId);
	}

	public boolean isMemberNotCached(String nickname) {
		return !cacheKeyMemoryStore.isMemberCached(nickname);
	}

	public void cacheChatRoom(ChatRoomCacheDto chatRoomCacheDto) {
		chatMessageRedisRepository.cacheChatRoom(chatRoomCacheDto);
		cacheKeyMemoryStore.cacheChatRoom(chatRoomCacheDto.chatRoomId());
	}

	public void cacheMember(MemberCacheDto memberCacheDto) {
		chatMessageRedisRepository.cacheMember(memberCacheDto);
		cacheKeyMemoryStore.cacheMember(memberCacheDto.nickname());
	}

	public MemberCacheDto getMemberCache(String nickname) {
		return chatMessageRedisRepository.getMemberCache(nickname);
	}
}
