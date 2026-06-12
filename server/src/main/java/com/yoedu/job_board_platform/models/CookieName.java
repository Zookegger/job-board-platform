package com.yoedu.job_board_platform.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
/**
 * Tên các cookie được sử dụng trong ứng dụng.
 * ACCESS_TOKEN và REFRESH_TOKEN dùng để lưu token xác thực.
 */
public enum CookieName {
    ACCESS_TOKEN("accessToken"),
    REFRESH_TOKEN("refreshToken");

    private final String value;
}
