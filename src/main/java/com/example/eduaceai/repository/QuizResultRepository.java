package com.example.eduaceai.repository;

import com.example.eduaceai.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserStudentCode(String studentCode);

    long countByUserStudentCode(String studentCode);
}
