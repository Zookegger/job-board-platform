package com.yoedu.job_board_platform.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
/**
 * Hằng số đường dẫn API.
 * Lưu prefix chung "/api" cho tất cả endpoint REST.
 */
public final class ApiPaths {
    public static final String BASE = "/api";
}
