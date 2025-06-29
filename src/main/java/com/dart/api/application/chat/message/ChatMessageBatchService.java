package com.dart.api.application.chat.message;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageBatchService {

	private final JobLauncher jobLauncher;
	private final Job chatMessageBatchJob;
	private final ChatMessageReadService chatMessageReadService;

	@Scheduled(cron = "0 */1 * * * *") // 매 1분마다 실행
	// @Scheduled(cron = "0 */15 * * * *")	// 매 10분마다 실행
	public void runBatchForAllRooms() {
		final Set<String> chatRoomKeys = chatMessageReadService.findAllChatRoomKeysWithMessages();
		final List<Long> chatRoomIds = chatRoomKeys.stream()
			.map(key -> key.replace(REDIS_CHAT_MESSAGE_STORE_PREFIX, ""))
			.map(Long::parseLong)
			.toList();

		for (Long chatRoomId : chatRoomIds) {
			executeBatch(buildJobParameters(chatRoomId));
		}
	}

	private void executeBatch(JobParameters jobParameters) {
		try {
			jobLauncher.run(chatMessageBatchJob, jobParameters);
		} catch (Exception e) {
			log.error("[✅ LOGGER] 채팅방 배치 실패: {}", jobParameters, e);
		}
	}

	private JobParameters buildJobParameters(Long chatRoomId) {
		final long maxScore = System.currentTimeMillis();
		log.info("▶️ jobParam chatRoomId={}, maxScore={}", chatRoomId, maxScore);

		return new JobParametersBuilder()
			.addLong("chatRoomId", chatRoomId)
			.addLong("maxScore", maxScore)
			.addString("UUID", UUID.randomUUID().toString())
			.toJobParameters();
	}
}
