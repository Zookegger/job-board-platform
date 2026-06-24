package com.yoedu.job_board_platform.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.yoedu.job_board_platform.dtos.report.CreateReportRequest;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.Report;
import com.yoedu.job_board_platform.models.User;

/**
 * MapStruct mapper cho entity Report.
 * Chuyển đổi giữa CreateReportRequest -> Report entity và Report entity ->
 * ReportResponse.
 */
@Mapper(componentModel = "spring")
public interface ReportMapper {

    /**
     * Chuyển đổi CreateReportRequest thành Report entity.
     * Các trường tự động sinh (id, status, createdAt) và trường xử lý (reviewedBy,
     * reviewedAt) được bỏ qua.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewNotes", ignore = true)
    @Mapping(target = "job", source = "job")
    @Mapping(target = "company", source = "company")
    @Mapping(target = "reportedBy", source = "reportedBy")
    Report toEntity(CreateReportRequest request, User reportedBy, Job job, Company company);

    /**
     * Chuyển đổi Report entity thành ReportResponse DTO.
     * Ánh xạ các trường từ entity liên quan (Job, Company, User).
     */
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.companyName")
    @Mapping(target = "reportedById", source = "reportedBy.id")
    @Mapping(target = "reportedByName", source = "reportedBy.profile.fullName")
    @Mapping(target = "reviewedById", source = "reviewedBy.id")
    @Mapping(target = "reviewedByName", source = "reviewedBy.profile.fullName")
    ReportResponse toResponse(Report report);
}
