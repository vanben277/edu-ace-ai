package com.example.eduaceai.dto.res;

public record UserAnswerResponse(
        Long questionId,
        String questionContent,
        String selectedOption,
        String correctAnswer,
        boolean isCorrect,
        String explanation
) {
}
