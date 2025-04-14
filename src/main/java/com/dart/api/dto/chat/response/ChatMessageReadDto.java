package com.dart.api.dto.chat.response;

import java.time.LocalDateTime;

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

	public static ChatMessageReadDto fromChatMessageCreateDto(
		Long chatRoomId,
		ChatMessageCreateDto chatMessageCreateDto) {

		return ChatMessageReadDto.builder()
			.chatRoomId(chatRoomId)
			.sender(chatMessageCreateDto.sender())
			.content(chatMessageCreateDto.content())
			.createdAt(chatMessageCreateDto.createdAt())
			.isAuthor(chatMessageCreateDto.isAuthor())
			.profileImageUrl(chatMessageCreateDto.profileImageUrl())
			.build();
	}
}
