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

import com.dart.api.application.chat.message.ChatMessageService;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.chat.response.MemberSessionDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.websocket.MemberSessionRegistry;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

	private final SimpMessageSendingOperations simpMessageSendingOperations;
	private final MemberSessionRegistry memberSessionRegistry;
	private final ChatMessageService chatMessageService;

	@MessageMapping(value = "/ws/{chatRoomId}/chat-messages")
	public void saveAndSendChatMessageToMySQL(
		@DestinationVariable("chatRoomId") Long chatRoomId,
		@Payload @Validated ChatMessageSendDto chatMessageSendDto) {

		chatMessageService.saveChatMessage(chatRoomId, chatMessageSendDto);
		simpMessageSendingOperations.convertAndSend(TOPIC_PREFIX + chatRoomId, chatMessageSendDto);
	}

	@GetMapping("/api/{chatRoomId}/chat-messages")
	public ResponseEntity<PageResponse<ChatMessageReadDto>> getChatMessageList(
		@PathVariable("chatRoomId") Long chatRoomId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "100") int size) {

		return ResponseEntity.ok(chatMessageService.getAllChatMessages(chatRoomId, page, size));
	}

	@GetMapping("/api/chat-rooms/{chatRoomId}/members")
	public ResponseEntity<List<MemberSessionDto>> getLoggedInVisitors(@PathVariable("chatRoomId") Long chatRoomId) {
		return ResponseEntity.ok(memberSessionRegistry.getMembersInChatRoom(TOPIC_PREFIX + chatRoomId));
	}
}
