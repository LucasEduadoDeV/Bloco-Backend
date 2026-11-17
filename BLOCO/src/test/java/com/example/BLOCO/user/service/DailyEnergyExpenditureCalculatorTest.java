package com.example.BLOCO.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.BLOCO.user.enums.ActivityLevel;

class DailyEnergyExpenditureCalculatorTest {

	@Test
	void appliesTheSelectedMaintenanceFactorToTheBasalMetabolicRate() {
		assertEquals(1920, DailyEnergyExpenditureCalculator.calculate(1600, ActivityLevel.SEDENTARY));
		assertEquals(2200, DailyEnergyExpenditureCalculator.calculate(1600, ActivityLevel.LIGHTLY_ACTIVE));
		assertEquals(2480, DailyEnergyExpenditureCalculator.calculate(1600, ActivityLevel.MODERATELY_ACTIVE));
		assertEquals(2760, DailyEnergyExpenditureCalculator.calculate(1600, ActivityLevel.VERY_ACTIVE));
		assertEquals(3040, DailyEnergyExpenditureCalculator.calculate(1600, ActivityLevel.EXTREMELY_ACTIVE));
	}
}
