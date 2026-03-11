package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.dto.res.DashboardResponse;
import com.example.eduaceai.dto.res.QuizHistoryResponse;
import com.example.eduaceai.dto.res.QuizResponse;
import com.example.eduaceai.dto.res.QuizResultResponse;
import com.example.eduaceai.entity.Quiz;
import com.example.eduaceai.entity.QuizResult;

import java.util.List;

public interface IQuizService {
    QuizResponse createQuizFromAi(Long documentId, int num);

    QuizResultResponse submitQuiz(SubmitQuizRequest req);

    DashboardResponse getStudentDashboard();

    List<QuizHistoryResponse> getMyQuizHistory();

    QuizResult getResultDetail(Long resultId);

    Quiz getQuizDetail(Long quizId);
}