package com.example.eduaceai.service;

import com.example.eduaceai.dto.res.InteractionResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface IAiService {
    String askAi(@Valid String message);

    String askAiOnDocument(Long documentId, String message);

    String generateQuizJson(Long documentId, int num);

    String getQuizFeedback(Long resultId);

    List<InteractionResponse> getChatHistory(Long documentId);

}