package com.dart.api.application.chat.message;

import org.springframework.stereotype.Service;

import com.dart.api.dto.chat.request.ChatMessageSendDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageReadService chatMessageReadService;
	private final ChatMessageWriteService chatMessageWriteService;

	public void saveChatMessage(Long chatRoomId, ChatMessageSendDto chatMessageSendDto) {
		chatMessageReadService.validateChatRoomExistsInCache(chatRoomId);
		chatMessageReadService.validateMemberExistsInCache(chatMessageSendDto.sender());
		chatMessageWriteService.saveChatMessage(chatRoomId, chatMessageSendDto);
	}

	public PageResponse<ChatMessageReadDto> getAllChatMessages(Long chatRoomId, int page, int size) {
		return chatMessageReadService.findChatMessages(chatRoomId, page, size);
	}
}
