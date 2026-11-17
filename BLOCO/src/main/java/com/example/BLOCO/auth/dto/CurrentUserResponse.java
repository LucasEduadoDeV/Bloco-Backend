package com.example.BLOCO.auth.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.BLOCO.user.entity.UserAccount;
import com.example.BLOCO.user.enums.ActivityLevel;
import com.example.BLOCO.user.enums.Gender;

public record CurrentUserResponse(
	UUID id,
	String name,
	String email,
	String phone,
	Integer age,
	Gender gender,
	BigDecimal heightCm,
	BigDecimal weightKg,
	ActivityLevel activityLevel,
	Integer basalMetabolicRateKcal,
	Integer estimatedDailyEnergyExpenditureKcal,
	Integer dailyCalorieConsumptionKcal,
	Integer waterConsumptionGoalMl,
	Integer dailyWaterConsumptionMl
) {

	public static CurrentUserResponse from(
		UserAccount user,
		Integer dailyCalorieConsumptionKcal,
		Integer dailyWaterConsumptionMl
	) {
		return new CurrentUserResponse(
			user.getId(),
			user.getName(),
			user.getEmail(),
			user.getPhone(),
			user.getAge(),
			user.getGender(),
			user.getHeightCm(),
			user.getWeightKg(),
			user.getActivityLevel(),
			user.getBasalMetabolicRateKcal(),
			user.getEstimatedDailyEnergyExpenditureKcal(),
			dailyCalorieConsumptionKcal,
			user.getWaterConsumptionGoalMl(),
			dailyWaterConsumptionMl);
	}
}
