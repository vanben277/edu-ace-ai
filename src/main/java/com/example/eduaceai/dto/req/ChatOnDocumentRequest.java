package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatOnDocumentRequest(
        @NotNull(message = "ID tài liệu không được để trống")
        Long documentId,

        @NotBlank(message = "Tin nhắn không được để trống")
        String message
) {}