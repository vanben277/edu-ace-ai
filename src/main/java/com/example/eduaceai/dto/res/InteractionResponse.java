package com.example.eduaceai.dto.res;

import java.time.LocalDateTime;

public record InteractionResponse(
        Long id,
        String question,
        String answer,
        LocalDateTime createdAt
) {
}