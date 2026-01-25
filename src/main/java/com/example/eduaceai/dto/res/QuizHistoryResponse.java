package com.example.eduaceai.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizHistoryResponse {
    private Long id;
    private String quizTitle;
    private Double score;
    private Integer correctAnswers;
    private Integer totalQuestions;
    private LocalDateTime completedAt;
}