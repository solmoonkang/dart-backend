package com.dart.global.common.util;

import static com.dart.global.common.util.GlobalConstant.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisConstant {

	public static final String REDIS_ACCESS_TOKEN_PREFIX = "token:access:";
	public static final String REDIS_REFRESH_TOKEN_PREFIX = "token:refresh:";
	public static final String REDIS_BLACKLIST_TOKEN_PREFIX = "token:blacklist:";
	public static final String REDIS_SESSION_LOGIN_PREFIX = "session:login:";
	public static final String REDIS_EMAIL_PREFIX = "email:";
	public static final String REDIS_PAYMENT_PREFIX = "payment:";
	public static final String REDIS_CHAT_MESSAGE_STORE_PREFIX = "chat:message:store:";
	public static final String REDIS_CHAT_MESSAGE_READ_PREFIX = "chat:message:read:";
	public static final String REDIS_CHAT_ROOM_CACHE_PREFIX = "chat:room:exists:";
	public static final String REDIS_MEMBER_CACHE_PREFIX = "chat:member:exists:";
	public static final String REDIS_NICKNAME_PREFIX = "nickname:";
	public static final String REDIS_COUPON_PREFIX = "priority_coupon:";
	public static final String REDIS_COUPON_COUNT_PREFIX = "priority_coupon_count:";
	public static final String REDIS_TITLE_PREFIX = "title:";
	public static final String REDIS_AUTHOR_PREFIX = "author:";
	public static final String REDIS_HASHTAG_PREFIX = "hashtag:";
	public static final String REDIS_COUNT_PREFIX = "count:";

	public static final String REDIS_SESSION_EMAIL_PREFIX = "session:email:";
	public static final String REDIS_SESSION_NICKNAME_PREFIX = "session:nickname:";

	public static final String CODE = "code";
	public static final String RESERVED = "reserved";
	public static final String VERIFIED = "verified";
	public static final String TITLE = "title";
	public static final String AUTHOR = "author";
	public static final String HASHTAG = "hashtag";

	public static final long EMAIL_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final long NICKNAME_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_EMAIL_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_NICKNAME_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
}
