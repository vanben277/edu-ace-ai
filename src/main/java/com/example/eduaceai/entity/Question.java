package com.example.eduaceai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "TEXT")
    String content;

    String optionA;
    String optionB;
    String optionC;
    String optionD;
    String correctAnswer;

    @Column(columnDefinition = "TEXT")
    String explanation;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @JsonIgnore
    Quiz quiz;
}