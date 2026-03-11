package com.example.eduaceai.dto.req;

public record UserFilterForm(
        String search,
        String role,
        Boolean enabled,
        String sortBy,
        String sortDir
) {
}