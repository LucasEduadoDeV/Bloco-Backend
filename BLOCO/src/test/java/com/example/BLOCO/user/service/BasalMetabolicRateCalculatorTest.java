package com.example.BLOCO.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.example.BLOCO.user.enums.Gender;

class BasalMetabolicRateCalculatorTest {

	@Test
	void calculatesMifflinStJeorForMaleAndFemaleProfiles() {
		BigDecimal heightCm = new BigDecimal("170.18");
		BigDecimal weightKg = new BigDecimal("68.04");

		assertEquals(1604, BasalMetabolicRateCalculator.calculate(Gender.MALE, 29, heightCm, weightKg));
		assertEquals(1438, BasalMetabolicRateCalculator.calculate(Gender.FEMALE, 29, heightCm, weightKg));
	}
}
