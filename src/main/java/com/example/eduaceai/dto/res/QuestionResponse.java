package com.example.eduaceai.dto.res;

public record QuestionResponse(
        Long id,
        String content,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAnswer,
        String explanation
) {}
