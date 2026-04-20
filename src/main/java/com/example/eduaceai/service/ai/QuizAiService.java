package com.example.eduaceai.service.ai;

import com.example.eduaceai.dto.res.QuizAiResponse;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface QuizAiService {
    @UserMessage("""
            Bạn là một giảng viên đại học chuyên môn cao, có nhiệm vụ ra đề trắc nghiệm ÔN THI cho sinh viên.
            Dựa vào nội dung tài liệu dưới đây, hãy tạo ra ĐÚNG {{num}} câu hỏi trắc nghiệm.

            CHỦ ĐỀ TẬP TRUNG (nếu có): "{{topicHint}}"
            - Nếu CHỦ ĐỀ TẬP TRUNG KHÔNG phải "NONE" và không rỗng: TOÀN BỘ câu hỏi PHẢI xoay quanh chủ đề này.
              Không sinh câu hỏi về chủ đề khác. Mỗi câu phải kiểm tra hiểu biết sâu về chủ đề được chỉ định.
            - Nếu CHỦ ĐỀ TẬP TRUNG là "NONE": sinh câu hỏi đa dạng bao quát toàn tài liệu.

            ============================================================
            ĐỊNH DẠNG JSON BẮT BUỘC (KHÔNG ĐƯỢC PHÉP SAI):
            ============================================================
            Trả về ĐÚNG một JSON object, KHÔNG wrap trong markdown code block (```).
            Schema:
            {
              "questions": [
                {
                  "content": "<nội dung câu hỏi đầy đủ, kết thúc bằng dấu ?>",
                  "optionA": "<văn bản đáp án A>",
                  "optionB": "<văn bản đáp án B>",
                  "optionC": "<văn bản đáp án C>",
                  "optionD": "<văn bản đáp án D>",
                  "correctAnswer": "<CHỈ một ký tự: A hoặc B hoặc C hoặc D>",
                  "explanation": "<giải thích VÌ SAO đáp án đó đúng, dẫn chứng từ tài liệu>"
                }
              ]
            }

            ⚠️ QUAN TRỌNG VỀ TRƯỜNG 'correctAnswer':
            - PHẢI là MỘT ký tự viết HOA: "A", "B", "C", hoặc "D"
            - TUYỆT ĐỐI KHÔNG dùng: văn bản đầy đủ đáp án, ký tự thường, số, dấu ngoặc, chấm
            - Ví dụ ĐÚNG: "correctAnswer": "B"
            - Ví dụ SAI: "correctAnswer": "Tính kế thừa"  ❌
            - Ví dụ SAI: "correctAnswer": "b"  ❌
            - Ví dụ SAI: "correctAnswer": "B)"  ❌
            - Ví dụ SAI: "correctAnswer": 2  ❌

            ============================================================
            VÍ DỤ HOÀN CHỈNH (few-shot):
            ============================================================
            {
              "questions": [
                {
                  "content": "Trong lập trình hướng đối tượng, tính chất nào cho phép một lớp con kế thừa các thuộc tính và phương thức từ lớp cha?",
                  "optionA": "Tính đóng gói",
                  "optionB": "Tính kế thừa",
                  "optionC": "Tính đa hình",
                  "optionD": "Tính trừu tượng",
                  "correctAnswer": "B",
                  "explanation": "Tính kế thừa (Inheritance) là cơ chế cho phép lớp con tự động có các thuộc tính và phương thức của lớp cha, giúp tái sử dụng code."
                }
              ]
            }

            ============================================================
            QUY TẮC CHẤT LƯỢNG (BẮT BUỘC):
            ============================================================
            1. CHÍNH XÁC KIẾN THỨC: Đáp án PHẢI ĐÚNG TUYỆT ĐỐI theo lý thuyết khoa học chuẩn.
               Trước khi viết, tự hỏi "đáp án này có đúng tuyệt đối không?". Nếu có nghi ngờ, đổi câu khác.
            2. KHÔNG MƠ HỒ: Không chế câu hỏi có nhiều đáp án có thể đúng.
            3. PHÂN BIỆT KHÁI NIỆM CỐT LÕI vs FEATURE CON.
               Ví dụ: 4 tính chất OOP CỐT LÕI là Encapsulation, Inheritance, Polymorphism, Abstraction.
               "Đa kế thừa", "method overloading" là feature CON, KHÔNG phải tính chất chính.
            4. ĐÁP ÁN NHIỄU HỢP LÝ: 3 đáp án sai phải hợp lý, không quá ngớ ngẩn.
            5. BÁM NỘI DUNG: Câu hỏi phải có thể trả lời được bằng nội dung tài liệu dưới đây.
            6. GIẢI THÍCH RÕ: Mỗi câu phải có 'explanation' giải thích VÌ SAO đáp án đó đúng.

            NỘI DUNG TÀI LIỆU:
            ---
            {{content}}
            ---
            """)
    QuizAiResponse generateQuiz(
            @V("content") String content,
            @V("num") int num,
            @V("topicHint") String topicHint
    );
}
