package com.yoedu.job_board_platform.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.yoedu.job_board_platform.models.CookieName;
import com.yoedu.job_board_platform.services.AuthService;
import com.yoedu.job_board_platform.utils.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieLogoutHandler implements LogoutHandler {
    private final CookieUtil cookieUtil;
    private final AuthService authService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
            @Nullable Authentication authentication) {
        String refreshToken = cookieUtil.extract(request, CookieName.REFRESH_TOKEN);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
    }
}
