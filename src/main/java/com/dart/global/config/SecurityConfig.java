package com.dart.global.config;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.dart.api.application.auth.CustomOAuth2UserService;
import com.dart.api.application.auth.JwtProviderService;
import com.dart.global.auth.filter.AuthenticationFilter;
import com.dart.global.auth.handler.CustomAuthenticationFailureHandler;
import com.dart.global.auth.handler.CustomAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtProviderService jwtProviderService;
	private final HandlerExceptionResolver handlerExceptionResolver;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
	private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

	public SecurityConfig(
		JwtProviderService jwtProviderService,
		@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
		CustomOAuth2UserService customOAuth2UserService,
		CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
		CustomAuthenticationFailureHandler customAuthenticationFailureHandler
	) {
		this.jwtProviderService = jwtProviderService;
		this.handlerExceptionResolver = handlerExceptionResolver;
		this.customOAuth2UserService = customOAuth2UserService;
		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
		this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
			.requestMatchers("/h2-console/**")
			.requestMatchers("/api/login")
			.requestMatchers("/api/login/social/**")
			.requestMatchers("/api/signup/**")
			.requestMatchers("/api/email/**")
			.requestMatchers("/api/nickname/check")
			.requestMatchers("/api/payment/kakao/**")
			.requestMatchers("/ws/**")
			.requestMatchers("/api/*/chat-messages");
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		httpSecurity
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

		httpSecurity.authorizeHttpRequests((auth) -> auth
			.requestMatchers("/favicon.ico").permitAll()
			.requestMatchers("/ws/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/signup/*").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/reissue").permitAll()
			.requestMatchers(HttpMethod.POST, "/api/email/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/galleries/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/galleries/info").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/reviews/{gallery-id}/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/mypage").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/members").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/reviews/info").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/autocomplete").permitAll()
			.anyRequest().authenticated()
		);

		httpSecurity.oauth2Login((oauth) -> oauth
			.loginPage("/api/login")
			.authorizationEndpoint(authorization -> authorization
				.baseUri("/api/oauth2/authorization")
			)
			.redirectionEndpoint(redirection -> redirection
				.baseUri("/api/oauth2/callback/*")
			)
			.userInfoEndpoint(userInfo -> userInfo
				.userService(customOAuth2UserService)
			)
			.successHandler(customAuthenticationSuccessHandler)
			.failureHandler(customAuthenticationFailureHandler)
		);

		httpSecurity.addFilterBefore(
			new AuthenticationFilter(jwtProviderService, handlerExceptionResolver),
			UsernamePasswordAuthenticationFilter.class
		);

		httpSecurity.exceptionHandling((exceptionHandling) -> {
			HttpStatusEntryPoint httpStatusEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
			exceptionHandling.authenticationEntryPoint(httpStatusEntryPoint);
		});

		return httpSecurity.build();
	}
}
