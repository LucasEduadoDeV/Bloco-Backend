package com.example.BLOCO.user.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DailyIntakeTest {

	@Test
	void recordsCaloriesAndWaterForOneDate() {
		UserAccount user = UserAccount.create("Pessoa", "person@example.com", "hash", null);
		DailyIntake intake = DailyIntake.create(user, LocalDate.of(2026, 11, 16));

		intake.addCalories(120);
		intake.addCalories(80);
		intake.addWater(300);
		intake.addWater(200);

		assertEquals(200, intake.getCalorieIntakeKcal());
		assertEquals(500, intake.getWaterIntakeMl());
		assertEquals(LocalDate.of(2026, 11, 16), intake.getRecordDate());
	}

	@Test
	void rejectsNonPositiveIntake() {
		UserAccount user = UserAccount.create("Pessoa", "person@example.com", "hash", null);
		DailyIntake intake = DailyIntake.create(user, LocalDate.now());

		assertThrows(IllegalArgumentException.class, () -> intake.addCalories(0));
		assertThrows(IllegalArgumentException.class, () -> intake.addWater(-1));
	}
}
