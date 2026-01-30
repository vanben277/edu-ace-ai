package com.example.eduaceai.dto.req;

public record RegisterRequest(
        String studentCode,
        String password,
        String fullName
) {
}
