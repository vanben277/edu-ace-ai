package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.service.IDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final IDocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> upload(@RequestParam("file") MultipartFile file) {
        var doc = documentService.uploadDocument(file);
        return ResponseEntity.ok(new ApiResponse("Tải lên và bóc tách dữ liệu thành công", doc));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list() {
        return ResponseEntity.ok(new ApiResponse("Lấy danh sách thành công", documentService.getAllDocuments()));
    }
}