package com.dart.api.dto.chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChatMessageSendDto(
	@NotBlank(message = "[❎ ERROR] 사용자 닉네임을 입력해주세요.")
	String sender,

	@NotBlank(message = "[❎ ERROR] 메시지 내용을 입력해주세요.")
	@Size(max = 50, message = "[❎ ERROR] 메시지 내용은 50자 이내여야 합니다.")
	String content,

	boolean isAuthor,

	String profileImageUrl
) {
}
