package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.AskConversationRequest;
import com.example.eduaceai.dto.req.StartConversationRequest;
import com.example.eduaceai.service.IConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final IConversationService conversationService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse> start(@Valid @RequestBody StartConversationRequest req) {
        var data = conversationService.start(req);
        return ResponseEntity.ok(new ApiResponse("Đã tạo phiên chat", data));
    }

    @PostMapping("/{id}/ask")
    public ResponseEntity<ApiResponse> ask(@PathVariable Long id, @Valid @RequestBody AskConversationRequest req) {
        var data = conversationService.ask(id, req.message());
        return ResponseEntity.ok(new ApiResponse("AI đã phản hồi", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(value = "subjectId", required = false) Long subjectId) {
        var data = conversationService.listMine(subjectId);
        return ResponseEntity.ok(new ApiResponse("Lấy lịch sử chat thành công", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> detail(@PathVariable Long id) {
        var data = conversationService.getDetail(id);
        return ResponseEntity.ok(new ApiResponse("Lấy chi tiết phiên chat thành công", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        conversationService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Đã xoá phiên chat", null));
    }
}
