package com.dart.api.dto.chat.response;

import java.time.LocalDateTime;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;

import lombok.Builder;

@Builder
public record ChatMessageReadDto(
	Long chatRoomId,

	String sender,

	String content,

	LocalDateTime createdAt,

	boolean isAuthor,

	String profileImageUrl
) {

	public static ChatMessageReadDto createMessageReadDto(ChatMessage chatMessage) {
		return new ChatMessageReadDto(
			chatMessage.getChatRoom().getId(),
			chatMessage.getMember().getNickname(),
			chatMessage.getContent(),
			chatMessage.getCreatedAt(),
			chatMessage.isAuthor(),
			chatMessage.getMember().getProfileImageUrl()
		);
	}
}
