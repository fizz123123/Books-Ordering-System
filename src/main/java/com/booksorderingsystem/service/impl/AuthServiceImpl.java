package com.booksorderingsystem.service.impl;

import com.booksorderingsystem.dto.LoginRequest;
import com.booksorderingsystem.dto.LoginResponse;
import com.booksorderingsystem.entity.User;
import com.booksorderingsystem.repository.UserRepository;
import com.booksorderingsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new IllegalArgumentException("Email錯誤"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("密碼錯誤");
        }

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

   //===== Helper methods =====

    private void validateLoginRequest(LoginRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("Email不可為空");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("密碼不可為空");
        }
    }
}
