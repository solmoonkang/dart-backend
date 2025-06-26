package com.dart.api.dto.chat.request.cache;

import com.dart.api.domain.chat.entity.ChatRoom;

import lombok.Builder;

@Builder
public record ChatRoomCacheDto(
	Long chatRoomId,

	String title
) {

	public static ChatRoomCacheDto createChatRoomCacheDto(ChatRoom chatRoom) {
		return new ChatRoomCacheDto(chatRoom.getId(), chatRoom.getTitle());
	}
}
