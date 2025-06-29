package com.dart.global.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.infrastructure.batch.chat.ChatMessageRedisReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	private static final String CHAT_MESSAGE_BATCH_JOB_NAME = "chatMessageBatchJob";
	private static final String CHAT_MESSAGE_STEP_NAME = "chatMessageStep";

	private final ChatMessageRedisRepository chatMessageRedisRepository;

	@Bean
	@StepScope
	public ChatMessageRedisReader chatMessageRedisReader(
		@Value("#{jobParameters['chatRoomId']}") Long chatRoomId,
		@Value("#{jobParameters['maxScore']}") Long maxScore) {

		log.info("✅ Reader Bean 생성됨: chatRoomId={}, maxScore={}", chatRoomId, maxScore);
		return new ChatMessageRedisReader(chatMessageRedisRepository, chatRoomId, maxScore);
	}

	@Bean
	public Job chatMessageBatchJob(JobRepository jobRepository, Step chatMessageStep) {
		return new JobBuilder(CHAT_MESSAGE_BATCH_JOB_NAME, jobRepository)
			.start(chatMessageStep)
			.build();
	}

	@Bean
	public Step chatMessageStep(
		JobRepository jobRepository,
		PlatformTransactionManager platformTransactionManager,
		ItemProcessor<ChatMessageCreateDto, ChatMessage> chatMessageBatchProcessor,
		ItemWriter<ChatMessage> chatMessageWriter) {

		return new StepBuilder(CHAT_MESSAGE_STEP_NAME, jobRepository)
			.<ChatMessageCreateDto, ChatMessage>chunk(100, platformTransactionManager)
			.reader(chatMessageRedisReader(null, null))
			.processor(chatMessageBatchProcessor)
			.writer(chatMessageWriter)
			.build();
	}
}
