package com.example.eduaceai.dto.res;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResultResponse(
        Long id,
        String quizTitle,
        Integer totalQuestions,
        Integer correctAnswers,
        Double score,
        LocalDateTime completedAt,
        List<UserAnswerResponse> answers
) {
}
