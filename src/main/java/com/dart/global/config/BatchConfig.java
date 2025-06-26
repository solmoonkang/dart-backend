package com.dart.global.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.infrastructure.batch.chat.ChatMessageBatchProcessor;
import com.dart.api.infrastructure.batch.chat.ChatMessageRedisReader;
import com.dart.api.infrastructure.batch.chat.ChatMessageWriter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	private static final String CHAT_MESSAGE_BATCH_JOB_NAME = "chatMessageBatchJob";
	private static final String CHAT_MESSAGE_STEP_NAME = "chatMessageStep";

	private final ChatMessageRedisReader chatMessageRedisReader;
	private final ChatMessageBatchProcessor chatMessageBatchProcessor;
	private final ChatMessageWriter chatMessageWriter;

	@Bean
	public Job chatMessageBatchJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new JobBuilder(CHAT_MESSAGE_BATCH_JOB_NAME, jobRepository)
			.start(chatMessageStep(jobRepository, platformTransactionManager))
			.build();
	}

	@Bean
	public Step chatMessageStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder(CHAT_MESSAGE_STEP_NAME, jobRepository)
			.<ChatMessageCreateDto, ChatMessage>chunk(100, platformTransactionManager)
			.reader(chatMessageRedisReader)
			.processor(chatMessageBatchProcessor)
			.writer(chatMessageWriter)
			.build();
	}
}
