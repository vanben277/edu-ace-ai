package com.example.eduaceai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String fileName;
    String fileType;
    Long fileSize;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String content;

    @CreationTimestamp
    LocalDateTime createdAt;

}