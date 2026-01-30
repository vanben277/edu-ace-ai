package com.example.eduaceai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    User user;

    @ManyToOne
    @JoinColumn(name = "document_id")
    @JsonIgnore
    Document document;

    @Column(columnDefinition = "TEXT")
    String question;

    @Column(columnDefinition = "LONGTEXT")
    String answer;

    @CreationTimestamp
    LocalDateTime createdAt;
}