package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.SetDocumentSubjectRequest;
import com.example.eduaceai.dto.res.DocumentResponse;
import com.example.eduaceai.service.IDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IDocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subjectId", required = false) Long subjectId) {
        DocumentResponse doc = documentService.uploadDocument(file, subjectId);
        return ResponseEntity.ok(new ApiResponse("Tải lên và bóc tách dữ liệu thành công", doc));
    }

    @PostMapping("/upload-batch")
    public ResponseEntity<ApiResponse> uploadBatch(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "subjectId", required = false) Long subjectId) {
        List<DocumentResponse> docs = documentService.uploadDocuments(files, subjectId);
        return ResponseEntity.ok(new ApiResponse("Tải lên " + docs.size() + " tài liệu thành công", docs));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "unassignedOnly", required = false) Boolean unassignedOnly) {
        List<DocumentResponse> docs = documentService.getAllDocuments(subjectId, unassignedOnly);
        return ResponseEntity.ok(new ApiResponse("Lấy danh sách thành công", docs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> show(@PathVariable Long id) {
        DocumentResponse doc = documentService.getById(id);
        return ResponseEntity.ok(new ApiResponse("Lấy chi tiết tài liệu thành công", doc));
    }

    @PutMapping("/{id}/subject")
    public ResponseEntity<ApiResponse> updateSubject(
            @PathVariable Long id,
            @RequestBody SetDocumentSubjectRequest req) {
        DocumentResponse doc = documentService.setSubject(id, req.getSubjectId());
        return ResponseEntity.ok(new ApiResponse("Cập nhật môn học cho tài liệu thành công", doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(new ApiResponse("Đã xóa tài liệu và các dữ liệu liên quan thành công", null));
    }
}
