package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse> upload(@RequestParam("file") MultipartFile file) {
        DocumentResponse doc = documentService.uploadDocument(file);
        return ResponseEntity.ok(new ApiResponse("Tải lên và bóc tách dữ liệu thành công", doc));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list() {
        List<DocumentResponse> docs = documentService.getAllDocuments();
        return ResponseEntity.ok(new ApiResponse("Lấy danh sách thành công", docs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> show(@PathVariable Long id) {
        var doc = documentService.getById(id);
        return ResponseEntity.ok(new ApiResponse("Lấy chi tiết tài liệu thành công", doc));
    }
}