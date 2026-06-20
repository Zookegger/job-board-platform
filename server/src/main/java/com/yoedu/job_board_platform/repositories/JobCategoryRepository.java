package com.yoedu.job_board_platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.JobCategory;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Integer> {
    java.util.Optional<JobCategory> findByName(String name);
}
