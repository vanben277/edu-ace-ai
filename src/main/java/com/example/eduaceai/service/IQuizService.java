package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.entity.Quiz;
import com.example.eduaceai.entity.QuizResult;

public interface IQuizService {
    Quiz createQuizFromAi(Long documentId, int num);
    QuizResult submitQuiz(SubmitQuizRequest req);
}