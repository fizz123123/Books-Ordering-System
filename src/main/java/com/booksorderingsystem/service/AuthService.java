package com.booksorderingsystem.service;

import com.booksorderingsystem.dto.LoginRequest;
import com.booksorderingsystem.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
