package com.dart.api.application.chat.command;

import org.springframework.stereotype.Component;

import com.dart.api.application.chat.message.ChatMessageMapper;
import com.dart.api.dto.chat.request.ChatMessageCommandDto;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.ChatMessageSendDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageCommandProducer {

	private final MessageCommandQueue messageCommandQueue;

	public void produce(Long chatRoomId, ChatMessageSendDto chatMessageSendDto) {
		final ChatMessageCreateDto chatMessageCreateDto = ChatMessageMapper.toChatMessageCreateDto(chatMessageSendDto);
		final ChatMessageCommandDto chatMessageCommandDto = ChatMessageMapper.toChatMessageCommandDto(
			chatRoomId, chatMessageCreateDto
		);

		messageCommandQueue.enqueue(chatMessageCommandDto);
	}
}
