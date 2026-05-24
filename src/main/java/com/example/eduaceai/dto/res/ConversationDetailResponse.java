package com.example.eduaceai.dto.res;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationDetailResponse(
        Long id,
        String title,
        Long subjectId,
        List<Long> sourceDocumentIds,
        List<String> sourceDocumentNames,
        List<ConversationMessageResponse> messages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
