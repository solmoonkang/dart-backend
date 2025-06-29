package com.dart.api.infrastructure.batch.chat;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRedisRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.cache.ChatRoomCacheDto;
import com.dart.api.dto.chat.request.cache.MemberCacheDto;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class ChatMessageBatchProcessor implements ItemProcessor<ChatMessageCreateDto, ChatMessage> {

	private final EntityManager entityManager;
	private final ChatMessageRedisRepository chatMessageRedisRepository;

	@Value("#{jobParameters['chatRoomId']}")
	public Long chatRoomId;

	@Override
	public ChatMessage process(ChatMessageCreateDto chatMessageCreateDto) {
		final ChatRoom chatRoom = getChatRoomFromCache(chatRoomId);
		final Member member = getMemberFromCache(chatMessageCreateDto.sender());
		return ChatMessage.chatMessageFromCreateDto(chatRoom, member, chatMessageCreateDto);
	}

	private ChatRoom getChatRoomFromCache(Long chatRoomId) {
		final ChatRoomCacheDto chatRoomCacheDto = chatMessageRedisRepository.getChatRoomCache(chatRoomId);
		return entityManager.getReference(ChatRoom.class, chatRoomCacheDto.chatRoomId());
	}

	private Member getMemberFromCache(String nickname) {
		final MemberCacheDto memberCacheDto = chatMessageRedisRepository.getMemberCache(nickname);
		return entityManager.getReference(Member.class, memberCacheDto.memberId());
	}
}
