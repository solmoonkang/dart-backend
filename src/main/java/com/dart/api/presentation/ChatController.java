package com.dart.api.presentation;

import static com.dart.global.common.util.ChatConstant.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.chat.ChatMessageReadService;
import com.dart.api.application.chat.ChatMessageService;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.chat.response.MemberSessionDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.websocket.MemberSessionRegistry;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessageSendingOperations simpMessageSendingOperations;
	private final MemberSessionRegistry memberSessionRegistry;
	private final ChatMessageService chatMessageService;
	private final ChatMessageReadService chatMessageReadService;

	@MessageMapping(value = "/ws/{chat-room-id}/chat-messages/mysql")
	public void saveAndSendChatMessageToMySQL(
		@DestinationVariable("chat-room-id") Long chatRoomId,
		@Payload @Validated ChatMessageCreateDto chatMessageCreateDto
	) {
		chatMessageService.saveChatMessageToMySQL(chatRoomId, chatMessageCreateDto);
		simpMessageSendingOperations.convertAndSend(TOPIC_PREFIX + chatRoomId, chatMessageCreateDto);
	}

	@MessageMapping(value = "/ws/{chat-room-id}/chat-messages/redis")
	public void saveAndSendChatMessageToRedis(
		@DestinationVariable("chat-room-id") Long chatRoomId,
		@Payload @Validated ChatMessageCreateDto chatMessageCreateDto
	) {
		chatMessageService.saveChatMessageToRedis(chatRoomId, chatMessageCreateDto);
		simpMessageSendingOperations.convertAndSend(TOPIC_PREFIX + chatRoomId, chatMessageCreateDto);
	}

	@GetMapping("/api/{chat-room-id}/chat-messages")
	public ResponseEntity<PageResponse<ChatMessageReadDto>> getChatMessageList(
		@PathVariable("chat-room-id") Long chatRoomId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "99") int size
	) {
		return ResponseEntity.ok(chatMessageReadService.getChatMessageList(chatRoomId, page, size));
	}

	@GetMapping("/api/chat-rooms/{chat-room-id}/members")
	public ResponseEntity<List<MemberSessionDto>> getLoggedInVisitors(@PathVariable("chat-room-id") Long chatRoomId) {
		return ResponseEntity.ok(memberSessionRegistry.getMembersInChatRoom(TOPIC_PREFIX + chatRoomId));
	}
}
