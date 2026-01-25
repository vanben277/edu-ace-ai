package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.dto.res.DashboardResponse;
import com.example.eduaceai.dto.res.QuizHistoryResponse;
import com.example.eduaceai.entity.Quiz;
import com.example.eduaceai.entity.QuizResult;

import java.util.List;

public interface IQuizService {
    Quiz createQuizFromAi(Long documentId, int num);
    QuizResult submitQuiz(SubmitQuizRequest req);
    DashboardResponse getStudentDashboard();
    List<QuizHistoryResponse> getMyQuizHistory();
}