package com.yoedu.job_board_platform.dtos.report;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Yêu cầu xử lý báo cáo từ admin (xem xét, bác bỏ, giải quyết).
 * reviewNotes là tuỳ chọn — có thể để trống.
 */
@Schema(description = "Yêu cầu xử lý báo cáo từ admin")
public record AdminReportActionRequest(
        @Schema(description = "Ghi chú xử lý (tuỳ chọn)", nullable = true)
        String reviewNotes
) {
}
