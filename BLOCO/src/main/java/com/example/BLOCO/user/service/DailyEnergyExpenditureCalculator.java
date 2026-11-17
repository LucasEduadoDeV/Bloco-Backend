package com.example.BLOCO.user.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.BLOCO.user.enums.ActivityLevel;

public final class DailyEnergyExpenditureCalculator {

	private DailyEnergyExpenditureCalculator() {
	}

	public static int calculate(int basalMetabolicRateKcal, ActivityLevel activityLevel) {
		return BigDecimal.valueOf(basalMetabolicRateKcal)
			.multiply(activityLevel.getMaintenanceFactor())
			.setScale(0, RoundingMode.HALF_UP)
			.intValueExact();
	}
}
