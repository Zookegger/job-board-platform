package com.yoedu.job_board_platform.repositories;

import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    long countByCreatedAtAfter(OffsetDateTime dateTime);

    List<User> findAllByRole(UserRole role);
}