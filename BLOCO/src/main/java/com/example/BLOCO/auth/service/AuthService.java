package com.example.BLOCO.auth.service;

import java.util.Locale;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BLOCO.auth.exception.EmailAlreadyRegisteredException;
import com.example.BLOCO.auth.exception.InvalidCredentialsException;
import com.example.BLOCO.auth.exception.InvalidSessionException;
import com.example.BLOCO.auth.dto.CurrentUserResponse;
import com.example.BLOCO.auth.dto.LoginRequest;
import com.example.BLOCO.auth.dto.RegisterRequest;
import com.example.BLOCO.auth.dto.UserResponse;
import com.example.BLOCO.user.entity.UserAccount;
import com.example.BLOCO.user.repository.DailyIntakeRepository;
import com.example.BLOCO.user.repository.UserRepository;

@Service
public class AuthService {

	private static final int EMPTY_DAILY_INTAKE = 0;

	private final UserRepository userRepository;
	private final DailyIntakeRepository dailyIntakeRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(
		UserRepository userRepository,
		DailyIntakeRepository dailyIntakeRepository,
		PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.dailyIntakeRepository = dailyIntakeRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException();
		}

		String phone = request.phone() == null || request.phone().isBlank()
			? null
			: request.phone().trim();
		UserAccount user = UserAccount.create(
			request.name().trim(),
			email,
			passwordEncoder.encode(request.password()),
			phone);

		try {
			return UserResponse.from(userRepository.saveAndFlush(user));
		}
		catch (DataIntegrityViolationException exception) {
			throw new EmailAlreadyRegisteredException(exception);
		}
	}

	@Transactional(readOnly = true)
	public UserResponse login(LoginRequest request) {
		return userRepository.findByEmail(normalizeEmail(request.email()))
			.filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
			.map(UserResponse::from)
			.orElseThrow(InvalidCredentialsException::new);
	}

	@Transactional(readOnly = true)
	public CurrentUserResponse getCurrentUser(UUID userId) {
		UserAccount user = userRepository.findById(userId)
			.orElseThrow(InvalidSessionException::new);
		return dailyIntakeRepository.findByUser_IdAndRecordDate(userId, LocalDate.now())
			.map(dailyIntake -> CurrentUserResponse.from(
				user, dailyIntake.getCalorieIntakeKcal(), dailyIntake.getWaterIntakeMl()))
			.orElseGet(() -> CurrentUserResponse.from(user, EMPTY_DAILY_INTAKE, EMPTY_DAILY_INTAKE));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
