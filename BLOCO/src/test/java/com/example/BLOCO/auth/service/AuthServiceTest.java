package com.example.BLOCO.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

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
import com.example.BLOCO.auth.exception.InvalidSessionException;
import com.example.BLOCO.auth.dto.CurrentUserResponse;
import com.example.BLOCO.auth.dto.LoginRequest;
import com.example.BLOCO.auth.dto.RegisterRequest;
import com.example.BLOCO.auth.dto.UserResponse;
import com.example.BLOCO.user.entity.DailyIntake;
import com.example.BLOCO.user.entity.UserAccount;
import com.example.BLOCO.user.repository.DailyIntakeRepository;
import com.example.BLOCO.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private DailyIntakeRepository dailyIntakeRepository;

	private PasswordEncoder passwordEncoder;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BCryptPasswordEncoder();
		authService = new AuthService(userRepository, dailyIntakeRepository, passwordEncoder);
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
		assertEquals(0, savedUser.getBasalMetabolicRateKcal());
		assertEquals(0, savedUser.getWaterConsumptionGoalMl());
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

	@Test
	void currentUserReturnsStoredNutritionMetrics() {
		UserAccount user = UserAccount.create(
			"Pessoa", "person@example.com", passwordEncoder.encode("senha-segura-123"), null);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		CurrentUserResponse response = authService.getCurrentUser(user.getId());

		assertEquals(user.getId(), response.id());
		assertEquals("Pessoa", response.name());
		assertEquals(0, response.basalMetabolicRateKcal());
		assertEquals(0, response.estimatedDailyEnergyExpenditureKcal());
		assertEquals(0, response.dailyCalorieConsumptionKcal());
		assertEquals(0, response.waterConsumptionGoalMl());
		assertEquals(0, response.dailyWaterConsumptionMl());
		assertNull(response.age());
		assertNull(response.gender());
		assertNull(response.heightCm());
		assertNull(response.weightKg());
		assertNull(response.activityLevel());
	}

	@Test
	void currentUserReturnsTodayIntakeFromDailyRecord() {
		UserAccount user = UserAccount.create(
			"Pessoa", "person@example.com", passwordEncoder.encode("senha-segura-123"), null);
		DailyIntake dailyIntake = DailyIntake.create(user, LocalDate.now());
		dailyIntake.addCalories(2100);
		dailyIntake.addWater(900);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(dailyIntakeRepository.findByUser_IdAndRecordDate(user.getId(), LocalDate.now()))
			.thenReturn(Optional.of(dailyIntake));

		CurrentUserResponse response = authService.getCurrentUser(user.getId());

		assertEquals(2100, response.dailyCalorieConsumptionKcal());
		assertEquals(900, response.dailyWaterConsumptionMl());
	}

	@Test
	void currentUserRejectsTokenForDeletedUser() {
		UUID userId = UUID.randomUUID();
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(InvalidSessionException.class, () -> authService.getCurrentUser(userId));
	}
}
