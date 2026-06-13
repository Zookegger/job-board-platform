package com.yoedu.job_board_platform.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
/**
 * Cấu hình bảo mật Spring Security.
 * Thiết lập CORS, CSRF (tắt), session stateless, filter JWT,
 * và phân quyền cho các endpoint.
 */
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		var origins = List.of(
				"http://localhost:5173", // Vite dev server
				"http://localhost:3000", // Docker nginx
				"http://localhost:8080", // direct API access
				"http://localhost:5000"  // direct API access
		);

		config.setAllowedOrigins(origins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/login", "/api/auth/register/company",
								"/api/auth/register/candidate", "/api/auth/refresh-token", "/api/auth/logout")
						.permitAll()
						.requestMatchers("/api/public/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/company").permitAll()
						.requestMatchers("/api/company/job-post").permitAll()
						.requestMatchers("/uploads/**", "/api/profile/resume/preview").permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**",
								"/v3/api-docs", "/v3/api-docs/**")
						.permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class)
				.httpBasic(basic -> basic.disable()).formLogin(form -> form.disable())
				.exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, authException) -> {
					res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}));
		return http.build();
	}

}
