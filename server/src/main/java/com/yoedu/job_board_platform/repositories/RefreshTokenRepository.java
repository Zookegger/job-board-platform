package com.yoedu.job_board_platform.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.RefreshToken;

import jakarta.transaction.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenString(String tokenString);

    List<RefreshToken> findByUserId(UUID userId);

    @Transactional
    void deleteByExpiresAtBefore(OffsetDateTime now); // now > expiresAt
}