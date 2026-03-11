package com.example.eduaceai.controller;

import com.example.eduaceai.dto.ApiResponse;
import com.example.eduaceai.dto.req.GenerateQuizRequest;
import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.dto.res.QuizHistoryResponse;
import com.example.eduaceai.entity.Quiz;
import com.example.eduaceai.entity.QuizResult;
import com.example.eduaceai.service.IQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard() {
        return ResponseEntity.ok(new ApiResponse("Lấy số liệu thống kê thành công", quizService.getStudentDashboard()));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse> getHistory() {
        List<QuizHistoryResponse> history = quizService.getMyQuizHistory();

        return ResponseEntity.ok(new ApiResponse("Lấy lịch sử làm bài thành công", history));
    }

    @GetMapping("/result/{resultId}")
    public ResponseEntity<ApiResponse> getResultDetail(@PathVariable Long resultId) {
        return ResponseEntity.ok(new ApiResponse("Lấy chi tiết kết quả thành công", quizService.getResultDetail(resultId)));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse> getQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(new ApiResponse("Lấy bộ đề thành công", quizService.getQuizDetail(quizId)));
    }
}