package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StartConversationRequest(
        @NotEmpty(message = "Cần ít nhất 1 tài liệu nguồn")
        @Size(max = 4, message = "Tối đa 4 tài liệu mỗi phiên chat")
        List<Long> documentIds,

        @NotBlank(message = "Câu hỏi không được rỗng")
        @Size(max = 4000, message = "Câu hỏi tối đa 4000 ký tự")
        String message,

        Long subjectId
) {
}
