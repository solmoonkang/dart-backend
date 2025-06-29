package com.dart.api.domain.chat.entity;

import java.time.LocalDateTime;

import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;

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
		ChatMessageCreateDto chatMessageCreateDto) {

		return new ChatMessage(
			chatMessageCreateDto.content(),
			chatMessageCreateDto.isAuthor(),
			chatMessageCreateDto.createdAt(),
			chatRoom,
			member);
	}
}
