package com.dart.global.common.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ObjectMapConverter {

	private final ObjectMapper objectMapper;

	public Map<String, String> toMap(Object dto) {
		Map<String, Object> objectFieldMap = objectMapper.convertValue(dto, new TypeReference<>() {});
		Map<String, String> stringMap = new LinkedHashMap<>();

		for (Map.Entry<String, Object> entry : objectFieldMap.entrySet()) {
			addStringEntry(stringMap, entry.getKey(), entry.getValue());
		}

		return stringMap;
	}

	private void addStringEntry(Map<String, String> target, String key, Object value) {
		if (value == null) {
			target.put(key, null);
			return;
		}

		target.put(key, value.toString());
	}

	public <T> T fromMap(Map<String, String> map, Class<T> clazz) {
		return objectMapper.convertValue(map, clazz);
	}
}
