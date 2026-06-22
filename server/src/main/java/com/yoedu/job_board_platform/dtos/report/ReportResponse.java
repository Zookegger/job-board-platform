package com.yoedu.job_board_platform.dtos.report;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.yoedu.job_board_platform.models.ReportStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Phản hồi thông tin báo cáo vi phạm.
 * Được sử dụng cho cả phản hồi public (sau khi tạo báo cáo) và admin (khi xem
 * danh sách báo cáo).
 */
@Schema(description = "Thông tin báo cáo vi phạm")
public record ReportResponse(
                @Schema(description = "ID báo cáo") UUID id,

                @Schema(description = "ID bài tuyển dụng bị báo cáo") UUID jobId,

                @Schema(description = "Tiêu đề bài tuyển dụng bị báo cáo") String jobTitle,

                @Schema(description = "ID công ty bị báo cáo") UUID companyId,

                @Schema(description = "Tên công ty bị báo cáo") String companyName,

                @Schema(description = "ID người báo cáo") UUID reportedById,

                @Schema(description = "Tên người báo cáo") String reportedByName,

                @Schema(description = "Lý do báo cáo") String reason,

                @Schema(description = "Chi tiết báo cáo") String details,

                @Schema(description = "Chi tiết duyệt") String reviewNotes,

                @Schema(description = "Trạng thái xử lý báo cáo") ReportStatus status,

                @Schema(description = "ID người xử lý") UUID reviewedById,

                @Schema(description = "Tên người xử lý") String reviewedByName,

                @Schema(description = "Thời điểm xử lý") OffsetDateTime reviewedAt,

                @Schema(description = "Thời điểm tạo") OffsetDateTime createdAt) {
}
