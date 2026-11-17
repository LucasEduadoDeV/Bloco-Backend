package com.example.BLOCO.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BLOCO.auth.exception.InvalidSessionException;
import com.example.BLOCO.user.dto.UpdateUserProfileRequest;
import com.example.BLOCO.user.dto.UserProfileResponse;
import com.example.BLOCO.user.entity.UserAccount;
import com.example.BLOCO.user.repository.UserRepository;

@Service
public class UserProfileService {

	private final UserRepository userRepository;

	public UserProfileService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
		UserAccount user = userRepository.findById(userId)
			.orElseThrow(InvalidSessionException::new);
		int basalMetabolicRateKcal = BasalMetabolicRateCalculator.calculate(
			request.gender(), request.ageYears(), request.heightCm(), request.weightKg());
		user.updateProfile(
			request.gender(),
			request.ageYears(),
			request.heightCm(),
			request.weightKg(),
			request.activityLevel(),
			basalMetabolicRateKcal,
			DailyEnergyExpenditureCalculator.calculate(basalMetabolicRateKcal, request.activityLevel()));
		return UserProfileResponse.from(userRepository.save(user));
	}
}
