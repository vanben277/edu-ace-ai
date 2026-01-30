package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.dto.res.DashboardResponse;
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
import com.example.eduaceai.utils.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements IQuizService {
    private final IAiService aiService;
    private final QuizRepository quizRepository;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Quiz createQuizFromAi(Long documentId, int num) {
        String rawJson = aiService.generateQuizJson(documentId, num);

        String cleanJson = rawJson.replaceAll("```json|```", "").trim();

        try {
            List<Question> questions = objectMapper.readValue(cleanJson, new TypeReference<List<Question>>() {
            });

            Document doc = documentRepository.findById(documentId).get();
            Quiz quiz = Quiz.builder()
                    .title("Bài ôn tập: " + doc.getFileName())
                    .document(doc)
                    .build();

            // Gán quan hệ
            for (Question q : questions) {
                q.setQuiz(quiz);
            }
            quiz.setQuestions(questions);

            return quizRepository.save(quiz);

        } catch (Exception e) {
            throw new BusinessException("AI trả về dữ liệu không đúng định dạng JSON", ErrorCodeConstant.AI_SERVICE_ERROR);
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