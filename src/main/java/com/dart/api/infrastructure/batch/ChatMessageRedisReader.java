package com.dart.api.infrastructure.batch;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dart.api.application.chat.ChatMessageReadService;
import com.dart.api.dto.chat.response.ChatMessageReadDto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ChatMessageRedisReader implements ItemReader<ChatMessageReadDto> {

	private final ChatMessageReadService chatMessageReadService;

	@Value("#{jobParameters['archiveStartTime']}")
	private String archiveStartTime;

	@Value("#{jobParameters['archiveEndTime']}")
	private String archiveEndTime;

	private Iterator<ChatMessageReadDto> chatMessageReadDtoIterator;

	@PostConstruct
	public void init() {
		List<ChatMessageReadDto> chatMessageReadDtos = chatMessageReadService.getMessagesInRange(
			LocalDateTime.parse(archiveStartTime), LocalDateTime.parse(archiveEndTime)
		);

		this.chatMessageReadDtoIterator = chatMessageReadDtos.iterator();
	}

	@Override
	public ChatMessageReadDto read() {
		if (chatMessageReadDtoIterator == null || !chatMessageReadDtoIterator.hasNext()) {
			return null;
		}

		return chatMessageReadDtoIterator.next();
	}
}
