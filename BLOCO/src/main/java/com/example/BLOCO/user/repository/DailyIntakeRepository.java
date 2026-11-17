package com.example.BLOCO.user.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BLOCO.user.entity.DailyIntake;

public interface DailyIntakeRepository extends JpaRepository<DailyIntake, UUID> {

	Optional<DailyIntake> findByUser_IdAndRecordDate(UUID userId, LocalDate recordDate);
}
