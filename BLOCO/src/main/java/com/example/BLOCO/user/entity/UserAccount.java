package com.example.BLOCO.user.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
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

	public String getPasswordHash() {
		return passwordHash;
	}
}
