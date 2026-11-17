package com.example.BLOCO.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.BLOCO.user.dto.UpdateUserProfileRequest;
import com.example.BLOCO.user.dto.UserProfileResponse;
import com.example.BLOCO.user.entity.UserAccount;
import com.example.BLOCO.user.enums.ActivityLevel;
import com.example.BLOCO.user.enums.Gender;
import com.example.BLOCO.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

	@Mock
	private UserRepository userRepository;

	@Test
	void storesImperialMeasurementsInCanonicalUnits() {
		UserAccount user = UserAccount.create("Pessoa", "person@example.com", "hash", null);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		UserProfileService service = new UserProfileService(userRepository);
		UpdateUserProfileRequest request = new UpdateUserProfileRequest(
			Gender.FEMALE,
			29,
			new UpdateUserProfileRequest.HeightInput(
				UpdateUserProfileRequest.HeightUnit.FT_IN, null, 5, new BigDecimal("7")),
			new UpdateUserProfileRequest.WeightInput(
				UpdateUserProfileRequest.WeightUnit.LB, new BigDecimal("150")),
			ActivityLevel.MODERATELY_ACTIVE);

		UserProfileResponse response = service.updateProfile(user.getId(), request);

		assertEquals(new BigDecimal("170.18"), response.heightCm());
		assertEquals(new BigDecimal("68.04"), response.weightKg());
		assertEquals(29, response.ageYears());
		assertEquals(Gender.FEMALE, response.gender());
		assertEquals(ActivityLevel.MODERATELY_ACTIVE, response.activityLevel());
		assertEquals(1438, response.basalMetabolicRateKcal());
		assertEquals(2229, response.estimatedDailyEnergyExpenditureKcal());
		verify(userRepository).save(user);
	}
}
