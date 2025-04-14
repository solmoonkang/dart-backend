package com.dart.api.infrastructure.batch;

import org.springframework.stereotype.Component;

import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatMessageSerializer {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public String serializeMessage(ChatMessageReadDto chatMessageReadDto) {
		try {
			return objectMapper.writeValueAsString(chatMessageReadDto);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("[✅ LOGGER] JSON 메시지 직렬화 실패", e);
		}
	}

	public ChatMessageReadDto deserializeMessage(String messageJSON) {
		try {
			return objectMapper.readValue(messageJSON, ChatMessageReadDto.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("[✅ LOGGER] JSON 메시지 역직렬화 실패", e);
		}
	}
}
