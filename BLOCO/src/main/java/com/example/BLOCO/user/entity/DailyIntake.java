package com.example.BLOCO.user.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "daily_intakes", uniqueConstraints = {
	@UniqueConstraint(name = "uk_daily_intakes_user_date", columnNames = {"user_id", "record_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyIntake {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Column(name = "record_date", nullable = false)
	private LocalDate recordDate;

	@ColumnDefault("0")
	@Column(name = "calorie_intake_kcal", nullable = false)
	private Integer calorieIntakeKcal = 0;

	@ColumnDefault("0")
	@Column(name = "water_intake_ml", nullable = false)
	private Integer waterIntakeMl = 0;

	private DailyIntake(UserAccount user, LocalDate recordDate) {
		this.user = user;
		this.recordDate = recordDate;
	}

	public static DailyIntake create(UserAccount user, LocalDate recordDate) {
		return new DailyIntake(user, recordDate);
	}

	public void addCalories(int caloriesKcal) {
		if (caloriesKcal <= 0) {
			throw new IllegalArgumentException("Calories must be greater than zero");
		}
		calorieIntakeKcal = Math.addExact(calorieIntakeKcal, caloriesKcal);
	}

	public void addWater(int waterMl) {
		if (waterMl <= 0) {
			throw new IllegalArgumentException("Water amount must be greater than zero");
		}
		waterIntakeMl = Math.addExact(waterIntakeMl, waterMl);
	}
}
