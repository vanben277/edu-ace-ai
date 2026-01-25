package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.GenerateQuizRequest;
import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.entity.Quiz;
import com.example.eduaceai.entity.QuizResult;
import com.example.eduaceai.service.IQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final IQuizService quizService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse> generateQuiz(@Valid @RequestBody GenerateQuizRequest req) {
        Quiz quiz = quizService.createQuizFromAi(req.documentId(), req.numberOfQuestions());

        return ResponseEntity.ok(new ApiResponse("Đã tạo bộ đề thi thành công từ AI", quiz));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse> submit(@RequestBody SubmitQuizRequest req) {
        QuizResult result = quizService.submitQuiz(req);
        return ResponseEntity.ok(new ApiResponse("Đã chấm điểm xong", result));
    }
}