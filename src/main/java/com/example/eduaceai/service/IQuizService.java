package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.dto.res.DashboardResponse;
import com.example.eduaceai.dto.res.QuizHistoryResponse;
import com.example.eduaceai.dto.res.QuizResponse;
import com.example.eduaceai.dto.res.QuizResultResponse;

import java.util.List;

public interface IQuizService {
    QuizResponse createQuizFromAi(List<Long> documentIds, int num, String topicHint);

    QuizResultResponse submitQuiz(SubmitQuizRequest req);

    DashboardResponse getStudentDashboard();

    List<QuizHistoryResponse> getMyQuizHistory();

    QuizResultResponse getResultDetail(Long resultId);

    QuizResponse getQuizDetail(Long quizId);
}