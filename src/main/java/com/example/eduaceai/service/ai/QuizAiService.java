package com.example.eduaceai.service.ai;

import com.example.eduaceai.dto.res.QuizAiResponse;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface QuizAiService {
    @UserMessage("""
            Dựa vào nội dung tài liệu sau, hãy tạo ra {{num}} câu hỏi trắc nghiệm ôn thi.
            Hãy đảm bảo kết quả trả về là một đối tượng JSON có chứa danh sách 'questions'.
            
            NỘI DUNG TÀI LIỆU:
            ---
            {{content}}
            ---
            """)
    QuizAiResponse generateQuiz(@V("content") String content, @V("num") int num);
}