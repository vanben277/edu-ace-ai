package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.SubjectRequest;
import com.example.eduaceai.dto.res.SubjectResponse;
import com.example.eduaceai.entity.Document;
import com.example.eduaceai.entity.Subject;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.exception.NotFoundException;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.SubjectRepository;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.service.ISubjectService;
import com.example.eduaceai.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements ISubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @Override
    @Transactional
    public SubjectResponse create(SubjectRequest req) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        User user = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại", ErrorCodeConstant.USER_NOT_FOUND));

        String trimmedName = req.getName().trim();
        if (subjectRepository.existsByUserStudentCodeAndName(studentCode, trimmedName)) {
            throw new BusinessException("Bạn đã có một môn học cùng tên", ErrorCodeConstant.SUBJECT_NAME_DUPLICATE);
        }

        Subject subject = Subject.builder()
                .name(trimmedName)
                .description(req.getDescription())
                .color(req.getColor())
                .user(user)
                .build();

        Subject saved = subjectRepository.save(subject);
        return toResponse(saved, 0L);
    }

    @Override
    public List<SubjectResponse> listMine() {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        List<Subject> subjects = subjectRepository.findByUserStudentCodeOrderByCreatedAtDesc(studentCode);
        return subjects.stream()
                .map(s -> toResponse(s, documentRepository.countBySubjectId(s.getId())))
                .toList();
    }

    @Override
    public SubjectResponse getById(Long id) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        Subject subject = subjectRepository.findByIdAndUserStudentCode(id, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy môn học", ErrorCodeConstant.SUBJECT_NOT_FOUND));
        long count = documentRepository.countBySubjectId(subject.getId());
        return toResponse(subject, count);
    }

    @Override
    @Transactional
    public SubjectResponse update(Long id, SubjectRequest req) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        Subject subject = subjectRepository.findByIdAndUserStudentCode(id, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy môn học", ErrorCodeConstant.SUBJECT_NOT_FOUND));

        String trimmedName = req.getName().trim();
        if (!subject.getName().equals(trimmedName)
                && subjectRepository.existsByUserStudentCodeAndNameAndIdNot(studentCode, trimmedName, id)) {
            throw new BusinessException("Bạn đã có một môn học cùng tên", ErrorCodeConstant.SUBJECT_NAME_DUPLICATE);
        }

        subject.setName(trimmedName);
        subject.setDescription(req.getDescription());
        subject.setColor(req.getColor());

        Subject saved = subjectRepository.save(subject);
        long count = documentRepository.countBySubjectId(saved.getId());
        return toResponse(saved, count);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        Subject subject = subjectRepository.findByIdAndUserStudentCode(id, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy môn học", ErrorCodeConstant.SUBJECT_NOT_FOUND));

        List<Document> docs = documentRepository.findBySubjectId(subject.getId());
        for (Document doc : docs) {
            doc.setSubject(null);
        }
        if (!docs.isEmpty()) {
            documentRepository.saveAll(docs);
        }

        subjectRepository.delete(subject);
    }

    private SubjectResponse toResponse(Subject s, long documentCount) {
        return SubjectResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .color(s.getColor())
                .documentCount((int) documentCount)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
