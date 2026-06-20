package com.booksorderingsystem.service;

import com.booksorderingsystem.dto.user.LoginRequest;
import com.booksorderingsystem.dto.user.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
