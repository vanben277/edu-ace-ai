package com.example.eduaceai.service.impl;

import com.example.eduaceai.config.ResilientChatModel;
import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.dto.res.*;
import com.example.eduaceai.entity.*;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.LearningRoadmapRepository;
import com.example.eduaceai.repository.QuizRepository;
import com.example.eduaceai.repository.QuizResultRepository;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.service.IAiService;
import com.example.eduaceai.service.IQuizService;
import com.example.eduaceai.service.ai.QuizAiService;
import com.example.eduaceai.service.ai.RoadmapAiService;
import com.example.eduaceai.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements IQuizService {
    private final IAiService aiService;
    private final QuizRepository quizRepository;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final QuizAiService quizAiService;
    private final RoadmapAiService roadmapAiService;
    private final ResilientChatModel resilientChatModel;
    private final LearningRoadmapRepository learningRoadmapRepository;

    private static final int MAX_CHARS_PER_DOC = 20000;

    @Override
    @Transactional
    public QuizResponse createQuizFromAi(List<Long> documentIds, int num, String topicHint) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new BusinessException("Cần chọn ít nhất 1 tài liệu", ErrorCodeConstant.BAD_REQUEST);
        }
        if (documentIds.size() > 3) {
            throw new BusinessException("Trắc nghiệm tối đa 3 tài liệu cùng lúc", ErrorCodeConstant.TOO_MANY_DOCUMENTS);
        }

        String studentCode = SecurityUtils.getCurrentStudentCode();

        List<Document> docs = new ArrayList<>();
        for (Long id : documentIds) {
            Document d = documentRepository.findByIdAndUserStudentCode(id, studentCode)
                    .orElseThrow(() -> new BusinessException(
                            "Tài liệu id=" + id + " không tồn tại hoặc không có quyền",
                            ErrorCodeConstant.DOCUMENT_NOT_FOUND));
            docs.add(d);
        }

        String effectiveHint = (topicHint == null || topicHint.isBlank()) ? "NONE" : topicHint.trim();
        boolean isTargeted = !"NONE".equals(effectiveHint);

        String combinedContent = docs.stream()
                .map(d -> "## Tài liệu: " + d.getFileName() + "\n"
                        + truncateContent(d.getContent(), MAX_CHARS_PER_DOC))
                .collect(Collectors.joining("\n\n"));

        try {
            QuizAiResponse aiResponse = quizAiService.generateQuiz(combinedContent, num, effectiveHint);

            if (aiResponse == null || aiResponse.getQuestions() == null || aiResponse.getQuestions().isEmpty()) {
                throw new BusinessException("AI không thể tạo câu hỏi", ErrorCodeConstant.AI_SERVICE_ERROR);
            }

            List<QuestionAiResponse> validated = aiResponse.getQuestions().stream()
                    .map(this::normalizeQuestion)
                    .filter(q -> q != null)
                    .toList();

            if (validated.isEmpty()) {
                log.error("AI trả về toàn bộ câu hỏi có format không hợp lệ. Raw: {}", aiResponse);
                throw new BusinessException(
                        "AI trả về đề không đúng định dạng. Vui lòng thử lại.",
                        ErrorCodeConstant.AI_SERVICE_ERROR);
            }

            String docNamesShort = docs.stream()
                    .map(Document::getFileName)
                    .collect(Collectors.joining(" + "));
            String title = isTargeted
                    ? "Luyện tập: " + effectiveHint + " (" + docs.size() + " tài liệu)"
                    : docs.size() == 1
                            ? "Bài ôn tập: " + docs.get(0).getFileName()
                            : "Bài ôn tập tổng hợp: " + docNamesShort;

            Quiz quiz = Quiz.builder()
                    .title(title)
                    .document(docs.get(0))
                    .sourceDocuments(new ArrayList<>(docs))
                    .build();

            List<Question> questions = validated.stream()
                    .map(res -> Question.builder()
                            .content(res.getContent())
                            .optionA(res.getOptionA())
                            .optionB(res.getOptionB())
                            .optionC(res.getOptionC())
                            .optionD(res.getOptionD())
                            .correctAnswer(res.getCorrectAnswer())
                            .explanation(res.getExplanation())
                            .quiz(quiz)
                            .build())
                    .toList();

            quiz.setQuestions(questions);

            Quiz savedQuiz = quizRepository.save(quiz);
            return mapToQuizResponse(savedQuiz);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Lỗi tạo Quiz: ", e);
            throw new BusinessException("Hệ thống AI không thể tạo đề thi. Thử lại sau!", ErrorCodeConstant.AI_SERVICE_ERROR);
        }
    }

    private static String truncateContent(String content, int maxChars) {
        if (content == null) return "";
        return content.length() <= maxChars ? content : content.substring(0, maxChars);
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(SubmitQuizRequest req) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        User currentUser = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại", "404005"));

        Quiz quiz = quizRepository.findById(req.quizId())
                .orElseThrow(() -> new BusinessException("Không thấy đề thi", ErrorCodeConstant.NOT_FOUND));

        List<Question> questions = quiz.getQuestions();
        List<UserAnswer> userAnswers = new ArrayList<>();
        int correctCount = 0;

        for (Question q : questions) {
            String studentAnswer = req.answers().get(q.getId());
            boolean isCorrect = studentAnswer != null && studentAnswer.equalsIgnoreCase(q.getCorrectAnswer());

            if (isCorrect) correctCount++;

            userAnswers.add(UserAnswer.builder()
                    .question(q)
                    .selectedOption(studentAnswer)
                    .isCorrect(isCorrect)
                    .build());
        }

        double rawScore = (double) correctCount / questions.size() * 10;

        QuizResult result = QuizResult.builder()
                .quiz(quiz)
                .user(currentUser)
                .totalQuestions(questions.size())
                .correctAnswers(correctCount)
                .score(Math.round(rawScore * 100.0) / 100.0)
                .build();

        for (UserAnswer ua : userAnswers) {
            ua.setQuizResult(result);
        }
        result.setUserAnswers(userAnswers);

        QuizResult savedResult = quizResultRepository.save(result);

        // Sinh lộ trình ngay trong flow submit: UX 1-click, không cần gọi API thứ 2
        LearningRoadmapResponse roadmap = generateAndPersistRoadmap(savedResult);
        String servedBy = resilientChatModel.getActiveTier();

        return mapToQuizResultResponse(savedResult, roadmap, servedBy);
    }

    @Override
    public QuizResultResponse getResultDetail(Long resultId) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        QuizResult result = quizResultRepository.findById(resultId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy kết quả", "404000"));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !result.getUser().getStudentCode().equals(studentCode)) {
            throw new BusinessException("Bạn không có quyền xem kết quả này", "403001");
        }

        // Đọc lại roadmap đã lưu (nếu có)
        LearningRoadmapResponse roadmap = null;
        String servedBy = null;
        var saved = learningRoadmapRepository.findByQuizResultId(resultId);
        if (saved.isPresent()) {
            try {
                roadmap = objectMapper.readValue(saved.get().getContentJson(), LearningRoadmapResponse.class);
                servedBy = saved.get().getServedByTier();
            } catch (Exception e) {
                log.warn("Không parse được roadmap đã lưu cho resultId={}", resultId);
            }
        }

        return mapToQuizResultResponse(result, roadmap, servedBy);
    }

    @Override
    public QuizResponse getQuizDetail(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bộ đề", "404000"));

        return mapToQuizResponse(quiz);
    }

    @Override
    public DashboardResponse getStudentDashboard() {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        long totalDocs = documentRepository.countByUserStudentCode(studentCode);
        List<QuizResult> myResults = quizResultRepository.findByUserStudentCode(studentCode);
        long totalQuizzes = myResults.size();
        double avgScore = myResults.stream()
                .mapToDouble(QuizResult::getScore)
                .average()
                .orElse(0.0);

        List<DashboardResponse.ChartData> chartData = myResults.stream()
                .map(r -> new DashboardResponse.ChartData(
                        r.getCompletedAt().toString().substring(5, 10),
                        r.getScore()
                ))
                .toList();

        return DashboardResponse.builder()
                .totalDocuments(totalDocs)
                .totalQuizzesTaken(totalQuizzes)
                .averageScore(Math.round(avgScore * 100.0) / 100.0)
                .progressChart(chartData)
                .build();
    }

    @Override
    public List<QuizHistoryResponse> getMyQuizHistory() {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        List<QuizResult> results = quizResultRepository.findByUserStudentCode(studentCode);

        return results.stream()
                .map(r -> {
                    Quiz quiz = r.getQuiz();
                    List<Long> sourceIds;
                    if (quiz.getSourceDocuments() != null && !quiz.getSourceDocuments().isEmpty()) {
                        sourceIds = quiz.getSourceDocuments().stream()
                                .map(Document::getId)
                                .toList();
                    } else if (quiz.getDocument() != null) {
                        sourceIds = List.of(quiz.getDocument().getId());
                    } else {
                        sourceIds = List.of();
                    }
                    return QuizHistoryResponse.builder()
                            .id(r.getId())
                            .quizTitle(quiz.getTitle())
                            .score(r.getScore())
                            .correctAnswers(r.getCorrectAnswers())
                            .totalQuestions(r.getTotalQuestions())
                            .completedAt(r.getCompletedAt())
                            .sourceDocumentIds(sourceIds)
                            .build();
                })
                .toList();
    }

    /**
     * Validate + normalize 1 câu hỏi AI sinh ra.
     * Trả về null nếu câu không thể sửa được (thiếu trường quan trọng).
     * - correctAnswer: ép về 1 ký tự A/B/C/D viết hoa. Nếu AI trả full text → match với optionA-D để suy ra.
     * - content/options/explanation: bắt buộc có, nếu thiếu → reject.
     */
    private QuestionAiResponse normalizeQuestion(QuestionAiResponse q) {
        if (q == null) return null;
        if (isBlank(q.getContent()) || isBlank(q.getOptionA()) || isBlank(q.getOptionB())
                || isBlank(q.getOptionC()) || isBlank(q.getOptionD())) {
            log.warn("[QUIZ-VALIDATE] Bỏ câu thiếu content/options: {}", q.getContent());
            return null;
        }

        String raw = q.getCorrectAnswer() == null ? "" : q.getCorrectAnswer().trim();
        String letter = null;

        // Case 1: đã là ký tự đơn A/B/C/D (hoa hoặc thường)
        if (raw.length() == 1 && "ABCDabcd".indexOf(raw.charAt(0)) >= 0) {
            letter = raw.toUpperCase();
        }
        // Case 2: dạng "A)" "A." "A:" — lấy ký tự đầu
        else if (raw.length() >= 2 && "ABCDabcd".indexOf(raw.charAt(0)) >= 0
                && "):. -".indexOf(raw.charAt(1)) >= 0) {
            letter = String.valueOf(Character.toUpperCase(raw.charAt(0)));
        }
        // Case 3: AI trả full text đáp án → match với optionA-D
        else {
            String normalizedRaw = raw.toLowerCase();
            if (normalizedRaw.equals(q.getOptionA().trim().toLowerCase())) letter = "A";
            else if (normalizedRaw.equals(q.getOptionB().trim().toLowerCase())) letter = "B";
            else if (normalizedRaw.equals(q.getOptionC().trim().toLowerCase())) letter = "C";
            else if (normalizedRaw.equals(q.getOptionD().trim().toLowerCase())) letter = "D";
        }

        if (letter == null) {
            log.warn("[QUIZ-VALIDATE] Không thể chuẩn hoá correctAnswer='{}' cho câu '{}', reject",
                    raw, q.getContent());
            return null;
        }

        q.setCorrectAnswer(letter);
        if (isBlank(q.getExplanation())) {
            q.setExplanation("Tham khảo lại tài liệu để biết chi tiết đáp án " + letter + ".");
        }
        return q;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Sinh roadmap dựa trên câu sai thực sự + lưu DB. Nếu AI (kể cả 3 tier) chết →
     * fallback static để UX không bao giờ thấy lỗi.
     */
    private LearningRoadmapResponse generateAndPersistRoadmap(QuizResult result) {
        List<UserAnswer> wrongs = result.getUserAnswers().stream()
                .filter(ua -> !ua.isCorrect())
                .toList();

        LearningRoadmapResponse roadmap;
        String tierServed;

        if (wrongs.isEmpty()) {
            roadmap = new LearningRoadmapResponse(
                    "Xuất sắc! Bạn đã trả lời đúng toàn bộ câu hỏi.",
                    List.of(),
                    List.of(new LearningRoadmapResponse.StudyStep(
                            1, "Nâng cao", "Thử đề khó hơn hoặc chủ đề mới", "Tạo một đề mới từ tài liệu khác")),
                    "Duy trì phong độ — thử chủ đề kế tiếp!"
            );
            tierServed = "static-perfect";
        } else {
            String wrongsText = wrongs.stream()
                    .limit(10)
                    .map(ua -> "- Câu: " + trim(ua.getQuestion().getContent(), 160)
                            + " | Bạn chọn: " + safe(ua.getSelectedOption())
                            + " | Đáp án đúng: " + ua.getQuestion().getCorrectAnswer()
                            + " | Giải thích: " + trim(safe(ua.getQuestion().getExplanation()), 200))
                    .collect(Collectors.joining("\n"));

            try {
                roadmap = roadmapAiService.generateRoadmap(
                        result.getQuiz().getTitle(),
                        result.getScore(),
                        result.getCorrectAnswers(),
                        result.getTotalQuestions(),
                        wrongsText
                );
                // Sanitize: chỉ accept tier name trong whitelist, tránh leak giá trị lạ (failed/none)
                String rawTier = resilientChatModel.getActiveTier();
                tierServed = resilientChatModel.isKnownTier(rawTier) ? rawTier : "static-fallback";
            } catch (Exception e) {
                log.error("[ROADMAP] Tất cả tier AI thất bại, dùng fallback tĩnh: {}", e.getMessage());
                roadmap = staticFallbackRoadmap(wrongs);
                tierServed = "static-fallback";
            }
        }

        try {
            String json = objectMapper.writeValueAsString(roadmap);
            learningRoadmapRepository.save(LearningRoadmap.builder()
                    .quizResult(result)
                    .contentJson(json)
                    .servedByTier(tierServed)
                    .build());
        } catch (Exception e) {
            log.warn("Không lưu được roadmap vào DB: {}", e.getMessage());
        }

        return roadmap;
    }

    private LearningRoadmapResponse staticFallbackRoadmap(List<UserAnswer> wrongs) {
        List<LearningRoadmapResponse.WeakTopic> topics = new ArrayList<>();
        topics.add(new LearningRoadmapResponse.WeakTopic(
                "Các nội dung chưa nắm vững", wrongs.size(), "CAO"));

        List<LearningRoadmapResponse.StudyStep> steps = new ArrayList<>();
        steps.add(new LearningRoadmapResponse.StudyStep(
                1, "Xem lại lý thuyết",
                "Đọc lại " + wrongs.size() + " câu sai kèm giải thích",
                "Làm lại các câu đó sau 1 ngày"));
        steps.add(new LearningRoadmapResponse.StudyStep(
                2, "Luyện tập mở rộng",
                "Tạo đề mới từ cùng tài liệu",
                "Hoàn thành với ít nhất 80% đúng"));

        return new LearningRoadmapResponse(
                "Bạn còn " + wrongs.size() + " câu chưa đúng. Hãy tập trung ôn lại!",
                topics,
                steps,
                "Xem lại phần giải thích và làm đề ôn lần hai."
        );
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static String safe(String s) {
        return s == null ? "(bỏ trống)" : s;
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        List<QuestionResponse> questions = quiz.getQuestions().stream()
                .map(q -> new QuestionResponse(
                        q.getId(), q.getContent(), q.getOptionA(),
                        q.getOptionB(), q.getOptionC(), q.getOptionD(),
                        q.getCorrectAnswer(), q.getExplanation()))
                .toList();

        List<Document> sources = quiz.getSourceDocuments();
        List<Long> sourceIds;
        List<String> sourceNames;
        if (sources != null && !sources.isEmpty()) {
            sourceIds = sources.stream().map(Document::getId).toList();
            sourceNames = sources.stream().map(Document::getFileName).toList();
        } else if (quiz.getDocument() != null) {
            sourceIds = List.of(quiz.getDocument().getId());
            sourceNames = List.of(quiz.getDocument().getFileName());
        } else {
            sourceIds = List.of();
            sourceNames = List.of();
        }

        Long primaryDocId = quiz.getDocument() != null ? quiz.getDocument().getId() : null;

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                primaryDocId,
                sourceIds,
                sourceNames,
                questions,
                quiz.getCreatedAt()
        );
    }

    private QuizResultResponse mapToQuizResultResponse(QuizResult result,
                                                       LearningRoadmapResponse roadmap,
                                                       String servedBy) {
        List<UserAnswerResponse> answers = result.getUserAnswers().stream()
                .map(ua -> new UserAnswerResponse(
                        ua.getQuestion().getId(),
                        ua.getQuestion().getContent(),
                        ua.getQuestion().getOptionA(),
                        ua.getQuestion().getOptionB(),
                        ua.getQuestion().getOptionC(),
                        ua.getQuestion().getOptionD(),
                        ua.getSelectedOption(),
                        ua.getQuestion().getCorrectAnswer(),
                        ua.isCorrect(),
                        ua.getQuestion().getExplanation()
                ))
                .toList();

        return new QuizResultResponse(
                result.getId(),
                result.getQuiz().getTitle(),
                result.getTotalQuestions(),
                result.getCorrectAnswers(),
                result.getScore(),
                result.getCompletedAt(),
                answers,
                result.getQuiz().getId(),
                roadmap,
                servedBy
        );
    }
}
