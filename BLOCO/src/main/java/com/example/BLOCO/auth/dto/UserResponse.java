package com.example.BLOCO.auth.dto;

import java.util.UUID;

import com.example.BLOCO.user.entity.UserAccount;

public record UserResponse(UUID id, String name, String email, String phone) {

	public static UserResponse from(UserAccount user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
	}
}
