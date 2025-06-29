package com.dart.api.application.chat.command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.dto.chat.request.ChatMessageCommandDto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCommandConsumer implements DisposableBean {

	private final MessageCommandQueue messageCommandQueue;
	private final ChatMessageRedisRepository chatMessageRedisRepository;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor(singleThread -> {
		Thread thread = new Thread(singleThread);
		thread.setName("chat-message-consumer-thread");
		return thread;
	});

	private volatile boolean running = true;

	@PostConstruct
	public void startConsumerThread() {
		executorService.submit(() -> {
			while (running && !Thread.currentThread().isInterrupted()) {
				try {
					final ChatMessageCommandDto chatMessageCommandDto = messageCommandQueue.dequeue();
					chatMessageRedisRepository.saveChatMessage(
						chatMessageCommandDto.chatRoomId(), chatMessageCommandDto.chatMessageCreateDto()
					);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("[✅ LOGGER] 소비자 스레드 인터럽트가 발생했습니다.", e);
				} catch (Exception e) {
					log.error("[✅ LOGGER] 메시지 저장 중 예외가 발생했습니다.", e);
				}
			}
		});
	}

	@Override
	public void destroy() {
		log.info("[✅ LOGGER] 애플리케이션 종료 감지 - 소비자 스레드 종료를 시도합니다.");

		running = false;
		executorService.shutdownNow();

		try {
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				log.warn("[✅ LOGGER] 소비자 스레드 종료가 지연되었습니다.");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
