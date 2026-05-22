package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.SubjectRequest;
import com.example.eduaceai.dto.res.SubjectResponse;
import com.example.eduaceai.service.ISubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final ISubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody SubjectRequest req) {
        SubjectResponse data = subjectService.create(req);
        return ResponseEntity.ok(new ApiResponse("Tạo môn học thành công", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list() {
        List<SubjectResponse> data = subjectService.listMine();
        return ResponseEntity.ok(new ApiResponse("Lấy danh sách môn học thành công", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> show(@PathVariable Long id) {
        SubjectResponse data = subjectService.getById(id);
        return ResponseEntity.ok(new ApiResponse("Lấy chi tiết môn học thành công", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest req) {
        SubjectResponse data = subjectService.update(id, req);
        return ResponseEntity.ok(new ApiResponse("Cập nhật môn học thành công", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.ok(new ApiResponse(
                "Đã xóa môn học. Các tài liệu thuộc môn này được chuyển sang chưa phân loại.",
                null));
    }
}
