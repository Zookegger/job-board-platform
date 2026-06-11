package com.yoedu.job_board_platform.utils;

import org.springframework.stereotype.Component;

import com.yoedu.job_board_platform.models.CookieName;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
/**
 * Tiện ích thao tác với cookie HTTP.
 * Hỗ trợ trích xuất, thêm và xóa cookie một cách an toàn (HttpOnly).
 */
public class CookieUtil {

    /**
     * Trích xuất giá trị cookie từ request.
     *
     * @param request yêu cầu HTTP
     * @param name    tên cookie cần lấy
     * @return giá trị cookie, hoặc {@code null} nếu không tìm thấy
     */
    public String extract(HttpServletRequest request, CookieName name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        for (Cookie c : cookies) {
            if (name.getValue().equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    /**
     * Thêm cookie vào response.
     *
     * @param response phản hồi HTTP
     * @param name     tên cookie
     * @param value    giá trị cookie
     */
    public void add(HttpServletResponse response, CookieName name, String value) {
        Cookie cookie = new Cookie(name.getValue(), value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Xóa cookie khỏi trình duyệt bằng cách đặt maxAge = 0.
     *
     * @param response phản hồi HTTP
     * @param name     tên cookie cần xóa
     */
    public void clear(HttpServletResponse response, CookieName name) {
        Cookie cookie = new Cookie(name.getValue(), null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
