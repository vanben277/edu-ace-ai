package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.res.DocumentResponse;
import com.example.eduaceai.entity.Document;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.exception.NotFoundException;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.service.IDocumentService;
import com.example.eduaceai.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements IDocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file) {
        if (!file.getContentType().equals("application/pdf")) {
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

    @Override
    public List<DocumentResponse> getAllDocuments() {
        String studentCode = SecurityUtils.getCurrentStudentCode();

        // 1. Lấy danh sách Entity
        List<Document> documents = documentRepository.findByUserStudentCode(studentCode);

        // 2. Chuyển sang DTO (Ngắt vòng lặp JSON)
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

        // CHỐT CHẶN BẢO MẬT: Phải đúng ID và đúng chủ sở hữu
        return documentRepository.findByIdAndUserStudentCode(id, studentCode)
                .orElseThrow(() -> new BusinessException("Bạn không có quyền truy cập tài liệu này hoặc tài liệu không tồn tại", ErrorCodeConstant.DOCUMENT_NOT_FOUND));
    }
}