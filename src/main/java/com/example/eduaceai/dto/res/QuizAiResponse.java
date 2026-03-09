package com.example.eduaceai.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class QuizAiResponse {
    private List<QuestionAiResponse> questions;
}