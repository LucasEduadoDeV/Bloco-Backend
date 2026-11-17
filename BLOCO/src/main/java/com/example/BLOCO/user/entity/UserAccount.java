package com.example.BLOCO.user.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.ColumnDefault;

import com.example.BLOCO.user.enums.ActivityLevel;
import com.example.BLOCO.user.enums.ActivityLevelConverter;
import com.example.BLOCO.user.enums.Gender;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(length = 50)
	private String phone;

	@Column(name = "age", length = 3)
	private Integer age;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", length = 10)
	private Gender gender;

	@Column(name = "height_cm", precision = 5, scale = 2)
	private BigDecimal heightCm;

	@Column(name = "weight_kg", precision = 6, scale = 2)
	private BigDecimal weightKg;

	@Convert(converter = ActivityLevelConverter.class)
	@Column(name = "activity_level")
	private ActivityLevel activityLevel;

	@ColumnDefault("0")
	@Column(name = "basal_metabolic_rate_kcal", nullable = false)
	private Integer basalMetabolicRateKcal = 0;

	@ColumnDefault("0")
	@Column(name = "estimated_daily_energy_expenditure_kcal", nullable = false)
	private Integer estimatedDailyEnergyExpenditureKcal = 0;

	@ColumnDefault("0")
	@Column(name = "water_consumption_goal_ml", nullable = false)
	private Integer waterConsumptionGoalMl = 0;

	private UserAccount(UUID id, String name, String email, String passwordHash, String phone) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.phone = phone;
	}

	public static UserAccount create(String name, String email, String passwordHash, String phone) {
		return new UserAccount(UUID.randomUUID(), name, email, passwordHash, phone);
	}

	public void updateProfile(
		Gender gender,
		Integer age,
		BigDecimal heightCm,
		BigDecimal weightKg,
		ActivityLevel activityLevel,
		Integer basalMetabolicRateKcal,
		Integer estimatedDailyEnergyExpenditureKcal
	) {
		this.gender = gender;
		this.age = age;
		this.heightCm = heightCm;
		this.weightKg = weightKg;
		this.activityLevel = activityLevel;
		this.basalMetabolicRateKcal = basalMetabolicRateKcal;
		this.estimatedDailyEnergyExpenditureKcal = estimatedDailyEnergyExpenditureKcal;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

    public UUID getId() {
        return id;
    }

    public Gender getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public Integer getBasalMetabolicRateKcal() {
        return basalMetabolicRateKcal;
    }

    public Integer getEstimatedDailyEnergyExpenditureKcal() {
        return estimatedDailyEnergyExpenditureKcal;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getWaterConsumptionGoalMl() {
        return waterConsumptionGoalMl;
    }
}
