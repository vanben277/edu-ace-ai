package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.UserFilterForm;
import com.example.eduaceai.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUsers(UserFilterForm form) {
        var users = adminService.getAllUsers(form);

        return ResponseEntity.ok(new ApiResponse("Lấy danh sách người dùng thành công", users));
    }
}