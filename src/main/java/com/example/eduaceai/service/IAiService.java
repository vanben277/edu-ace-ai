package com.example.eduaceai.service;

import jakarta.validation.Valid;

public interface IAiService {
    String askAi(@Valid String message);
    String askAiOnDocument(Long documentId, String message);
    String generateQuizJson(Long documentId, int num);
    String getQuizFeedback(Long resultId);
}