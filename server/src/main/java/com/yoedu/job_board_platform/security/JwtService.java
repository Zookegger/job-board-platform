package com.yoedu.job_board_platform.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long jwtExpirationInMs; // 1 giờ

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-millis}") long jwtExpirationInMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    /**
     * Phương thức generateToken sẽ tạo ra một JWT token mới dựa trên thông tin của người dùng (UserDetails). Token này sẽ chứa tên người dùng và quyền hạn của người dùng, cùng với thời gian tạo và thời gian hết hạn của token. Token được ký bằng secret key để đảm bảo tính toàn vẹn và bảo mật.
     * Dùng thuật toán HS256 (HMAC với SHA-256) để ký token, đảm bảo rằng token không thể bị giả mạo hoặc thay đổi mà không có secret key.
     * @param userDetails
     * @return
     */
    public long getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder().subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().stream().findFirst().orElseThrow().getAuthority())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Phương thức extractUsername sẽ giải mã token và lấy tên người dùng (username) từ phần payload của token. Phần payload chứa thông tin về người dùng, bao gồm cả tên người dùng và quyền hạn.
     * @param token JWT token cần giải mã để lấy tên người dùng.
     * @return Tên người dùng (username) được trích xuất từ token. Nếu token không hợp lệ hoặc không chứa thông tin tên người dùng, phương thức có thể trả về null hoặc ném ra một ngoại lệ tùy thuộc vào cách bạn xử lý lỗi trong ứng dụng của mình.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Phương thức validateToken sẽ kiểm tra tính hợp lệ của token bằng cách so sánh tên người dùng trong token với tên người dùng trong UserDetails và kiểm tra xem token đã hết hạn hay chưa.
     * @param token JWT token cần kiểm tra.
     * @param userDetails Thông tin chi tiết về người dùng, bao gồm tên người dùng và quyền hạn.
     * @return true nếu token hợp lệ và khớp với thông tin người dùng, false nếu token không hợp lệ hoặc đã hết hạn.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Kiểm tra xem token đã hết hạn hay chưa bằng cách so sánh thời gian hết hạn trong token với thời gian hiện tại.
     * @param token JWT token cần kiểm tra.
     * @return true nếu token đã hết hạn, false nếu token còn hiệu lực.
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Lấy tất cả thông tin từ token, bao gồm cả phần payload (claims) và phần header.
     * @param token JWT token cần giải mã.
     * @return Claims chứa thông tin từ token, bao gồm cả phần payload và phần header.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}

