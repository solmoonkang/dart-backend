package com.dart.api.dto.chat.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChatMessageCreateDto(
	String sender,

	@Size(max = 50, message = "[❎ ERROR] 메시지 내용은 50자 이내여야 합니다.")
	String content,

	LocalDateTime createdAt,

	boolean isAuthor,

	String profileImageUrl
) {
}
