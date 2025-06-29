package com.dart.api.application.chat.message;

import java.time.LocalDateTime;

import com.dart.api.dto.chat.request.ChatMessageCommandDto;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMessageMapper {

	public static ChatMessageCreateDto toChatMessageCreateDto(ChatMessageSendDto chatMessageSendDto) {
		return new ChatMessageCreateDto(
			chatMessageSendDto.sender(),
			chatMessageSendDto.content(),
			LocalDateTime.now(),
			chatMessageSendDto.isAuthor(),
			chatMessageSendDto.profileImageUrl()
		);
	}

	public static ChatMessageCreateDto toChatMessageCreateDto(ChatMessageReadDto chatMessageReadDto) {
		return new ChatMessageCreateDto(
			chatMessageReadDto.sender(),
			chatMessageReadDto.content(),
			chatMessageReadDto.createdAt(),
			chatMessageReadDto.isAuthor(),
			chatMessageReadDto.profileImageUrl()
		);
	}

	public static ChatMessageCommandDto toChatMessageCommandDto(Long chatRoomId,
		ChatMessageCreateDto chatMessageCreateDto) {

		return new ChatMessageCommandDto(chatRoomId, chatMessageCreateDto);
	}
}
