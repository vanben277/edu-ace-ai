package com.example.eduaceai.service;

import com.example.eduaceai.entity.Document;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IDocumentService {
    Document uploadDocument(MultipartFile file);
    List<Document> getAllDocuments();
    Document getById(Long id);
}