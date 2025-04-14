package com.dart.api.infrastructure.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatMessageWriter implements ItemWriter<ChatMessage> {

	private final ChatMessageRepository chatMessageRepository;

	@Override
	public void write(Chunk<? extends ChatMessage> chatMessages) {
		chatMessageRepository.saveAll(chatMessages);
	}
}
