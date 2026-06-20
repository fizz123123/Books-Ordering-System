package com.booksorderingsystem.controller;

import com.booksorderingsystem.dto.user.LoginRequest;
import com.booksorderingsystem.dto.user.LoginResponse;
import com.booksorderingsystem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    public static final String LOGIN_USER_SESSION_KEY = "loginUser";

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse loginResponse = authService.login(request);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LOGIN_USER_SESSION_KEY, loginResponse);

        return loginResponse;
    }

    @GetMapping("/me")
    public LoginResponse getCurrentUser(HttpServletRequest request) {
        return getLoginUserFromSession(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    // ===== Helper methods =====

    private LoginResponse getLoginUserFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請先登入");
        }

        Object loginUser = session.getAttribute(LOGIN_USER_SESSION_KEY);

        if (!(loginUser instanceof LoginResponse loginResponse)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請先登入");
        }
        return loginResponse;
    }


}
