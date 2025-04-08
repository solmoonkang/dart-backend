package com.dart.api.domain.member.entity;

import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

public enum OAuthProvider {
	KAKAO,
	GOOGLE,
	NONE;

	public static OAuthProvider findByName(String name) {
		for(OAuthProvider oauthProvider : values()) {
			if(oauthProvider.name().equalsIgnoreCase(name)) {
				return oauthProvider;
			}
		}
		throw new NotFoundException(ErrorCode.FAIL_REGISTRATION_NOT_FOUND);
	}
}
