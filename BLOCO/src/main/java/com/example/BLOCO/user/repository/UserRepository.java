package com.example.BLOCO.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BLOCO.user.entity.UserAccount;

public interface UserRepository extends JpaRepository<UserAccount, UUID> {

	boolean existsByEmail(String email);

	Optional<UserAccount> findByEmail(String email);
}
