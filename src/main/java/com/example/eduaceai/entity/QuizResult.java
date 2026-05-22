package com.example.eduaceai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    Quiz quiz;

    Integer totalQuestions;
    Integer correctAnswers;
    Double score;

    @CreationTimestamp
    LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "quizResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswers;

    // Bidirectional side để JPA cascade-delete LearningRoadmap khi QuizResult bị xoá.
    // Thiếu cái này → khi cascade từ Document → Quiz → QuizResult, MySQL chặn vì FK
    // learning_roadmaps.quiz_result_id còn trỏ vào row đang bị xoá.
    @OneToOne(mappedBy = "quizResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private LearningRoadmap learningRoadmap;
}