package com.example.BLOCO.user.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.example.BLOCO.user.enums.ActivityLevel;
import com.example.BLOCO.user.enums.Gender;

public record UpdateUserProfileRequest(
	@NotNull(message = "O gênero é obrigatório.")
	Gender gender,
	@NotNull(message = "A idade é obrigatória.")
	@Min(value = 1, message = "A idade deve estar entre 1 e 120 anos.")
	@Max(value = 120, message = "A idade deve estar entre 1 e 120 anos.")
	Integer ageYears,
	@NotNull(message = "A altura é obrigatória.")
	@Valid
	HeightInput height,
	@NotNull(message = "O peso é obrigatório.")
	@Valid
	WeightInput weight,
	@NotNull(message = "O nível de atividade é obrigatório.")
	ActivityLevel activityLevel
) {

	public BigDecimal heightCm() {
		return height.toCentimeters();
	}

	public BigDecimal weightKg() {
		return weight.toKilograms();
	}

	public enum HeightUnit {
		CM,
		FT_IN
	}

	public enum WeightUnit {
		KG,
		LB
	}

	public record HeightInput(
		@NotNull(message = "A unidade da altura é obrigatória.")
		HeightUnit unit,
		@Digits(integer = 3, fraction = 2, message = "A altura deve ter no máximo duas casas decimais.")
		BigDecimal value,
		@Min(value = 1, message = "Os pés devem estar entre 1 e 8.")
		@Max(value = 8, message = "Os pés devem estar entre 1 e 8.")
		Integer feet,
		@Digits(integer = 2, fraction = 2, message = "As polegadas devem ter no máximo duas casas decimais.")
		BigDecimal inches
	) {

		@AssertTrue(message = "Informe a altura em cm ou em pés e polegadas, dentro dos limites aceitos.")
		public boolean isValidMeasurement() {
			if (unit == null) {
				return false;
			}
			return switch (unit) {
				case CM -> value != null
					&& feet == null
					&& inches == null
					&& between(value, new BigDecimal("50"), new BigDecimal("250"));
				case FT_IN -> value == null
					&& feet != null
					&& inches != null
					&& between(toCentimeters(), new BigDecimal("50"), new BigDecimal("250"))
					&& inches.compareTo(BigDecimal.ZERO) >= 0
					&& inches.compareTo(new BigDecimal("11.99")) <= 0;
			};
		}

		public BigDecimal toCentimeters() {
			if (unit == HeightUnit.CM) {
				return value.setScale(2, RoundingMode.HALF_UP);
			}
			return BigDecimal.valueOf(feet * 12L)
				.add(inches)
				.multiply(new BigDecimal("2.54"))
				.setScale(2, RoundingMode.HALF_UP);
		}
	}

	public record WeightInput(
		@NotNull(message = "A unidade do peso é obrigatória.")
		WeightUnit unit,
		@NotNull(message = "O peso é obrigatório.")
		@Digits(integer = 6, fraction = 2, message = "O peso deve ter no máximo duas casas decimais.")
		BigDecimal value
	) {

		@AssertTrue(message = "O peso deve estar entre 20 e 300 kg.")
		public boolean isValidMeasurement() {
			if (unit == null || value == null) {
				return false;
			}
			BigDecimal kg = toKilograms();
			return between(kg, new BigDecimal("20"), new BigDecimal("300"));
		}

		public BigDecimal toKilograms() {
			BigDecimal kg = unit == WeightUnit.KG
				? value
				: value.multiply(new BigDecimal("0.45359237"));
			return kg.setScale(2, RoundingMode.HALF_UP);
		}
	}

	private static boolean between(BigDecimal value, BigDecimal min, BigDecimal max) {
		return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
	}
}
