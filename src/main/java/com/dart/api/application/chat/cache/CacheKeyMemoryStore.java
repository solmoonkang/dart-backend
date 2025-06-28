package com.dart.api.application.chat.cache;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class CacheKeyMemoryStore {

	private final Set<String> cachedChatRoomKeys = ConcurrentHashMap.newKeySet();
	private final Set<String> cachedMemberKeys = ConcurrentHashMap.newKeySet();

	public boolean isChatRoomCached(Long chatRoomId) {
		return cachedChatRoomKeys.contains(REDIS_CHAT_ROOM_CACHE_PREFIX + chatRoomId);
	}

	public boolean isMemberCached(String nickname) {
		return cachedMemberKeys.contains(REDIS_MEMBER_CACHE_PREFIX + nickname);
	}

	public void cacheChatRoom(Long chatRoomId) {
		cachedChatRoomKeys.add(REDIS_CHAT_ROOM_CACHE_PREFIX + chatRoomId);
	}

	public void cacheMember(String nickname) {
		cachedMemberKeys.add(REDIS_MEMBER_CACHE_PREFIX + nickname);
	}

	public void clearAll() {
		cachedChatRoomKeys.clear();
		cachedMemberKeys.clear();
	}
}
