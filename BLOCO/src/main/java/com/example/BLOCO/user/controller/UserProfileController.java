package com.example.BLOCO.user.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BLOCO.user.dto.UpdateUserProfileRequest;
import com.example.BLOCO.user.dto.UserProfileResponse;
import com.example.BLOCO.user.service.UserProfileService;

@RestController
@RequestMapping("/api/v1/me/profile")
public class UserProfileController {

	private final UserProfileService userProfileService;

	public UserProfileController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@PutMapping
	public ResponseEntity<UserProfileResponse> updateProfile(
		@AuthenticationPrincipal Jwt jwt,
		@Valid @RequestBody UpdateUserProfileRequest request
	) {
		UserProfileResponse profile = userProfileService.updateProfile(
			UUID.fromString(jwt.getSubject()), request);
		return ResponseEntity.ok()
			.cacheControl(CacheControl.noStore())
			.body(profile);
	}
}
