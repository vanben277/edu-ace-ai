package com.example.eduaceai.repository;

import com.example.eduaceai.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Modifying
    @Query(value = "DELETE FROM quiz_source_documents WHERE document_id = :docId", nativeQuery = true)
    void deleteSourceDocumentLinksForDocument(@Param("docId") Long docId);
}
