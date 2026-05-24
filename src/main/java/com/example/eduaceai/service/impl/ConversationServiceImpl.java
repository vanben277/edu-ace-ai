package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.StartConversationRequest;
import com.example.eduaceai.dto.res.ConversationDetailResponse;
import com.example.eduaceai.dto.res.ConversationMessageResponse;
import com.example.eduaceai.dto.res.ConversationSummaryResponse;
import com.example.eduaceai.entity.Conversation;
import com.example.eduaceai.entity.ConversationMessage;
import com.example.eduaceai.entity.Document;
import com.example.eduaceai.entity.Subject;
import com.example.eduaceai.entity.User;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.exception.NotFoundException;
import com.example.eduaceai.repository.ConversationRepository;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.SubjectRepository;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.service.IAiService;
import com.example.eduaceai.service.IConversationService;
import com.example.eduaceai.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final DocumentRepository documentRepository;
    private final IAiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ConversationDetailResponse start(StartConversationRequest req) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        User user = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại", ErrorCodeConstant.USER_NOT_FOUND));

        if (req.documentIds() == null || req.documentIds().isEmpty()) {
            throw new BusinessException("Cần ít nhất 1 tài liệu nguồn", ErrorCodeConstant.BAD_REQUEST);
        }

        Subject subject = null;
        if (req.subjectId() != null) {
            subject = subjectRepository.findByIdAndUserStudentCode(req.subjectId(), studentCode).orElse(null);
        }

        String answer = aiService.askAiOnDocuments(req.documentIds(), req.message());

        Conversation conv = Conversation.builder()
                .user(user)
                .subject(subject)
                .title(buildTitle(req.message()))
                .sourceDocumentIdsJson(writeJson(req.documentIds()))
                .messages(new ArrayList<>())
                .build();

        conv.getMessages().add(message(conv, "USER", req.message()));
        conv.getMessages().add(message(conv, "ASSISTANT", answer));

        Conversation saved = conversationRepository.save(conv);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public ConversationMessageResponse ask(Long conversationId, String message) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        Conversation conv = conversationRepository.findByIdAndUserStudentCode(conversationId, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên chat", ErrorCodeConstant.NOT_FOUND));

        List<Long> docIds = readJson(conv.getSourceDocumentIdsJson());
        String answer = aiService.askAiOnDocuments(docIds, message);

        if (conv.getMessages() == null) {
            conv.setMessages(new ArrayList<>());
        }
        conv.getMessages().add(message(conv, "USER", message));
        ConversationMessage aiMsg = message(conv, "ASSISTANT", answer);
        conv.getMessages().add(aiMsg);

        conversationRepository.save(conv);
        return new ConversationMessageResponse(aiMsg.getId(), aiMsg.getRole(), aiMsg.getContent(), aiMsg.getCreatedAt());
    }

    @Override
    public List<ConversationSummaryResponse> listMine(Long subjectId) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        List<Conversation> conversations = (subjectId != null)
                ? conversationRepository.findByUserStudentCodeAndSubjectIdOrderByUpdatedAtDesc(studentCode, subjectId)
                : conversationRepository.findByUserStudentCodeOrderByUpdatedAtDesc(studentCode);

        return conversations.stream().map(c -> {
            List<ConversationMessage> msgs = c.getMessages() == null ? List.of() : c.getMessages();
            String preview = msgs.stream()
                    .filter(m -> "ASSISTANT".equals(m.getRole()))
                    .reduce((a, b) -> b) // last assistant message
                    .map(m -> truncate(m.getContent(), 120))
                    .orElseGet(() -> msgs.isEmpty() ? "" : truncate(msgs.get(0).getContent(), 120));
            return new ConversationSummaryResponse(
                    c.getId(),
                    c.getTitle(),
                    c.getSubject() != null ? c.getSubject().getId() : null,
                    readJson(c.getSourceDocumentIdsJson()),
                    msgs.size(),
                    preview,
                    c.getUpdatedAt());
        }).toList();
    }

    @Override
    public ConversationDetailResponse getDetail(Long conversationId) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        Conversation conv = conversationRepository.findByIdAndUserStudentCode(conversationId, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên chat", ErrorCodeConstant.NOT_FOUND));
        return toDetail(conv);
    }

    @Override
    @Transactional
    public void delete(Long conversationId) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        Conversation conv = conversationRepository.findByIdAndUserStudentCode(conversationId, studentCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên chat", ErrorCodeConstant.NOT_FOUND));
        conversationRepository.delete(conv);
    }

    // ===== helpers =====

    private ConversationMessage message(Conversation conv, String role, String content) {
        return ConversationMessage.builder()
                .conversation(conv)
                .role(role)
                .content(content)
                .build();
    }

    private ConversationDetailResponse toDetail(Conversation conv) {
        List<Long> docIds = readJson(conv.getSourceDocumentIdsJson());
        List<String> docNames = docIds.stream()
                .map(id -> documentRepository.findById(id).map(Document::getFileName).orElse("(đã xoá)"))
                .toList();

        List<ConversationMessageResponse> messages = (conv.getMessages() == null ? List.<ConversationMessage>of() : conv.getMessages())
                .stream()
                .map(m -> new ConversationMessageResponse(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()))
                .toList();

        return new ConversationDetailResponse(
                conv.getId(),
                conv.getTitle(),
                conv.getSubject() != null ? conv.getSubject().getId() : null,
                docIds,
                docNames,
                messages,
                conv.getCreatedAt(),
                conv.getUpdatedAt());
    }

    private String buildTitle(String firstMessage) {
        String t = firstMessage == null ? "Phiên chat" : firstMessage.trim();
        return t.length() > 60 ? t.substring(0, 60) + "…" : t;
    }

    private String writeJson(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<Long> readJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            Long[] arr = objectMapper.readValue(json, Long[].class);
            return List.of(arr);
        } catch (Exception e) {
            log.warn("Không parse được sourceDocumentIdsJson: {}", json);
            return List.of();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
