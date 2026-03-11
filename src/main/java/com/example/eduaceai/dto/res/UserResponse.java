package com.example.eduaceai.dto.res;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String studentCode,
        String fullName,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {
}