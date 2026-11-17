package com.example.BLOCO.user.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ActivityLevelConverter implements AttributeConverter<ActivityLevel, Integer> {

	@Override
	public Integer convertToDatabaseColumn(ActivityLevel activityLevel) {
		return activityLevel == null ? null : activityLevel.getLevel();
	}

	@Override
	public ActivityLevel convertToEntityAttribute(Integer level) {
		return level == null ? null : ActivityLevel.fromLevel(level);
	}
}
