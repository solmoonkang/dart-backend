package com.dart.global.config;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dart.api.application.chat.ChatMessageReadService;
import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.infrastructure.batch.ChatMessageProcessor;
import com.dart.api.infrastructure.batch.ChatMessageRedisReader;
import com.dart.api.infrastructure.batch.ChatMessageWriter;

@Configuration
public class ChatMessageStepConfig {

	@Bean
	@StepScope
	public ItemReader<ChatMessageReadDto> chatMessageReader(ChatMessageReadService chatMessageReadService) {
		return new ChatMessageRedisReader(chatMessageReadService);
	}

	@Bean
	@StepScope
	public ItemProcessor<ChatMessageReadDto, ChatMessage> chatMessageProcessor(
		MemberRepository memberRepository,
		ChatRoomRepository chatRoomRepository) {

		return new ChatMessageProcessor(memberRepository, chatRoomRepository);
	}

	@Bean
	@StepScope
	public ItemWriter<ChatMessage> chatMessageWriter(ChatMessageRepository chatMessageRepository) {
		return new ChatMessageWriter(chatMessageRepository);
	}
}
