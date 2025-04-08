package com.dart.api.application.auth;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.auth.repository.TokenRedisRepository;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProviderService {

	private static final String ID = "id";
	private static final String EMAIL = "email";
	private static final String NICKNAME = "nickname";
	private static final String PROFILE_IMAGE = "profileImage";
	private static final String CLIENT_INFO = "clientInfo";

	@Value("${jwt.secret.access-key}")
	private String secret;

	@Value("${jwt.access-expire}")
	private long accessTokenExpire;

	@Value("${jwt.refresh-expire}")
	private long refreshTokenExpire;

	private SecretKey secretKey;

	private final TokenRedisRepository tokenRedisRepository;

	@PostConstruct
	private void init() {
		secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	private JwtBuilder buildJwt(Date issuedDate, Date expiredDate) {
		return Jwts.builder()
			.issuedAt(issuedDate)
			.expiration(expiredDate)
			.signWith(secretKey, Jwts.SIG.HS256);
	}

	public boolean isUsable(String token) {
		try {
			if (token == null) {
				return false;
			}

			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token);

			return true;
		} catch (ExpiredJwtException e) {
			log.warn("====== TOKEN EXPIRED ======");
			throw new UnauthorizedException(ErrorCode.FAIL_TOKEN_EXPIRED);
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("====== INVALID TOKEN ======", e);
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	private Claims getClaimsByToken(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_TOKEN_EXPIRED);
		}
	}

	public String generateAccessToken(Long id, String email, String nickname, String profileImage, String clientInfo) {
		String token = buildJwt(new Date(), new Date(System.currentTimeMillis() + accessTokenExpire))
			.claim(ID, id)
			.claim(EMAIL, email)
			.claim(NICKNAME, nickname)
			.claim(PROFILE_IMAGE, profileImage)
			.claim(CLIENT_INFO, clientInfo)
			.compact();

		tokenRedisRepository.saveAccessToken(email, token);
		return token;
	}

	@Transactional
	public String generateRefreshToken(String email) {
		String token = buildJwt(new Date(), new Date(System.currentTimeMillis() + refreshTokenExpire))
			.claim(EMAIL, email)
			.compact();

		tokenRedisRepository.saveRefreshToken(email, token);
		return token;
	}

	public String extractToken(String header, HttpServletRequest request) {
		String token = request.getHeader(header);

		if (token == null || !token.startsWith(BEARER)) {
			log.warn("====== {} is null or not bearer =======", header);
			return null;
		}

		return token.replaceFirst(BEARER, BLANK).trim();
	}

	public AuthUser extractAuthUserByAccessToken(String token) {
		final Claims claims = getClaimsByToken(token);
		return AuthUser.create(claims.get(ID, Long.class), claims.get(EMAIL, String.class),
			claims.get(NICKNAME, String.class));
	}

	public Long extractIdFromToken(String token) {
		try {
			final Claims claims = getClaimsByToken(token);
			Long id = claims.get(ID, Long.class);
			return claims.get(ID, Long.class);
		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	public String extractEmailFromToken(String accessToken) {
		try {
			final Claims claims = getClaimsByToken(accessToken);
			return claims.get(EMAIL, String.class);
		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	public String extractNicknameFromToken(String token) {
		try {
			final Claims claims = getClaimsByToken(token);
			return claims.get(NICKNAME, String.class);
		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	public String extractProfileImageFromToken(String token) {
		try {
			final Claims claims = getClaimsByToken(token);
			return claims.get(PROFILE_IMAGE, String.class);
		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	public boolean isAccessToken(String email) {
		return tokenRedisRepository.checkAccessTokenExists(email);
	}

	public boolean validateAccessToken(String token, String clientInfo) {
		try {
			Claims claims = Jwts.parser()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

			String email = claims.getSubject();
			String storedToken = tokenRedisRepository.getAccessToken(email);

			return token.equals(storedToken) && claims.get(CLIENT_INFO).equals(clientInfo);
		} catch (ExpiredJwtException e) {
			return e.getClaims().get(CLIENT_INFO).equals(clientInfo);
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}
