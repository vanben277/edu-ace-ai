package com.example.eduaceai.dto.req;

public record GenerateQuizRequest(
        Long documentId,
        int numberOfQuestions
) {}
