package com.dart.api.application.chat;

import static com.dart.global.common.util.ChatConstant.*;

import java.util.AbstractMap;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final MemberRepository memberRepository;
	private final ChatRedisRepository chatRedisRepository;
	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public void saveChatMessageToMySQL(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final Member member = getMemberByNickname(chatMessageCreateDto.sender());
		final ChatMessage chatMessage = ChatMessage.chatMessageFromCreateDto(chatRoom, member, chatMessageCreateDto);

		chatMessageRepository.save(chatMessage);
	}

	@Transactional
	public void saveChatMessageToRedis(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final Member member = getMemberByNickname(chatMessageCreateDto.sender());
		final ChatMessage chatMessage = ChatMessage.chatMessageFromCreateDto(chatRoom, member, chatMessageCreateDto);

		final ChatMessageSendDto chatMessageSendDto = chatMessage.toChatMessageSendDto(CHAT_MESSAGE_EXPIRY_SECONDS);
		chatRedisRepository.saveChatMessage(chatMessageSendDto, member);
		chatRedisRepository.saveBatchChatMessage(chatMessageSendDto, member);
	}

	@Scheduled(cron = CHAT_BATCH_SAVE_INTERVAL)
	public void batchSaveMessages() {
		List<Long> activeChatRoomIds = chatRedisRepository.getActiveChatRoomIds();

		activeChatRoomIds.stream()
			.map(chatRoomId -> {
				List<ChatMessageCreateDto> messageCreateDtoList = chatRedisRepository.getAllBatchMessages(chatRoomId);
				return new AbstractMap.SimpleEntry<>(chatRoomId, messageCreateDtoList);
			})
			.filter(entry -> !entry.getValue().isEmpty())
			.forEach(entry -> saveMessagesIfNotEmpty(entry.getValue(), entry.getKey()));
	}

	private void saveMessagesIfNotEmpty(List<ChatMessageCreateDto> chatMessageCreateDtoList, Long chatRoomId) {
		if (!chatMessageCreateDtoList.isEmpty()) {
			final ChatRoom chatRoom = getChatRoomById(chatRoomId);
			List<ChatMessage> chatMessageList = chatMessageCreateDtoList.stream()
				.map(chatMessageCreateDto -> {
					final Member member = getMemberByNickname(chatMessageCreateDto.sender());
					return ChatMessage.chatMessageFromCreateDto(chatRoom, member, chatMessageCreateDto);
				})
				.toList();

			chatMessageRepository.saveAll(chatMessageList);
			chatRedisRepository.deleteBatchChatMessages(chatRoomId);
		}
	}

	private ChatRoom getChatRoomById(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));
	}

	private Member getMemberByNickname(String nickname) {
		return memberRepository.findByNickname(nickname)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}
}
