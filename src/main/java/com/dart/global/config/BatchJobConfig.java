package com.dart.global.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.dto.chat.response.ChatMessageReadDto;

@Configuration
public class BatchJobConfig {

	private static final String JOB_NAME = "chatMessageSaveJob";
	private static final String STEP_NAME = "chatMessageSaveStep";

	@Bean
	public Job chatMessageSaveJob(JobRepository jobRepository, Step chatMessageSaveStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
			.incrementer(new RunIdIncrementer())
			.start(chatMessageSaveStep)
			.build();
	}

	@Bean
	public Step chatMessageSaveStep(
		JobRepository jobRepository,
		PlatformTransactionManager platformTransactionManager,
		ItemReader<ChatMessageReadDto> itemReader,
		ItemProcessor<ChatMessageReadDto, ChatMessage> itemProcessor,
		ItemWriter<ChatMessage> itemWriter) {

		return new StepBuilder(STEP_NAME, jobRepository)
			.<ChatMessageReadDto, ChatMessage>chunk(100, platformTransactionManager)
			.reader(itemReader)
			.processor(itemProcessor)
			.writer(itemWriter)
			.build();
	}
}
