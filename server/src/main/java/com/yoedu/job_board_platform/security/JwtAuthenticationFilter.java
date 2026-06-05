package com.yoedu.job_board_platform.security;

import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filter xác thực JWT được thực thi một lần cho mỗi HTTP request.
 *
 * <p>
 * Nhiệm vụ:
 * </p>
 * <ul>
 * <li>Đọc access token từ cookie.</li>
 * <li>Giải mã token để lấy thông tin người dùng.</li>
 * <li>Kiểm tra tính hợp lệ của token.</li>
 * <li>Thiết lập thông tin xác thực vào SecurityContext.</li>
 * </ul>
 *
 * <p>
 * Sau khi xác thực thành công, Spring Security sẽ xem request hiện tại
 * là đã đăng nhập và cho phép truy cập các endpoint được bảo vệ.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userService;

    /**
     * Xử lý xác thực JWT cho mỗi request đi qua Security Filter Chain.
     *
     * @param request     HTTP request từ client
     * @param response    HTTP response trả về client
     * @param filterChain chuỗi filter tiếp theo trong Spring Security
     * @throws ServletException lỗi servlet
     * @throws IOException      lỗi I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        String token = null;
        
        // Kiểm tra cookies không null trước khi stream
        if (cookies != null) {
            token = Stream.of(cookies)
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        try {
            if (token != null) {
                String email = jwtService.extractUsername(token);

                // Chỉ xác thực khi:
                // 1. Email tồn tại trong token.
                // 2. Chưa có Authentication trong SecurityContext.
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userService.loadUserByUsername(email);

                    if (jwtService.validateToken(token, userDetails)) {
                        // Tạo đối tượng Authentication chứa:
                        // - Thông tin người dùng
                        // - Danh sách quyền (authorities)
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                                null, userDetails.getAuthorities());

                        // Gắn thêm thông tin request hiện tại (IP, session, ...)
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Lưu Authentication vào SecurityContext. Từ đây Spring Security xem request đã đăng nhập.
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

}
