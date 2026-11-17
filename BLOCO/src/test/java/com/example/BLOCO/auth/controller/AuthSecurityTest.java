package com.example.BLOCO.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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

import com.example.BLOCO.auth.dto.CurrentUserResponse;
import com.example.BLOCO.auth.dto.LoginRequest;
import com.example.BLOCO.auth.dto.UserResponse;
import com.example.BLOCO.auth.controller.CsrfController;
import com.example.BLOCO.auth.service.AuthService;
import com.example.BLOCO.auth.service.JwtService;
import com.example.BLOCO.config.AuthCookie;
import com.example.BLOCO.config.CookieBearerTokenResolver;
import com.example.BLOCO.config.CsrfConfig;
import com.example.BLOCO.config.JwtConfig;
import com.example.BLOCO.config.SecurityConfig;
import com.example.BLOCO.user.controller.UserProfileController;
import com.example.BLOCO.user.dto.UpdateUserProfileRequest;
import com.example.BLOCO.user.dto.UserProfileResponse;
import com.example.BLOCO.user.enums.ActivityLevel;
import com.example.BLOCO.user.enums.Gender;
import com.example.BLOCO.user.service.UserProfileService;

@WebMvcTest(controllers = {AuthController.class, CsrfController.class, UserProfileController.class}, properties = {
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

	@MockitoBean
	private UserProfileService userProfileService;

	@Test
	void loginSetsHttpOnlyJwtCookieAndCookieAuthenticatesProtectedRoute() throws Exception {
		UUID userId = UUID.randomUUID();
		when(authService.login(any(LoginRequest.class)))
			.thenReturn(new UserResponse(userId, "Pessoa", "person@example.com", null));
		when(authService.getCurrentUser(userId))
			.thenReturn(new CurrentUserResponse(
				userId,
				"Pessoa",
				"person@example.com",
				null,
				29,
				Gender.FEMALE,
				new BigDecimal("170.50"),
				new BigDecimal("62.40"),
				ActivityLevel.MODERATELY_ACTIVE,
				1650,
				2558,
				2100,
				2500,
				900));
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
			.andExpect(jsonPath("$.email").value("person@example.com"))
			.andExpect(jsonPath("$.basalMetabolicRateKcal").value(1650))
			.andExpect(jsonPath("$.estimatedDailyEnergyExpenditureKcal").value(2558))
			.andExpect(jsonPath("$.dailyCalorieConsumptionKcal").value(2100))
			.andExpect(jsonPath("$.waterConsumptionGoalMl").value(2500))
			.andExpect(jsonPath("$.dailyWaterConsumptionMl").value(900))
			.andExpect(jsonPath("$.age").value(29))
			.andExpect(jsonPath("$.gender").value("FEMALE"))
			.andExpect(jsonPath("$.heightCm").value(170.5))
			.andExpect(jsonPath("$.weightKg").value(62.4))
			.andExpect(jsonPath("$.activityLevel").value(3));
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
	void profileUpdateAcceptsImperialUnitsAndRequiresAuthenticationAndCsrf() throws Exception {
		UUID userId = UUID.randomUUID();
		CsrfSession csrf = csrfSession();
		when(userProfileService.updateProfile(eq(userId), any(UpdateUserProfileRequest.class)))
			.thenReturn(new UserProfileResponse(
				userId,
				Gender.FEMALE,
				29,
				new BigDecimal("170.18"),
				new BigDecimal("68.04"),
				ActivityLevel.MODERATELY_ACTIVE,
				1438,
				2229));

		mockMvc.perform(put("/api/v1/me/profile")
				.cookie(new Cookie(AuthCookie.NAME, validToken(userId)), csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"gender":"FEMALE","ageYears":29,
					 "height":{"unit":"FT_IN","feet":5,"inches":7},
					 "weight":{"unit":"LB","value":150},
					 "activityLevel":3}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ageYears").value(29))
			.andExpect(jsonPath("$.heightCm").value(170.18))
			.andExpect(jsonPath("$.weightKg").value(68.04))
			.andExpect(jsonPath("$.activityLevel").value(3))
			.andExpect(jsonPath("$.basalMetabolicRateKcal").value(1438))
			.andExpect(jsonPath("$.estimatedDailyEnergyExpenditureKcal").value(2229));
	}

	@Test
	void profileUpdateRejectsOutOfRangeValues() throws Exception {
		UUID userId = UUID.randomUUID();
		CsrfSession csrf = csrfSession();

		mockMvc.perform(put("/api/v1/me/profile")
				.cookie(new Cookie(AuthCookie.NAME, validToken(userId)), csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"gender":"FEMALE","ageYears":121,
					 "height":{"unit":"CM","value":170},
					 "weight":{"unit":"KG","value":70},
					 "activityLevel":3}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fields.ageYears").exists());
	}

	@Test
	void profileUpdateRejectsMixedHeightUnits() throws Exception {
		UUID userId = UUID.randomUUID();
		CsrfSession csrf = csrfSession();

		mockMvc.perform(put("/api/v1/me/profile")
				.cookie(new Cookie(AuthCookie.NAME, validToken(userId)), csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"gender":"MALE","ageYears":35,
					 "height":{"unit":"CM","value":180,"feet":5,"inches":11},
					 "weight":{"unit":"KG","value":80},
					 "activityLevel":4}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
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

	private String validToken(UUID userId) {
		Instant issuedAt = Instant.now();
		return jwtEncoder.encode(JwtEncoderParameters.from(
			JwsHeader.with(MacAlgorithm.HS256).build(),
			JwtClaimsSet.builder()
				.issuer("BLOCO")
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(900))
				.subject(userId.toString())
				.claim("email", "person@example.com")
				.claim("name", "Pessoa")
				.build())).getTokenValue();
	}

	private record CsrfSession(String token, Cookie cookie) {
	}
}
