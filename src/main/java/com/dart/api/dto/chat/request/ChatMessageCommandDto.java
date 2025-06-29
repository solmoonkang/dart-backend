package com.dart.api.dto.chat.request;

import lombok.Builder;

@Builder
public record ChatMessageCommandDto(
	Long chatRoomId,

	ChatMessageCreateDto chatMessageCreateDto
) {
}
