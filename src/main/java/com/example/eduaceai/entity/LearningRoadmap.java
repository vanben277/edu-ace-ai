package com.example.eduaceai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_roadmaps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningRoadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "quiz_result_id", unique = true)
    @JsonIgnore
    QuizResult quizResult;

    @Column(columnDefinition = "LONGTEXT")
    String contentJson;

    @Column(length = 64)
    String servedByTier;

    @CreationTimestamp
    LocalDateTime createdAt;
}
