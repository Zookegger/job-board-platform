package com.yoedu.job_board_platform.security;

public final class AuthorizationConstants {
    public static final String PUBLIC = "permitAll()";
    public static final String EMPLOYER = "hasRole('EMPLOYER')";
    public static final String CANDIDATE = "hasRole('CANDIDATE')";
    public static final String ADMIN = "hasRole('ADMIN')";
    public static final String CANDIDATE_OR_EMPLOYER = "hasAnyRole('CANDIDATE', 'EMPLOYER')";

    private AuthorizationConstants() {}
}
