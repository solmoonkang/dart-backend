package com.dart.api.infrastructure.batch.chat;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@StepScope
@RequiredArgsConstructor
public class ChatMessageRedisReader implements ItemReader<ChatMessageCreateDto> {

	private final ChatMessageRedisRepository chatMessageRedisRepository;
	private final Long chatRoomId;
	private final Long maxScore;

	private Iterator<ChatMessageCreateDto> cacheMessagesIterator;

	@PostConstruct
	public void logParams() {
		log.info("📌 ChatMessageRedisReader 생성됨: chatRoomId={}, maxScore={}", chatRoomId, maxScore);
	}

	@Override
	public ChatMessageCreateDto read() {
		if (cacheMessagesIterator == null) {
			List<ChatMessageCreateDto> messages =
				chatMessageRedisRepository.readAllMessagesForBatch(chatRoomId, maxScore);

			cacheMessagesIterator = messages.iterator();
		}

		if (cacheMessagesIterator.hasNext()) {
			return cacheMessagesIterator.next();
		}

		return null;
	}
}
