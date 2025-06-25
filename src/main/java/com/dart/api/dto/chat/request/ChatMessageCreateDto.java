package com.dart.api.dto.chat.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChatMessageCreateDto(
	@NotBlank(message = "[❎ ERROR] 사용자 닉네임을 입력해주세요.")
	String sender,

	@NotBlank(message = "[❎ ERROR] 메시지 내용을 입력해주세요.")
	@Size(max = 50, message = "[❎ ERROR] 메시지 내용은 50자 이내여야 합니다.")
	String content,

	LocalDateTime createdAt,

	boolean isAuthor,

	String profileImageUrl
) {

	public static ChatMessageCreateDto createChatMessageCreateDto(String sender, String content,
		LocalDateTime createdAt, boolean isAuthor, String profileImageUrl) {

		return new ChatMessageCreateDto(sender, content, createdAt, isAuthor, profileImageUrl);
	}
}
