package com.example.bankcards.repository;

import com.example.bankcards.entity.CardLockRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardLockRequestRepository extends JpaRepository<CardLockRequest, Long> {
}
