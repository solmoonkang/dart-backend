package com.dart.api.dto.chat.request.cache;

import com.dart.api.domain.member.entity.Member;

import lombok.Builder;

@Builder
public record MemberCacheDto(
	Long memberId,

	String nickname,

	String profileImageURI
) {

	public static MemberCacheDto createMemberCacheDto(Member member) {
		return new MemberCacheDto(member.getId(), member.getNickname(), member.getProfileImageUrl());
	}
}
