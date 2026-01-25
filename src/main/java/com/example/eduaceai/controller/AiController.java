package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.ChatOnDocumentRequest;
import com.example.eduaceai.dto.req.ChatRequest;
import com.example.eduaceai.service.IAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final IAiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse> chat(@Valid @RequestBody ChatRequest request) {
        String response = aiService.askAi(request.message());
        return ResponseEntity.ok(new ApiResponse("Thành công", response));
    }

    @PostMapping("/chat-on-document")
    public ResponseEntity<ApiResponse> chatOnDocument(@Valid @RequestBody ChatOnDocumentRequest request) {
        String response = aiService.askAiOnDocument(request.documentId(), request.message());
        return ResponseEntity.ok(new ApiResponse("AI đã đọc tài liệu và phản hồi", response));
    }

    @GetMapping("/{resultId}/feedback")
    public ResponseEntity<ApiResponse> getFeedback(@PathVariable Long resultId) {
        String feedback = aiService.getQuizFeedback(resultId);
        return ResponseEntity.ok(new ApiResponse("AI đã phân tích xong bài làm", feedback));
    }

    @GetMapping("/history/{documentId}")
    public ResponseEntity<ApiResponse> getHistory(@PathVariable Long documentId) {
        var history = aiService.getChatHistory(documentId);
        return ResponseEntity.ok(new ApiResponse("Lấy lịch sử chat thành công", history));
    }
}