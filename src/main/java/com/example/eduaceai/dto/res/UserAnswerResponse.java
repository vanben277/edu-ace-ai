package com.example.eduaceai.dto.res;

public record UserAnswerResponse(
        Long questionId,
        String questionContent,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String selectedOption,
        String correctAnswer,
        boolean isCorrect,
        String explanation
) {
}
