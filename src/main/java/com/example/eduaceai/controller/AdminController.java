package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.dto.res.UserResponse;
import com.example.eduaceai.service.IAdminService;
import com.example.eduaceai.service.IDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;
    private final IDocumentService documentService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUsers(UserFilterForm form) {
        List<UserResponse> users = adminService.getAllUsers(form);

        return ResponseEntity.ok(new ApiResponse("Lấy danh sách người dùng thành công", users));
    }

    @GetMapping("/documents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllSystemDocuments() {
        var docs = documentService.adminGetAllDocuments();
        return ResponseEntity.ok(new ApiResponse("Lấy toàn bộ tài liệu thành công", docs));
    }

    @PostMapping("/users/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> toggleStatus(@PathVariable Long userId) {
        adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(new ApiResponse("Cập nhật trạng thái người dùng thành công", null));
    }
}