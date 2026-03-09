package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.res.DocumentResponse;
import com.example.eduaceai.entity.Document;
import com.example.eduaceai.entity.DocumentChunk;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.exception.NotFoundException;
import com.example.eduaceai.repository.DocumentChunkRepository;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.service.IDocumentService;
import com.example.eduaceai.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements IDocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file) {
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new BusinessException("Chỉ hỗ trợ định dạng file PDF", ErrorCodeConstant.INVALID_FILE_TYPE);
        }

        String studentCode = SecurityUtils.getCurrentStudentCode();
        User currentUser = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại", ErrorCodeConstant.USER_NOT_FOUND));

        try {
            // Bóc tách chữ từ PDF bằng PDFBox
            String extractedText = "";
            try (PDDocument pdDocument = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(pdDocument);
            }

            if (extractedText.trim().isEmpty()) {
                throw new BusinessException("File PDF rỗng hoặc không có nội dung chữ", ErrorCodeConstant.BAD_REQUEST);
            }

            Document document = Document.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .content(extractedText)
                    .user(currentUser)
                    .build();

            Document savedDoc = documentRepository.save(document);

            // Băm nhỏ và lưu Vector
            List<String> chunks = splitText(extractedText, 800); // 800 ký tự mỗi đoạn
            for (String text : chunks) {
                // Tạo Vector từ đoạn văn
                float[] vector = embeddingModel.embed(text).content().vector();

                try {
                    DocumentChunk chunk = DocumentChunk.builder()
                            .document(savedDoc)
                            .content(text)
                            .embeddingJson(objectMapper.writeValueAsString(vector))
                            .build();
                    chunkRepository.save(chunk);
                } catch (Exception e) {
                    log.error("Lỗi lưu chunk: ", e);
                }
            }

            return DocumentResponse.builder()
                    .id(savedDoc.getId())
                    .fileName(savedDoc.getFileName())
                    .fileType(savedDoc.getFileType())
                    .fileSize(savedDoc.getFileSize())
                    .createdAt(savedDoc.getCreatedAt())
                    .build();

        } catch (IOException e) {
            throw new BusinessException("Lỗi trong quá trình xử lý file", ErrorCodeConstant.FILE_UPLOAD_FAILED);
        }
    }

    private List<String> splitText(String text, int size) {
        List<String> chunks = new ArrayList<>();
        int step = size - 100;
        for (int i = 0; i < text.length(); i += step) {
            chunks.add(text.substring(i, Math.min(text.length(), i + size)));
            if (i + size >= text.length()) break;
        }
        return chunks;
    }

    @Override
    public List<DocumentResponse> getAllDocuments() {
        String studentCode = SecurityUtils.getCurrentStudentCode();

        List<Document> documents = documentRepository.findByUserStudentCode(studentCode);

        return documents.stream()
                .map(doc -> DocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .fileType(doc.getFileType())
                        .fileSize(doc.getFileSize())
                        .createdAt(doc.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public Document getById(Long id) {
        String studentCode = SecurityUtils.getCurrentStudentCode();

        return documentRepository.findByIdAndUserStudentCode(id, studentCode)
                .orElseThrow(() -> new BusinessException("Bạn không có quyền truy cập tài liệu này hoặc tài liệu không tồn tại", ErrorCodeConstant.DOCUMENT_NOT_FOUND));
    }

    @Override
    public List<DocumentResponse> adminGetAllDocuments() {
        List<Document> allDocs = documentRepository.findAll();

        return allDocs.stream()
                .map(doc -> DocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .fileType(doc.getFileType())
                        .fileSize(doc.getFileSize())
                        .ownerCode(doc.getUser() != null ? doc.getUser().getStudentCode() : "N/A")
                        .createdAt(doc.getCreatedAt())
                        .build())
                .toList();
    }
}