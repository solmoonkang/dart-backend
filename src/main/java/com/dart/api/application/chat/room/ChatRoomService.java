package com.dart.api.application.chat.room;

import org.springframework.stereotype.Service;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRedisRepository chatRedisRepository;
	private final ChatMessageRepository chatMessageRepository;

	public void createChatRoom(Gallery gallery) {
		final ChatRoom chatRoom = ChatRoom.createChatRoom(gallery);
		chatRoomRepository.save(chatRoom);
	}

	public void deleteChatRoom(Gallery gallery) {
		final ChatRoom chatRoom = chatRoomRepository.findByGallery(gallery)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));

		chatMessageRepository.deleteByChatRoom(chatRoom);
		chatRedisRepository.deleteChatMessages(chatRoom.getId());
		chatRoomRepository.delete(chatRoom);
	}
}
