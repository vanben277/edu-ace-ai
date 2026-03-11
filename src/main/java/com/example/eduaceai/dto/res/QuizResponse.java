package com.example.eduaceai.dto.res;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResponse(
        Long id,
        String title,
        Long documentId,
        List<QuestionResponse> questions,
        LocalDateTime createdAt
) {}
