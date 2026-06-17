package com.booksorderingsystem.dto;

import com.booksorderingsystem.entity.Role;
import lombok.*;

/**
 * 此DOT用於回傳登入後的使用者基本資料，去掉password等敏感資訊
 * {
 * "userId": 1,
 * "username": "admin",
 * "role": "ADMIN"
 * }
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long userId;
    private String username;
    private Role role;
}
