package com.dart.api.infrastructure.batch.chat;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ChatMessageRedisReader implements ItemReader<ChatMessageCreateDto> {

	private final ChatMessageRedisRepository chatMessageRedisRepository;

	@Value("#{jobParameters['chatRoomId']}")
	private final Long chatRoomId;

	@Value("#{jobParameters['maxScore']}")
	private final long maxScore;

	private Iterator<ChatMessageCreateDto> cacheMessagesIterator;

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
