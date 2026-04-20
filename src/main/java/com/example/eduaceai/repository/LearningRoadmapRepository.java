package com.example.eduaceai.repository;

import com.example.eduaceai.entity.LearningRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningRoadmapRepository extends JpaRepository<LearningRoadmap, Long> {
    Optional<LearningRoadmap> findByQuizResultId(Long quizResultId);
}
