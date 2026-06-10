package com.yoedu.job_board_platform.mappers;

import org.mapstruct.Mapper;

import com.yoedu.job_board_platform.dtos.profile.ResumeResponse;
import com.yoedu.job_board_platform.models.Resume;

@Mapper(componentModel = "spring")
public interface ResumeMapper {
    ResumeResponse toResponse(Resume resume);
}
