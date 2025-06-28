package com.dart.global.common.util;

import java.io.IOException;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MessagePackConverter {

	private final ObjectMapper objectMapper;

	public MessagePackConverter() {
		this.objectMapper = new ObjectMapper(new MessagePackFactory());
	}

	public <T> byte[] serialize(T object) {
		try {
			return objectMapper.writeValueAsBytes(object);
		} catch (IOException e) {
			throw new RuntimeException("[❎ ERROR] MessagePack 직렬화에 실패했습니다.", e);
		}
	}

	public <T> T deserialize(byte[] data, Class<T> type) {
		try {
			return objectMapper.readValue(data, type);
		} catch (IOException e) {
			throw new RuntimeException("[❎ ERROR] MessagePack 역직렬화에 실패했습니다.", e);
		}
	}
}
