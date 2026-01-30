package com.example.eduaceai.repository;

import com.example.eduaceai.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByUserStudentCodeAndDocumentIdOrderByCreatedAtAsc(String studentCode, Long documentId);
}