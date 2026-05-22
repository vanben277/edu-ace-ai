package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatOnDocumentsRequest(
        @NotEmpty(message = "Cần ít nhất 1 tài liệu để chat")
        @Size(max = 4, message = "Chat tối đa 4 tài liệu cùng lúc")
        List<Long> documentIds,

        @NotBlank(message = "Câu hỏi không được rỗng")
        @Size(max = 4000, message = "Câu hỏi tối đa 4000 ký tự")
        String message
) {
}
