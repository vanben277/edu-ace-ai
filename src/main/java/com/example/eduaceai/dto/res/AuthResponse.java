package com.example.eduaceai.dto.res;

public record AuthResponse(
        String token,
        String studentCode,
        String fullName,
        String role
) {}
