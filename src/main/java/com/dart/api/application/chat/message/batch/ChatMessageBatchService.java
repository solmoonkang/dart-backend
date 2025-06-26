package com.dart.api.application.chat.message.batch;

import java.util.UUID;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageBatchService {

	private final JobLauncher jobLauncher;
	private final Job chatMessageBatchJob;

	public void executeForChatRoom(Long chatRoomId, long maxScore) {
		JobParameters jobParameters = new JobParametersBuilder()
			.addLong("chatRoomId", chatRoomId)
			.addLong("maxScore", maxScore)
			.addString("UUID", UUID.randomUUID().toString())
			.toJobParameters();

		try {
			jobLauncher.run(chatMessageBatchJob, jobParameters);
		} catch (Exception e) {
			log.error("[✅ LOGGER] {}번 채팅방 배치에 실패했습니다.", chatRoomId, e);
		}
	}
}
