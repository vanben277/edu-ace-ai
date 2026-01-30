package com.example.eduaceai.dto.req;

import java.util.Map;

public record SubmitQuizRequest(
        Long quizId,
        Map<Long, String> answers
) {
}