package com.example.BLOCO.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.BLOCO.auth.exception.EmailAlreadyRegisteredException;
import com.example.BLOCO.auth.exception.InvalidCredentialsException;
import com.example.BLOCO.auth.dto.LoginRequest;
import com.example.BLOCO.auth.dto.RegisterRequest;
import com.example.BLOCO.auth.dto.UserResponse;
import com.example.BLOCO.user.entity.UserAccount;
import com.example.BLOCO.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	private PasswordEncoder passwordEncoder;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BCryptPasswordEncoder();
		authService = new AuthService(userRepository, passwordEncoder);
	}

	@Test
	void registerNormalizesEmailAndStoresOnlyPasswordHash() {
		when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
		when(userRepository.saveAndFlush(any(UserAccount.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		UserResponse response = authService.register(new RegisterRequest(
			"  Pessoa  ", " Person@Example.com ", "senha-segura-123", "  (11) 99999-9999  "));

		ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
		verify(userRepository).saveAndFlush(userCaptor.capture());
		UserAccount savedUser = userCaptor.getValue();

		assertEquals("Pessoa", response.name());
		assertEquals("person@example.com", response.email());
		assertEquals("(11) 99999-9999", response.phone());
		assertNotEquals("senha-segura-123", savedUser.getPasswordHash());
		assertTrue(passwordEncoder.matches("senha-segura-123", savedUser.getPasswordHash()));
	}

	@Test
	void registerRejectsAnExistingEmail() {
		when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

		assertThrows(EmailAlreadyRegisteredException.class, () -> authService.register(
			new RegisterRequest("Pessoa", "person@example.com", "senha-segura-123", null)));
	}

	@Test
	void registerRejectsEmailAlreadyInsertedByAnotherRequest() {
		when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
		when(userRepository.saveAndFlush(any(UserAccount.class)))
			.thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate email"));

		assertThrows(EmailAlreadyRegisteredException.class, () -> authService.register(
			new RegisterRequest("Pessoa", "person@example.com", "senha-segura-123", null)));
	}

	@Test
	void loginReturnsPublicProfileForValidCredentials() {
		UserAccount user = UserAccount.create(
			"Pessoa", "person@example.com", passwordEncoder.encode("senha-segura-123"), null);
		when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));

		UserResponse response = authService.login(new LoginRequest("PERSON@example.com", "senha-segura-123"));

		assertEquals("person@example.com", response.email());
		assertEquals("Pessoa", response.name());
		assertEquals(user.getId(), response.id());
	}

	@Test
	void loginDoesNotRevealWhetherEmailExists() {
		when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.empty());

		assertThrows(InvalidCredentialsException.class, () -> authService.login(
			new LoginRequest("person@example.com", "senha-errada")));
	}
}
