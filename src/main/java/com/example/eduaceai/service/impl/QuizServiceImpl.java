package com.example.eduaceai.service.impl;

import com.example.eduaceai.dto.req.SubmitQuizRequest;
import com.example.eduaceai.entity.Document;
import com.example.eduaceai.entity.Question;
import com.example.eduaceai.entity.Quiz;
import com.example.eduaceai.entity.QuizResult;
import com.example.eduaceai.exception.BusinessException;
import com.example.eduaceai.exception.ErrorCodeConstant;
import com.example.eduaceai.repository.DocumentRepository;
import com.example.eduaceai.repository.QuizRepository;
import com.example.eduaceai.repository.QuizResultRepository;
import com.example.eduaceai.service.IAiService;
import com.example.eduaceai.service.IQuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
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

    @Override
    @Transactional
    public Quiz createQuizFromAi(Long documentId, int num) {
        String rawJson = aiService.generateQuizJson(documentId, num);

        String cleanJson = rawJson.replaceAll("```json|```", "").trim();

        try {
            List<Question> questions = objectMapper.readValue(cleanJson, new TypeReference<List<Question>>() {});

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

        double finalScore = (double) correctCount / questions.size() * 10;

        QuizResult result = QuizResult.builder()
                .quiz(quiz)
                .totalQuestions(questions.size())
                .correctAnswers(correctCount)
                .score(finalScore)
                .build();

        return quizResultRepository.save(result);
    }
}