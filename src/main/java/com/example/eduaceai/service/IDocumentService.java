package com.example.eduaceai.service;

import com.example.eduaceai.dto.res.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentService {
    DocumentResponse uploadDocument(MultipartFile file);

    List<DocumentResponse> getAllDocuments();

    DocumentResponse getById(Long id);

    Object adminGetAllDocuments();

    void deleteDocument(Long id);
}