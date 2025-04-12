package com.dart.api.infrastructure.redis;

import static com.dart.global.common.util.GlobalConstant.*;
import static java.util.Objects.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ListRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void addElement(String key, Object value) {
		redisTemplate.opsForList().rightPush(requireNonNull(key), requireNonNull(value));
	}

	public void addElementWithExpiry(String key, Object value, long expire) {
		redisTemplate.opsForList().rightPush(requireNonNull(key), requireNonNull(value));

		if (expire > 0) {
			redisTemplate.expire(key, Duration.ofSeconds(expire));
		}
	}

	public List<Object> getRange(String key, long start, long end) {
		return redisTemplate.opsForList().range(requireNonNull(key), start, end);
	}

	public List<Long> getActiveChatRoomIds(String keyPrefix) {
		List<Long> chatRoomIds = new ArrayList<>();
		ScanOptions scanOptions = ScanOptions.scanOptions()
			.match(keyPrefix + "*")
			.count(1000)
			.build();

		RedisConnection redisConnection = redisTemplate.getRequiredConnectionFactory().getConnection();

		try (Cursor<byte[]> cursor = redisConnection.keyCommands().scan(scanOptions)) {
			while (cursor.hasNext()) {
				String key = redisTemplate.getStringSerializer().deserialize(cursor.next());
				String chatRoomId = requireNonNull(key).replace(keyPrefix, BLANK);
				chatRoomIds.add(Long.parseLong(chatRoomId));
			}
		}

		return chatRoomIds;
	}

	public void removeElement(String key, Object value) {
		redisTemplate.opsForList().remove(requireNonNull(key), 1, requireNonNull(value));
	}

	public void removeAllElements(String key, Object value) {
		redisTemplate.opsForList().remove(requireNonNull(key), 0, requireNonNull(value));
	}

	public void deleteAllElements(String key) {
		redisTemplate.delete(requireNonNull(key));
	}
}
