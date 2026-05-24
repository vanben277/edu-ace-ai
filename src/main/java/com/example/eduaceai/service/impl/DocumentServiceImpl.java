package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.res.DocumentResponse;
import com.example.eduaceai.entity.Document;
import com.example.eduaceai.entity.DocumentChunk;
import com.example.eduaceai.entity.Subject;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.exception.NotFoundException;
import com.example.eduaceai.repository.DocumentChunkRepository;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.QuizRepository;
import com.example.eduaceai.repository.SubjectRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SubjectRepository subjectRepository;
    private final DocumentChunkRepository chunkRepository;
    private final QuizRepository quizRepository;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, Long subjectId) {
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new BusinessException("Chỉ hỗ trợ định dạng file PDF", ErrorCodeConstant.INVALID_FILE_TYPE);
        }

        String studentCode = SecurityUtils.getCurrentStudentCode();
        User currentUser = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại", ErrorCodeConstant.USER_NOT_FOUND));

        Subject subject = resolveSubject(subjectId, studentCode);

        try {
            String extractedText;
            try (PDDocument pdDocument = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(pdDocument);
            }

            if (extractedText.trim().isEmpty()) {
                throw new BusinessException("File PDF rỗng hoặc không có nội dung chữ", ErrorCodeConstant.BAD_REQUEST);
            }

            String uniqueFileName = resolveUniqueFileName(studentCode, file.getOriginalFilename());

            Document document = Document.builder()
                    .fileName(uniqueFileName)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .content(extractedText)
                    .user(currentUser)
                    .subject(subject)
                    .build();

            Document savedDoc = documentRepository.save(document);

            List<String> chunks = splitText(extractedText, 800);
            for (String text : chunks) {
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

            return toResponse(savedDoc, false);

        } catch (IOException e) {
            throw new BusinessException("Lỗi trong quá trình xử lý file", ErrorCodeConstant.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public List<DocumentResponse> uploadDocuments(List<MultipartFile> files, Long subjectId) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất một file", ErrorCodeConstant.BAD_REQUEST);
        }
        if (files.size() > 4) {
            throw new BusinessException("Mỗi lần chỉ được upload tối đa 4 tài liệu", ErrorCodeConstant.TOO_MANY_DOCUMENTS);
        }
        List<DocumentResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadDocument(file, subjectId));
        }
        return responses;
    }

    private Subject resolveSubject(Long subjectId, String studentCode) {
        if (subjectId == null) {
            return null;
        }
        return subjectRepository.findByIdAndUserStudentCode(subjectId, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy môn học", ErrorCodeConstant.SUBJECT_NOT_FOUND));
    }

    private String resolveUniqueFileName(String studentCode, String original) {
        String name = (original == null || original.isBlank()) ? "tài liệu.pdf" : original.trim();

        int dot = name.lastIndexOf('.');
        String base = (dot > 0) ? name.substring(0, dot) : name;
        String ext = (dot > 0) ? name.substring(dot) : "";

        base = base.replaceAll("\\s*\\(\\d+\\)$", "").trim();
        if (base.isEmpty()) base = "tài liệu";

        List<String> existing = documentRepository.findByUserStudentCode(studentCode).stream()
                .map(Document::getFileName)
                .filter(Objects::nonNull)
                .toList();

        String candidate = base + ext;
        if (existing.stream().noneMatch(candidate::equalsIgnoreCase)) {
            return candidate;
        }
        for (int n = 1; ; n++) {
            String next = base + " (" + n + ")" + ext;
            if (existing.stream().noneMatch(next::equalsIgnoreCase)) {
                return next;
            }
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
    public List<DocumentResponse> getAllDocuments(Long subjectId, Boolean unassignedOnly) {
        String studentCode = SecurityUtils.getCurrentStudentCode();

        List<Document> documents;
        if (Boolean.TRUE.equals(unassignedOnly)) {
            documents = documentRepository.findByUserStudentCodeAndSubjectIsNull(studentCode);
        } else if (subjectId != null) {
            documents = documentRepository.findByUserStudentCodeAndSubjectId(studentCode, subjectId);
        } else {
            documents = documentRepository.findByUserStudentCode(studentCode);
        }

        return documents.stream()
                .map(doc -> toResponse(doc, false))
                .toList();
    }

    @Override
    public DocumentResponse getById(Long id) {
        String studentCode = SecurityUtils.getCurrentStudentCode();

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tài liệu không tồn tại", ErrorCodeConstant.DOCUMENT_NOT_FOUND));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !doc.getUser().getStudentCode().equals(studentCode)) {
            throw new BusinessException("Bạn không có quyền xem tài liệu này", "403001");
        }

        return toResponse(doc, true);
    }

    @Override
    public List<DocumentResponse> adminGetAllDocuments() {
        List<Document> allDocs = documentRepository.findAll();

        return allDocs.stream()
                .map(doc -> toResponse(doc, false))
                .toList();
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        String studentCode = SecurityUtils.getCurrentStudentCode();

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài liệu", ErrorCodeConstant.DOCUMENT_NOT_FOUND));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !doc.getUser().getStudentCode().equals(studentCode)) {
            throw new BusinessException("Bạn không có quyền xóa tài liệu của người khác", "403001");
        }

        quizRepository.deleteSourceDocumentLinksForDocument(doc.getId());

        documentRepository.delete(doc);
    }

    @Override
    @Transactional
    public DocumentResponse setSubject(Long documentId, Long subjectId) {
        String studentCode = SecurityUtils.getCurrentStudentCode();

        Document doc = documentRepository.findByIdAndUserStudentCode(documentId, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài liệu", ErrorCodeConstant.DOCUMENT_NOT_FOUND));

        Subject subject = resolveSubject(subjectId, studentCode);
        doc.setSubject(subject);
        Document saved = documentRepository.save(doc);

        return toResponse(saved, false);
    }

    private DocumentResponse toResponse(Document doc, boolean includeContent) {
        Subject subject = doc.getSubject();
        return DocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .fileType(doc.getFileType())
                .fileSize(doc.getFileSize())
                .content(includeContent ? doc.getContent() : null)
                .ownerCode(doc.getUser() != null ? doc.getUser().getStudentCode() : "N/A")
                .subjectId(subject != null ? subject.getId() : null)
                .subjectName(subject != null ? subject.getName() : null)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
