package com.dart.api.dto.chat.request;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ChatMessageSendDto(
	Long memberId,

	Long chatRoomId,

	String content,

	LocalDateTime createdAt,

	boolean isAuthor,

	long expirySeconds
) {
}
