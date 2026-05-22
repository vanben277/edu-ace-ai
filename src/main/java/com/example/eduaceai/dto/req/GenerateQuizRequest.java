package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GenerateQuizRequest(
        @NotEmpty(message = "Cần chọn ít nhất 1 tài liệu")
        @Size(max = 3, message = "Trắc nghiệm tối đa 3 tài liệu cùng lúc")
        List<Long> documentIds,

        @Min(value = 1, message = "Số câu hỏi tối thiểu là 1")
        @Max(value = 50, message = "Số câu hỏi tối đa là 50")
        int numberOfQuestions,

        String topicHint
) {
}
