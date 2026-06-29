package com.yoedu.job_board_platform.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	@Value("${app.cors.allowed-origins}")
	private List<String> configuredOrigins;

	private static final String[] PUBLIC_WHITELIST = {
			"/api/auth/login",
			"/api/auth/register/company",
			"/api/auth/register/candidate",
			"/api/auth/refresh-token",
			"/api/auth/logout",
			"/uploads/**",
			"/api/company", "/api/company/job-post",
			"/api/jobs/public/**",
			"/api/jobs/*/related",
			"/api/companies/public/**",
			"/api/skills/**",
			"/api/categories"
	};

	private static final String[] SWAGGER_WHITELIST = {
			"/swagger-ui.html",
			"/swagger-ui/**",
			"/api-docs",
			"/api-docs/**",
			"/v3/api-docs",
			"/v3/api-docs/**"
	};

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		List<String> origins = new ArrayList<>(List.of(
				"http://localhost:5173", // Vite dev server
				"http://localhost:3000", // Docker nginx
				"http://localhost:8080", // direct API access
				"http://localhost:5000" // direct API access
		));

		if (configuredOrigins != null && !configuredOrigins.isEmpty()) {
			origins.addAll(configuredOrigins);
		}

		config.setAllowedOrigins(origins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) {
		http.csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_WHITELIST).permitAll()
						.requestMatchers(SWAGGER_WHITELIST).permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class)
				.httpBasic(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
				.exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, authException) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
						.accessDeniedHandler((req, res, accessDeniedException) -> res.sendError(HttpServletResponse.SC_FORBIDDEN)));
		return http.build();
	}

}
