package com.example.BLOCO.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class CsrfConfig {

	@Bean
	CookieCsrfTokenRepository csrfTokenRepository(
		@Value("${security.jwt.cookie-secure:true}") boolean secureCookie
	) {
		CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		repository.setCookieCustomizer(cookie -> cookie
			.path("/")
			.secure(secureCookie)
			.sameSite("Lax"));
		return repository;
	}

	@Bean
	SpaCsrfTokenRequestHandler csrfTokenRequestHandler() {
		return new SpaCsrfTokenRequestHandler();
	}
}
