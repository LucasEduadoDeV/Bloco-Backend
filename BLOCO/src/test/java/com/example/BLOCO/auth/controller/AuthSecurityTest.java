package com.example.BLOCO.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.time.Instant;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.BLOCO.auth.service.JwtService;
import com.example.BLOCO.auth.service.AuthService;
import com.example.BLOCO.auth.dto.LoginRequest;
import com.example.BLOCO.auth.dto.UserResponse;
import com.example.BLOCO.auth.controller.CsrfController;
import com.example.BLOCO.config.AuthCookie;
import com.example.BLOCO.config.CookieBearerTokenResolver;
import com.example.BLOCO.config.CsrfConfig;
import com.example.BLOCO.config.JwtConfig;
import com.example.BLOCO.config.SecurityConfig;

@WebMvcTest(controllers = {AuthController.class, CsrfController.class}, properties = {
	"security.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@Import({SecurityConfig.class, JwtConfig.class, CsrfConfig.class, CookieBearerTokenResolver.class, JwtService.class})
class AuthSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder;

	@MockitoBean
	private AuthService authService;

	@Test
	void loginSetsHttpOnlyJwtCookieAndCookieAuthenticatesProtectedRoute() throws Exception {
		UUID userId = UUID.randomUUID();
		when(authService.login(any(LoginRequest.class)))
			.thenReturn(new UserResponse(userId, "Pessoa", "person@example.com", null));
		CsrfSession csrf = csrfSession();

		MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
				.cookie(csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"person@example.com","password":"senha-segura-123"}
					"""))
			.andExpect(status().isOk())
			.andExpect(cookie().doesNotExist("JSESSIONID"))
			.andExpect(header().string("Set-Cookie",
				org.hamcrest.Matchers.allOf(
					org.hamcrest.Matchers.containsString(AuthCookie.NAME + "="),
					org.hamcrest.Matchers.containsString("HttpOnly"),
					org.hamcrest.Matchers.containsString("Secure"),
					org.hamcrest.Matchers.containsString("SameSite=Lax"),
					org.hamcrest.Matchers.containsString("Max-Age=900"))))
			.andReturn();

		String authCookie = loginResult.getResponse().getCookie(AuthCookie.NAME).getValue();
		mockMvc.perform(get("/api/v1/me").cookie(new Cookie(AuthCookie.NAME, authCookie)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId.toString()))
			.andExpect(jsonPath("$.email").value("person@example.com"));
	}

	@Test
	void loginRejectsRequestWithoutCsrfToken() throws Exception {
		when(authService.login(any(LoginRequest.class)))
			.thenReturn(new UserResponse(UUID.randomUUID(), "Pessoa", "person@example.com", null));

		mockMvc.perform(post("/api/v1/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"person@example.com","password":"senha-segura-123"}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void otherRoutesAreDenied() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void expiredJwtCookieCannotAccessProtectedRoute() throws Exception {
		Instant expiredAt = Instant.now().minusSeconds(120);
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
			JwsHeader.with(MacAlgorithm.HS256).build(),
			JwtClaimsSet.builder()
				.issuer("BLOCO")
				.issuedAt(expiredAt.minusSeconds(60))
				.expiresAt(expiredAt)
				.subject(UUID.randomUUID().toString())
				.claim("email", "person@example.com")
				.claim("name", "Pessoa")
				.build())).getTokenValue();

		mockMvc.perform(get("/api/v1/me").cookie(new Cookie(AuthCookie.NAME, token)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void logoutClearsJwtCookie() throws Exception {
		CsrfSession csrf = csrfSession();

		mockMvc.perform(post("/api/v1/logout")
				.cookie(csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token()))
			.andExpect(status().isNoContent())
			.andExpect(header().string("Set-Cookie",
				org.hamcrest.Matchers.allOf(
					org.hamcrest.Matchers.containsString(AuthCookie.NAME + "="),
					org.hamcrest.Matchers.containsString("Max-Age=0"),
					org.hamcrest.Matchers.containsString("HttpOnly"))));
	}

	private CsrfSession csrfSession() throws Exception {
		MvcResult csrfResult = mockMvc.perform(get("/api/v1/csrf"))
			.andExpect(status().isNoContent())
			.andReturn();
		Cookie cookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
		return new CsrfSession(cookie.getValue(), cookie);
	}

	private record CsrfSession(String token, Cookie cookie) {
	}
}
