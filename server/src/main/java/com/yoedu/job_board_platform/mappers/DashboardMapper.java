package com.yoedu.job_board_platform.mappers;

import com.yoedu.job_board_platform.dtos.admin.AdminApplicationChartResponse.DailyApplicationPoint;
import com.yoedu.job_board_platform.dtos.admin.AdminApplicationChartResponse.StatusDistributionPoint;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import org.mapstruct.Mapper;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    default DailyApplicationPoint toDailyPoint(LocalDate date, long total) {
        return new DailyApplicationPoint(date, total);
    }

    default StatusDistributionPoint toStatusPoint(StatusApplicationCount row, long totalApplications) {
        return new StatusDistributionPoint(
                ApplicationStatus.valueOf(row.getStatus()),
                row.getTotal(),
                totalApplications == 0
                        ? 0
                        : Math.round((row.getTotal() * 10000.0) / totalApplications) / 100.0
        );
    }

    interface DailyApplicationCount {
        LocalDate getApplicationDate();
        long getTotal();
    }

    interface StatusApplicationCount {
        String getStatus();
        long getTotal();
    }
}
