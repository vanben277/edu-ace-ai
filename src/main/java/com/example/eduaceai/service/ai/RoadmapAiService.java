package com.example.eduaceai.service.ai;

import com.example.eduaceai.dto.res.LearningRoadmapResponse;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RoadmapAiService {

    @UserMessage("""
            Bạn là một gia sư cá nhân, kinh nghiệm xây lộ trình ôn tập cho sinh viên.
            Sinh viên vừa hoàn thành bài trắc nghiệm "{{quizTitle}}".
            Điểm: {{score}}/10 ({{correctCount}}/{{totalCount}} câu đúng).

            CÁC CÂU SAI (kèm đáp án đúng + giải thích):
            ---
            {{wrongAnswers}}
            ---

            NHIỆM VỤ:
            Sinh ra MỘT lộ trình học tập cá nhân hoá, dưới dạng JSON object khớp schema sau:
            {
              "overallComment": "<nhận xét tổng 1-2 câu về điểm mạnh/yếu, văn phong tích cực>",
              "weakTopics": [
                { "topic": "<tên chủ đề ngắn gọn>", "wrongCount": <số>, "priority": "CAO|TRUNG_BINH|THAP" }
              ],
              "studyPlan": [
                {
                  "day": 1,
                  "topic": "<chủ đề>",
                  "goal": "<mục tiêu cụ thể, có thể đo lường được>",
                  "practice": "<bài tập CỤ THỂ và HÀNH ĐỘNG ĐƯỢC, có ví dụ hoặc số lượng>"
                }
              ],
              "nextStepSuggestion": "<gợi ý bước tiếp theo, 1 câu hành động>"
            }

            YÊU CẦU CHẤT LƯỢNG:
            - weakTopics: tối đa 3 chủ đề, NHÓM các câu sai có chung kiến thức (không liệt kê từng câu).
            - studyPlan: 3 ngày, mỗi ngày 1 chủ đề, đi từ dễ đến khó.
            - 'goal' phải đo lường được (ví dụ: "Hiểu và phân biệt được 4 tính chất OOP" tốt hơn "Học OOP").
            - 'practice' phải HÀNH ĐỘNG ĐƯỢC: nêu rõ số lượng, ví dụ cụ thể, hoặc bài tập có thể làm ngay.
              VÍ DỤ TỐT: "Viết 3 class Java thể hiện Encapsulation: Person, BankAccount, Car"
              VÍ DỤ KÉM: "Đọc thêm về Encapsulation"
            - Văn phong tiếng Việt tự nhiên, ngắn gọn, tích cực, KHUYẾN KHÍCH sinh viên.
            - Chỉ trả JSON, không wrap trong markdown code block.
            """)
    LearningRoadmapResponse generateRoadmap(
            @V("quizTitle") String quizTitle,
            @V("score") double score,
            @V("correctCount") int correctCount,
            @V("totalCount") int totalCount,
            @V("wrongAnswers") String wrongAnswers
    );
}
