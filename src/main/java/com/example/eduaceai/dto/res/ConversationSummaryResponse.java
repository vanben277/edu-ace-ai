package com.example.eduaceai.dto.res;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationSummaryResponse(
        Long id,
        String title,
        Long subjectId,
        List<Long> sourceDocumentIds,
        int messageCount,
        String lastMessagePreview,
        LocalDateTime updatedAt
) {
}
