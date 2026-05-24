package com.example.eduaceai.dto.res;

import java.time.LocalDateTime;

public record ConversationMessageResponse(
        Long id,
        String role,
        String content,
        LocalDateTime createdAt
) {
}
