package com.sarth.walletsim.dto;

import com.sarth.walletsim.constants.UserRole;
import com.sarth.walletsim.entity.AppUser;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String fullName;
    private String email;
    private UserRole role;

    public static AuthResponse from(AppUser user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
