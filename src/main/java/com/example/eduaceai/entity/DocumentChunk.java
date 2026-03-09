package com.example.eduaceai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "document_chunks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    Document document;

    @Column(columnDefinition = "TEXT")
    String content;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String embeddingJson;
}