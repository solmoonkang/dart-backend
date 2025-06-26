package com.dart.api.domain.chat.entity;

import java.time.LocalDateTime;

import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "is_author", nullable = false)
	private boolean isAuthor;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private ChatMessage(String content, boolean isAuthor, LocalDateTime createdAt, ChatRoom chatRoom, Member member) {
		this.content = content;
		this.isAuthor = isAuthor;
		this.createdAt = createdAt;
		this.chatRoom = chatRoom;
		this.member = member;
	}

	public static ChatMessage chatMessageFromCreateDto(
		ChatRoom chatRoom,
		Member member,
		ChatMessageCreateDto chatMessageCreateDto
	) {
		return ChatMessage.builder()
			.content(chatMessageCreateDto.content())
			.isAuthor(chatMessageCreateDto.isAuthor())
			.chatRoom(chatRoom)
			.member(member)
			.build();
	}

	public static ChatMessage chatMessageFromReadDto(
		ChatRoom chatRoom,
		Member member,
		ChatMessageReadDto chatMessageReadDto
	) {
		return ChatMessage.builder()
			.chatRoom(chatRoom)
			.content(chatMessageReadDto.content())
			.isAuthor(chatMessageReadDto.isAuthor())
			.member(member)
			.build();
	}

	public ChatMessageReadDto toChatMessageReadDto() {
		return ChatMessageReadDto.builder()
			.sender(this.member.getNickname())
			.content(this.content)
			.createdAt(this.getCreatedAt())
			.isAuthor(this.isAuthor)
			.profileImageUrl(this.member.getProfileImageUrl())
			.build();
	}

	public ChatMessageSendDto toChatMessageSendDto(long expirySeconds) {
		return ChatMessageSendDto.builder()
			.memberId(this.member.getId())
			.chatRoomId(this.chatRoom.getId())
			.content(this.content)
			.createdAt(this.getCreatedAt())
			.isAuthor(this.isAuthor)
			.expirySeconds(expirySeconds)
			.build();
	}
}
