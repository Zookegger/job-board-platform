package com.yoedu.job_board_platform.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Tải thông tin người dùng dựa trên email để phục vụ cho quá trình xác thực JWT. Nếu không tìm thấy người dùng, ném ra UsernameNotFoundException.
     *
     * @param email email của người dùng
     * @return UserDetails thông tin người dùng
     * @throws UsernameNotFoundException nếu không tìm thấy người dùng
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        return org.springframework.security.core.userdetails.User.builder().username(appUser.getEmail())
            .password(appUser.getPassword()).authorities("ROLE_" + appUser.getRole().name()).build();
    }

}