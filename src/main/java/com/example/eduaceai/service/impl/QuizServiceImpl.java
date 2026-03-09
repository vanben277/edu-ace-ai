package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.dto.res.DashboardResponse;
import com.example.eduaceai.dto.res.QuizAiResponse;
import com.example.eduaceai.dto.res.QuizHistoryResponse;
import com.example.eduaceai.entity.*;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.QuizRepository;
import com.example.eduaceai.repository.QuizResultRepository;
import com.example.eduaceai.repository.UserRepository;
import com.example.eduaceai.service.IAiService;
import com.example.eduaceai.service.IQuizService;
import com.example.eduaceai.service.ai.QuizAiService;
import com.example.eduaceai.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    @Transactional
    public Quiz createQuizFromAi(Long documentId, int num) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài liệu", ErrorCodeConstant.DOCUMENT_NOT_FOUND));

        try {
            // 1. Nhận về đối tượng Wrapper
            QuizAiResponse aiResponse = quizAiService.generateQuiz(doc.getContent(), num);

            if (aiResponse == null || aiResponse.getQuestions() == null) {
                throw new BusinessException("AI không thể tạo câu hỏi", ErrorCodeConstant.AI_SERVICE_ERROR);
            }

            // 2. Tạo Quiz Entity
            Quiz quiz = Quiz.builder()
                    .title("Bài ôn tập: " + doc.getFileName())
                    .document(doc)
                    .build();

            // 3. Map từ List trong Wrapper sang Entity
            List<Question> questions = aiResponse.getQuestions().stream()
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

            return quizRepository.save(quiz);

        } catch (Exception e) {
            log.error("Lỗi chi tiết: ", e);
            throw new BusinessException("Hệ thống AI không thể đóng gói dữ liệu. Thử lại sau!", ErrorCodeConstant.AI_SERVICE_ERROR);
        }
    }

    @Override
    @Transactional
    public QuizResult submitQuiz(SubmitQuizRequest req) {
        String studentCode = SecurityUtils.getCurrentStudentCode();
        User currentUser = userRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại", "404005"));

        Quiz quiz = quizRepository.findById(req.quizId())
                .orElseThrow(() -> new BusinessException("Không thấy đề thi", ErrorCodeConstant.NOT_FOUND));

        int correctCount = 0;
        List<Question> questions = quiz.getQuestions();

        for (Question q : questions) {
            String studentAnswer = req.answers().get(q.getId());
            if (studentAnswer != null && studentAnswer.equalsIgnoreCase(q.getCorrectAnswer())) {
                correctCount++;
            }
        }

        double rawScore = (double) correctCount / questions.size() * 10;
        double finalScore = Math.round(rawScore * 100.0) / 100.0;

        QuizResult result = QuizResult.builder()
                .quiz(quiz)
                .user(currentUser)
                .totalQuestions(questions.size())
                .correctAnswers(correctCount)
                .score(finalScore)
                .build();

        return quizResultRepository.save(result);
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
                .map(r -> QuizHistoryResponse.builder()
                        .id(r.getId())
                        .quizTitle(r.getQuiz().getTitle())
                        .score(r.getScore())
                        .correctAnswers(r.getCorrectAnswers())
                        .totalQuestions(r.getTotalQuestions())
                        .completedAt(r.getCompletedAt())
                        .build())
                .toList();
    }
}