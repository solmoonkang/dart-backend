package com.dart.api.application.chat.command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import com.dart.api.dto.chat.request.ChatMessageCommandDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageCommandQueue {

	private static final int BLOCKING_QUEUE_MAX_SIZE = 5000;

	private final BlockingQueue<ChatMessageCommandDto> messageCommandQueue = new LinkedBlockingQueue<>(
		BLOCKING_QUEUE_MAX_SIZE
	);

	public void enqueue(ChatMessageCommandDto chatMessageCommandDto) {
		if (!messageCommandQueue.offer(chatMessageCommandDto)) {
			log.warn("[✅ LOGGER] 메시지 큐가 가득 차 메시지를 수용하지 못했습니다: {}", chatMessageCommandDto);
		}

		messageCommandQueue.offer(chatMessageCommandDto);
	}

	public ChatMessageCommandDto dequeue() throws InterruptedException {
		return messageCommandQueue.take();
	}

	public boolean isEmpty() {
		return messageCommandQueue.isEmpty();
	}
}
