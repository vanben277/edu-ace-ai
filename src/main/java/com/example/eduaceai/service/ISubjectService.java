package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.SubjectRequest;
import com.example.eduaceai.dto.res.SubjectResponse;

import java.util.List;

public interface ISubjectService {
    SubjectResponse create(SubjectRequest request);

    List<SubjectResponse> listMine();

    SubjectResponse getById(Long id);

    SubjectResponse update(Long id, SubjectRequest request);

    void delete(Long id);
}
