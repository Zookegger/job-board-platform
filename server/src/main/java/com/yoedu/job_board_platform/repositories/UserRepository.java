package com.yoedu.job_board_platform.repositories;

import java.util.Optional;
import java.util.UUID;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    long countByCreatedAtAfter(OffsetDateTime dateTime);
}
