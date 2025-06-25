package com.dart.global.common.util;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JsonConverter {

	private final ObjectMapper objectMapper;

	public String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("[❎ ERROR] 직렬화에 실패했습니다.", e);
		}
	}

	public <T> T fromJson(String JSON, Class<T> type) {
		try {
			return objectMapper.readValue(JSON, type);
		} catch (IOException e) {
			throw new RuntimeException("[❎ ERROR] 역직렬화에 실패했습니다.", e);
		}
	}
}
