package com.example.eduaceai.service;

import com.example.eduaceai.dto.req.StartConversationRequest;
import com.example.eduaceai.dto.res.ConversationDetailResponse;
import com.example.eduaceai.dto.res.ConversationMessageResponse;
import com.example.eduaceai.dto.res.ConversationSummaryResponse;

import java.util.List;

public interface IConversationService {
    ConversationDetailResponse start(StartConversationRequest request);

    ConversationMessageResponse ask(Long conversationId, String message);

    List<ConversationSummaryResponse> listMine(Long subjectId);

    ConversationDetailResponse getDetail(Long conversationId);

    void delete(Long conversationId);
}
