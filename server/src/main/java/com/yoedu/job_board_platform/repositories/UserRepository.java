package com.yoedu.job_board_platform.repositories;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    long countByCreatedAtAfter(OffsetDateTime dateTime);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByIsActive(boolean isActive, Pageable pageable);

    Page<User> findByRoleAndIsActive(UserRole role, boolean isActive, Pageable pageable);
}