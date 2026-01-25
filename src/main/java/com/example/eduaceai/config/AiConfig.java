package com.example.eduaceai.config;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {
    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    @Value("${GEMINI_MODEL}")
    private String modelName;

    @Bean
    public GoogleAiGeminiChatModel googleAiGeminiChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.7) // Độ sáng tạo (0: chính xác, 1: sáng tạo)
                .timeout(Duration.ofSeconds(180))
                .logRequestsAndResponses(true)
                .build();
    }
}