package com.booksorderingsystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 此DTO用於會員登入：
 * - POST /api/auth/login
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "帳號不可為空")
    private String username;

    @NotBlank(message = "密碼不可為空")
    private String password;
}
