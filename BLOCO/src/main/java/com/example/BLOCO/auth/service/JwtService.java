package com.example.BLOCO.auth.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.BLOCO.auth.dto.UserResponse;
import com.example.BLOCO.config.AuthCookie;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtEncoder jwtEncoder;

	@Value("${security.jwt.issuer}")
	private String issuer;

	@Value("${security.jwt.access-token-ttl:PT15M}")
	private Duration accessTokenTtl;

	@Value("${security.jwt.cookie-secure:true}")
	private boolean secureCookie;

    public ResponseCookie createAccessTokenCookie(UserResponse user) {
		Instant issuedAt = Instant.now();
		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
			.issuer(issuer)
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plus(accessTokenTtl))
			.subject(user.id().toString())
			.claim("email", user.email())
			.claim("name", user.name());
		if (user.phone() != null) {
			claims.claim("phone", user.phone());
		}
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
			JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();

		return ResponseCookie.from(AuthCookie.NAME, token)
			.httpOnly(true)
			.secure(secureCookie)
			.sameSite("Lax")
			.path("/api")
			.maxAge(accessTokenTtl)
			.build();
	}

	public ResponseCookie clearAccessTokenCookie() {
		return ResponseCookie.from(AuthCookie.NAME, "")
			.httpOnly(true)
			.secure(secureCookie)
			.sameSite("Lax")
			.path("/api")
			.maxAge(Duration.ZERO)
			.build();
	}
}
