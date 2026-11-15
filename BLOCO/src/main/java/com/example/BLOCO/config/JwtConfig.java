package com.example.BLOCO.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

	@Bean
	SecretKey jwtSecretKey(@Value("${security.jwt.secret-base64}") String secretBase64) {
		if (secretBase64.isBlank()) {
			throw new IllegalStateException(
				"JWT_SECRET_BASE64 must be configured with a Base64-encoded secret of at least 32 bytes.");
		}

		byte[] secret;
		try {
			secret = Base64.getDecoder().decode(secretBase64);
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalStateException("JWT_SECRET_BASE64 must contain valid Base64.", exception);
		}
		if (secret.length < 32) {
			throw new IllegalStateException("JWT_SECRET_BASE64 must decode to at least 32 bytes for HS256.");
		}

		return new SecretKeySpec(secret, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
	}

	@Bean
	JwtDecoder jwtDecoder(
		SecretKey jwtSecretKey,
		@Value("${security.jwt.issuer}") String issuer
	) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
		return decoder;
	}
}
