package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskConversationRequest(
        @NotBlank(message = "Câu hỏi không được rỗng")
        @Size(max = 4000, message = "Câu hỏi tối đa 4000 ký tự")
        String message
) {
}
