package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record ChatRequest(
        @NotBlank(message = "Tin nhắn không được để trống")
        @Size(max = 5000, message = "Tin nhắn quá dài")
        String message
) {}
