package com.dart.api.infrastructure.batch.chat;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessageWriter implements ItemWriter<ChatMessage> {

	private final ChatMessageRepository chatMessageRepository;

	@Override
	public void write(Chunk<? extends ChatMessage> items) {
		chatMessageRepository.saveAll(items);
	}
}
