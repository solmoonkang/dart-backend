package com.dart.api.application.chat.message;

import org.springframework.stereotype.Service;

import com.dart.api.dto.chat.request.ChatMessageSendDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageWriteService {

	private final MessageCommandProducer messageCommandProducer;

	public void saveChatMessage(Long chatRoomId, ChatMessageSendDto chatMessageSendDto) {
		messageCommandProducer.produce(chatRoomId, chatMessageSendDto);
	}
}
