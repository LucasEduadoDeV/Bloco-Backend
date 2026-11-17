package com.example.BLOCO.user.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ActivityLevelConverterTest {

	private final ActivityLevelConverter converter = new ActivityLevelConverter();

	@Test
	void convertsActivityLevelsToStableNumericCodes() {
		assertEquals(1, converter.convertToDatabaseColumn(ActivityLevel.SEDENTARY));
		assertEquals(2, converter.convertToDatabaseColumn(ActivityLevel.LIGHTLY_ACTIVE));
		assertEquals(3, converter.convertToDatabaseColumn(ActivityLevel.MODERATELY_ACTIVE));
		assertEquals(4, converter.convertToDatabaseColumn(ActivityLevel.VERY_ACTIVE));
		assertEquals(5, converter.convertToDatabaseColumn(ActivityLevel.EXTREMELY_ACTIVE));
	}

	@Test
	void convertsNumericCodesToActivityLevels() {
		assertEquals(ActivityLevel.SEDENTARY, converter.convertToEntityAttribute(1));
		assertEquals(ActivityLevel.LIGHTLY_ACTIVE, converter.convertToEntityAttribute(2));
		assertEquals(ActivityLevel.MODERATELY_ACTIVE, converter.convertToEntityAttribute(3));
		assertEquals(ActivityLevel.VERY_ACTIVE, converter.convertToEntityAttribute(4));
		assertEquals(ActivityLevel.EXTREMELY_ACTIVE, converter.convertToEntityAttribute(5));
	}

	@Test
	void preservesNullAndRejectsUnknownCodes() {
		assertNull(converter.convertToDatabaseColumn(null));
		assertNull(converter.convertToEntityAttribute(null));
		assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(0));
		assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(6));
	}
}
