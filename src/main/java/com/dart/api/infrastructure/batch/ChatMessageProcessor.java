package com.dart.api.infrastructure.batch;

import static com.dart.global.error.model.ErrorCode.*;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.global.error.exception.NotFoundException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessageProcessor implements ItemProcessor<ChatMessageReadDto, ChatMessage> {

	private final MemberRepository memberRepository;
	private final ChatRoomRepository chatRoomRepository;

	private Map<Long, ChatRoom> chatRoomCache;
	private Map<String, Member> memberCache;

	@PostConstruct
	public void init() {
		chatRoomCache = chatRoomRepository.findAll().stream()
			.collect(Collectors.toMap(ChatRoom::getId, Function.identity()));

		memberCache = memberRepository.findAll().stream()
			.collect(Collectors.toMap(Member::getNickname, Function.identity()));
	}

	@Override
	public ChatMessage process(ChatMessageReadDto chatMessageReadDto) {
		final ChatRoom chatRoom = chatRoomCache.get(chatMessageReadDto.chatRoomId());
		validateChatRoomExists(chatRoom);

		final Member member = memberCache.get(chatMessageReadDto.sender());
		validateMemberExists(member);

		return ChatMessage.recreateFrom(chatRoom, member, chatMessageReadDto);
	}

	private void validateChatRoomExists(ChatRoom chatRoom) {
		if (chatRoom == null) {
			throw new NotFoundException(FAIL_CHAT_ROOM_NOT_FOUND);
		}
	}

	private void validateMemberExists(Member member) {
		if (member == null) {
			throw new NotFoundException(FAIL_MEMBER_NOT_FOUND);
		}
	}
}
