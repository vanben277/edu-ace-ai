package com.example.eduaceai.repository;

import com.example.eduaceai.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserStudentCode(String studentCode);

    long countByUserStudentCode(String studentCode);

    Optional<Document> findByIdAndUserStudentCode(Long id, String studentCode);
}