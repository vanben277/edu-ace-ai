package com.example.eduaceai.repository;

import com.example.eduaceai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserStudentCodeOrderByUpdatedAtDesc(String studentCode);

    List<Conversation> findByUserStudentCodeAndSubjectIdOrderByUpdatedAtDesc(String studentCode, Long subjectId);

    Optional<Conversation> findByIdAndUserStudentCode(Long id, String studentCode);

    @Modifying
    @Query("UPDATE Conversation c SET c.subject = null WHERE c.subject.id = :subjectId")
    void detachFromSubject(@Param("subjectId") Long subjectId);
}
