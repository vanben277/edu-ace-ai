package com.example.eduaceai.service.impl;

import com.example.eduaceai.entity.Document;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.service.IDocumentService;
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

    @Override
    public Document uploadDocument(MultipartFile file) {
        if (!file.getContentType().equals("application/pdf")) {
            throw new BusinessException("Chỉ hỗ trợ định dạng file PDF", ErrorCodeConstant.INVALID_FILE_TYPE);
        }

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
                    .build();

            return documentRepository.save(document);

        } catch (IOException e) {
            throw new BusinessException("Lỗi trong quá trình xử lý file", ErrorCodeConstant.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài liệu", ErrorCodeConstant.DOCUMENT_NOT_FOUND));
    }
}