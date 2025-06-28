package com.dart.api.infrastructure.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HashRedisRepository {

	private final StringRedisTemplate redisTemplate;

	public void saveHashEntries(String key, Map<String, String> data) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.putAll(key, data);
	}

	public void saveHashEntriesWithExpiry(String key, Map<String, String> data, long duration) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.putAll(key, data);
		redisTemplate.expire(key, Duration.ofSeconds(duration));
	}

	@Transactional(readOnly = true)
	public String getHashEntry(String key, String hashKey) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		return Boolean.TRUE.equals(values.hasKey(key, hashKey)) ? (String)values.get(key, hashKey) : "";
	}

	public Map<String, String> getAllHashEntries(String key) {
		HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
		Map<Object, Object> redisRawMap = hashOperations.entries(key);
		Map<String, String> cacheData = new LinkedHashMap<>();

		for (Map.Entry<Object, Object> entry : redisRawMap.entrySet()) {
			addHashEntry(cacheData, entry.getKey(), entry.getValue());
		}

		return cacheData;
	}

	private void addHashEntry(Map<String, String> target, Object rawKey, Object rawValue) {
		if (rawKey == null) {
			return;
		}

		String key = rawKey.toString();

		if (rawValue == null) {
			target.put(key, null);
			return;
		}

		target.put(key, rawValue.toString());
	}

	public void updateHashEntry(String key, String field, String value) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.put(key, field, value);
	}


	public void deleteHashEntry(String key, String hashKey) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.delete(key, hashKey);
	}

	public void deleteAllHashEntries(String key) {
		redisTemplate.delete(key);
	}
}
