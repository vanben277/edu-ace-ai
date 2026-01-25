package com.example.eduaceai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;

    @ManyToOne
    @JoinColumn(name = "document_id")
    Document document;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    List<Question> questions;

    @CreationTimestamp
    LocalDateTime createdAt;
}