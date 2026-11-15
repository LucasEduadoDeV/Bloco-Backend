package com.example.BLOCO.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.BLOCO.auth.exception.EmailAlreadyRegisteredException;
import com.example.BLOCO.auth.exception.InvalidCredentialsException;
import com.example.BLOCO.auth.service.AuthService;
import com.example.BLOCO.auth.service.JwtService;
import com.example.BLOCO.auth.dto.LoginRequest;
import com.example.BLOCO.auth.dto.RegisterRequest;
import com.example.BLOCO.auth.dto.UserResponse;
import com.example.BLOCO.common.web.ApiExceptionHandler;

class AuthControllerTest {

	private AuthService authService;
	private JwtService jwtService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		authService = mock(AuthService.class);
		jwtService = mock(JwtService.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, jwtService))
			.setControllerAdvice(new ApiExceptionHandler())
			.setValidator(validator)
			.build();
	}

	@Test
	void registerReturnsCreatedProfileWithoutPassword() throws Exception {
		when(authService.register(any(RegisterRequest.class)))
			.thenReturn(new UserResponse(UUID.randomUUID(), "Pessoa", "person@example.com", null));

		mockMvc.perform(post("/api/v1/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Pessoa","email":"person@example.com","password":"senha-segura-123"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value("person@example.com"))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void loginReturnsProfileForValidCredentials() throws Exception {
		when(jwtService.createAccessTokenCookie(any(UserResponse.class)))
			.thenReturn(ResponseCookie.from("BLOCO_AUTH", "signed-token").httpOnly(true).build());
		when(authService.login(any(LoginRequest.class)))
			.thenReturn(new UserResponse(UUID.randomUUID(), "Pessoa", "person@example.com", null));

		mockMvc.perform(post("/api/v1/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"person@example.com","password":"senha-segura-123"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("person@example.com"))
			.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
				.header().string("Set-Cookie", org.hamcrest.Matchers.containsString("BLOCO_AUTH=signed-token")));
	}

	@Test
	void registerRejectsInvalidInput() throws Exception {
		mockMvc.perform(post("/api/v1/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"","email":"invalido","password":"curta"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fields.email").exists())
			.andExpect(jsonPath("$.fields.password").exists());
	}

	@Test
	void registerReturnsConflictForAnExistingEmail() throws Exception {
		when(authService.register(any(RegisterRequest.class)))
			.thenThrow(new EmailAlreadyRegisteredException());

		mockMvc.perform(post("/api/v1/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Pessoa","email":"person@example.com","password":"senha-segura-123"}
					"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
	}

	@Test
	void loginReturnsGenericErrorForInvalidCredentials() throws Exception {
		when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/api/v1/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"person@example.com","password":"senha-errada"}
					"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
			.andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
	}
}
