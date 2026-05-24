package com.example.eduaceai.repository;

import com.example.eduaceai.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByUserStudentCodeOrderByCreatedAtDesc(String studentCode);

    boolean existsByUserStudentCodeAndName(String studentCode, String name);

    boolean existsByUserStudentCodeAndNameAndIdNot(String studentCode, String name, Long id);

    Optional<Subject> findByIdAndUserStudentCode(Long id, String studentCode);

}
