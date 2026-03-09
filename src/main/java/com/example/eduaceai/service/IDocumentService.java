package com.example.eduaceai.service;

import com.example.eduaceai.dto.res.DocumentResponse;
import com.example.eduaceai.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentService {
    DocumentResponse uploadDocument(MultipartFile file);

    List<DocumentResponse> getAllDocuments();

    Document getById(Long id);

    Object adminGetAllDocuments();
}