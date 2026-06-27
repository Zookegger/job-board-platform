package com.yoedu.job_board_platform.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.yoedu.job_board_platform.dtos.application.ApplicationTimelineResponse;
import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.ApplicationStatusLog;
import com.yoedu.job_board_platform.models.User;

@Mapper(componentModel = "spring")
public interface ApplicationStatusLogMapper {

    @Mapping(target = "changedByName", expression = "java(resolveChangedByName(log))")
    @Mapping(target = "statusLabel", expression = "java(resolveStatusLabel(log))")
    ApplicationTimelineResponse toTimelineResponse(ApplicationStatusLog log);

    List<ApplicationTimelineResponse> toTimelineResponseList(List<ApplicationStatusLog> logs);

    default ApplicationStatusLog createLog(Application application, ApplicationStatus status, User changedBy, String note) {
        return ApplicationStatusLog.builder()
                .application(application)
                .status(status)
                .changedBy(changedBy)
                .note(note)
                .build();
    }

    default String resolveChangedByName(ApplicationStatusLog log) {
        if (log.getChangedBy() == null || log.getChangedBy().getProfile() == null) {
            return "Hệ thống";
        }
        String fullName = log.getChangedBy().getProfile().getFullName();
        return fullName != null ? fullName : "Hệ thống";
    }

    default String resolveStatusLabel(ApplicationStatusLog log) {
        if (log.getStatus() == null) return "";
        return switch (log.getStatus()) {
            case PENDING -> "Chờ xử lý";
            case REVIEWING -> "Đang xem xét";
            case INTERVIEW -> "Phỏng vấn";
            case HIRED -> "Đã tuyển";
            case REJECTED -> "Từ chối";
            case WITHDRAWN -> "Đã rút đơn";
        };
    }
}
