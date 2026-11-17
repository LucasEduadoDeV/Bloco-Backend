package com.example.BLOCO.user.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.BLOCO.user.enums.Gender;

public final class BasalMetabolicRateCalculator {

	private static final BigDecimal TEN = BigDecimal.TEN;
	private static final BigDecimal SIX_POINT_TWO_FIVE = new BigDecimal("6.25");
	private static final BigDecimal FIVE = new BigDecimal("5");

	private BasalMetabolicRateCalculator() {
	}

	public static int calculate(Gender gender, int ageYears, BigDecimal heightCm, BigDecimal weightKg) {
		BigDecimal genderConstant = switch (gender) {
			case MALE -> FIVE;
			case FEMALE -> new BigDecimal("-161");
		};

		return weightKg.multiply(TEN)
			.add(heightCm.multiply(SIX_POINT_TWO_FIVE))
			.subtract(BigDecimal.valueOf(ageYears).multiply(FIVE))
			.add(genderConstant)
			.setScale(0, RoundingMode.HALF_UP)
			.intValueExact();
	}
}
