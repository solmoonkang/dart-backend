package com.dart.api.application.auth;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.repository.SessionRedisRepository;
import com.dart.api.domain.auth.repository.TokenRedisRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.auth.response.TokenResDto;
import com.dart.api.dto.member.request.LoginReqDto;
import com.dart.api.dto.member.response.LoginResDto;
import com.dart.global.common.util.CookieUtil;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProviderService jwtProviderService;
	private final TokenRedisRepository tokenRedisRepository;
	private final SessionRedisRepository sessionRedisRepository;

	private final CookieUtil cookieUtil;

	@Transactional
	public LoginResDto login(LoginReqDto loginReqDto, HttpServletRequest request, HttpServletResponse response) {
		final Member member = findByMemberEmail(loginReqDto.email());
		final String email = member.getEmail();
		String clientInfo = extractClientInfo(request);

		validatePasswordMatch(loginReqDto.password(), member.getPassword());

		final String accessToken = jwtProviderService.generateAccessToken(member.getId(), member.getEmail(),
			member.getNickname(), member.getProfileImageUrl(), clientInfo);
		final String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		tokenRedisRepository.saveBlacklistToken(email, accessToken);

		saveTokensInResponse(response, accessToken, refreshToken);

		return new LoginResDto(accessToken, member.getEmail(), member.getNickname(), member.getProfileImageUrl());
	}

	public LoginResDto socialLogin(String sessionId, HttpServletRequest request, HttpServletResponse response) {
		final String email = sessionRedisRepository.getSessionLoginMapping(sessionId);
		final Member member = findByMemberEmail(email);

		String clientInfo = extractClientInfo(request);

		final String accessToken = jwtProviderService.generateAccessToken(member.getId(), email,
			member.getNickname(), member.getProfileImageUrl(), clientInfo);
		final String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		tokenRedisRepository.saveBlacklistToken(email, accessToken);

		saveTokensInResponse(response, accessToken, refreshToken);

		return new LoginResDto(accessToken, member.getEmail(), member.getNickname(), member.getProfileImageUrl());
	}

	@Transactional
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = extractTokenFromHeader(request);
		String email = jwtProviderService.extractEmailFromToken(accessToken);

		deleteAllTokensAndCookie(response, email);
	}

	@Transactional
	public TokenResDto reissue(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = extractTokenFromHeader(request);
		String refreshToken = cookieUtil.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);

		if (accessToken == null || refreshToken == null) {
			throw new NotFoundException(ErrorCode.FAIL_TOKEN_NOT_FOUND);
		}

		try {
			String email = jwtProviderService.extractEmailFromToken(refreshToken);
			String clientInfo = extractClientInfo(request);

			validateRefreshToken(email);
			validateBlacklist(email, accessToken);

			tokenRedisRepository.deleteBlacklistToken(email);

			String newAccessToken = generateAccessToken(accessToken, clientInfo);

			tokenRedisRepository.saveBlacklistToken(email, newAccessToken);
			saveAccessTokenInResponse(response, newAccessToken);
			return new TokenResDto(newAccessToken);

		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	private Member findByMemberEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private String generateAccessToken(String accessToken, String clientInfo) {
		return jwtProviderService.generateAccessToken(
			jwtProviderService.extractIdFromToken(accessToken),
			jwtProviderService.extractEmailFromToken(accessToken),
			jwtProviderService.extractNicknameFromToken(accessToken),
			jwtProviderService.extractProfileImageFromToken(accessToken),
			clientInfo
		);
	}

	private void saveTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		saveAccessTokenInResponse(response, accessToken);
		saveRefreshTokenInResponse(response, refreshToken);
	}

	private void saveAccessTokenInResponse(HttpServletResponse response, String accessToken) {
		response.setHeader(ACCESS_TOKEN_HEADER, accessToken);
	}

	private void saveRefreshTokenInResponse(HttpServletResponse response, String refreshToken) {
		cookieUtil.setRefreshCookie(response, refreshToken);
	}

	private String extractTokenFromHeader(HttpServletRequest request) {
		return request.getHeader(ACCESS_TOKEN_HEADER).replace(BEARER, BLANK).trim();
	}

	private String extractClientInfo(HttpServletRequest request) {
		String ipAddress = request.getRemoteAddr();
		String userAgent = request.getHeader("User-Agent");
		return ipAddress + "|" + userAgent;
	}

	private void validatePasswordMatch(String password, String encodedPassword) {
		if (!passwordEncoder.matches(password, encodedPassword)) {
			throw new BadRequestException(ErrorCode.FAIL_INCORRECT_PASSWORD);
		}
	}

	protected void validateRefreshToken(String email) {
		if (!tokenRedisRepository.checkRefreshTokenExists(email)) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_REFRESH_TOKEN);
		}
	}

	private void validateBlacklist(String email, String token) {
		if (!isBlacklist(email, token)) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	private boolean isBlacklist(String email, String token) {
		return tokenRedisRepository.getBlacklistToken(email).equals(token)
			&& tokenRedisRepository.getAccessToken(email) == null;
	}

	private void deleteAllTokensAndCookie(HttpServletResponse response, String email) {
		tokenRedisRepository.deleteAccessToken(email);
		tokenRedisRepository.deleteBlacklistToken(email);
		tokenRedisRepository.deleteRefreshToken(email);
	}
}
