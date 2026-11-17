package com.example.BLOCO.user.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.BLOCO.user.entity.UserAccount;
import com.example.BLOCO.user.enums.ActivityLevel;
import com.example.BLOCO.user.enums.Gender;

public record UserProfileResponse(
	UUID id,
	Gender gender,
	Integer ageYears,
	BigDecimal heightCm,
	BigDecimal weightKg,
	ActivityLevel activityLevel,
	Integer basalMetabolicRateKcal,
	Integer estimatedDailyEnergyExpenditureKcal
) {

	public static UserProfileResponse from(UserAccount user) {
		return new UserProfileResponse(
			user.getId(),
			user.getGender(),
			user.getAge(),
			user.getHeightCm(),
			user.getWeightKg(),
			user.getActivityLevel(),
			user.getBasalMetabolicRateKcal(),
			user.getEstimatedDailyEnergyExpenditureKcal());
	}
}
