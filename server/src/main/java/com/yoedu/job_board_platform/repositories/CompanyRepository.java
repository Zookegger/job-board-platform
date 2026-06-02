package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
}
