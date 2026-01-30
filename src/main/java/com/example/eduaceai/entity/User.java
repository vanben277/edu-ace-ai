package com.example.eduaceai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String studentCode;

    @Column(nullable = false)
    String password;

    String fullName;

    @Enumerated(EnumType.STRING)
    Role role;

    @CreationTimestamp
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Document> documents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<QuizResult> quizResults;

    public enum Role {
        STUDENT, ADMIN
    }
}