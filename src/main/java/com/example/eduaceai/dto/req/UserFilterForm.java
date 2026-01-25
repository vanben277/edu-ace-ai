package com.example.eduaceai.dto.req;

public record UserFilterForm(
        String search,
        String role,
        String sortBy,
        String sortDir
) {}