package com.dart.api.infrastructure.redis;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ValueRedisRepository {

	private final StringRedisTemplate redisTemplate;

	public void saveValue(String key, String value) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		valueOperations.set(key, value);
	}

	public void saveValueWithExpiry(String key, String value, long duration) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		Duration expireDuration = Duration.ofSeconds(duration);
		valueOperations.set(key, value, expireDuration);
	}

	public Long increment(String key, long delta) {
		return redisTemplate
			.opsForValue()
			.increment(key, delta);
	}

	public String getValue(String key) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		return valueOperations.get(key);
	}

	public Set<String> getKeysByPatten(String pattern) {
		return redisTemplate.keys(pattern);
	}

	public void deleteValue(String key) {
		redisTemplate.delete(key);
	}

	public boolean isValueExists(String key) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
}
