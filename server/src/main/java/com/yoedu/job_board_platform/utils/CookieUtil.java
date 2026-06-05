package com.yoedu.job_board_platform.utils;

import org.springframework.stereotype.Component;

import com.yoedu.job_board_platform.models.CookieName;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

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

    public void add(HttpServletResponse response, CookieName name, String value) {
        Cookie cookie = new Cookie(name.getValue(), value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void clear(HttpServletResponse response, CookieName name) {
        Cookie cookie = new Cookie(name.getValue(), null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
