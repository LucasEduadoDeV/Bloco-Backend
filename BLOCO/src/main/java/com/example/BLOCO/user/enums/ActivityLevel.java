package com.example.BLOCO.user.enums;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ActivityLevel {

	SEDENTARY(1, "1.20"),
	LIGHTLY_ACTIVE(2, "1.375"),
	MODERATELY_ACTIVE(3, "1.55"),
	VERY_ACTIVE(4, "1.725"),
	EXTREMELY_ACTIVE(5, "1.90");

	private final int level;
	private final BigDecimal maintenanceFactor;

	ActivityLevel(int level, String maintenanceFactor) {
		this.level = level;
		this.maintenanceFactor = new BigDecimal(maintenanceFactor);
	}

	@JsonValue
	public int getLevel() {
		return level;
	}

	public BigDecimal getMaintenanceFactor() {
		return maintenanceFactor;
	}

	@JsonCreator
	public static ActivityLevel fromLevel(int level) {
		for (ActivityLevel activityLevel : values()) {
			if (activityLevel.level == level) {
				return activityLevel;
			}
		}
		throw new IllegalArgumentException("Invalid activity level: " + level);
	}
}
