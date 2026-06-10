package com.yoedu.job_board_platform.dtos.profile;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResumeResponse(
        @Schema(description = "ID resume", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Tiêu đề", example = "Nguyễn Văn A - Software Engineer")
        String title,

        @Schema(description = "Tên file gốc", example = "cv_2026.pdf")
        String originalFileName,

        @Schema(description = "Dung lượng file (bytes)", example = "204800")
        long fileSize,

        @Schema(description = "Loại file", example = "application/pdf")
        String fileType,

        @Schema(description = "Ngày tạo", example = "2026-06-10T10:30:00Z")
        OffsetDateTime createdAt,

        @Schema(description = "Ngày cập nhật", example = "2026-06-10T10:30:00Z")
        OffsetDateTime updatedAt
) {
}
