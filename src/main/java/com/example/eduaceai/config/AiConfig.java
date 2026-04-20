package com.example.eduaceai.config;

import com.example.eduaceai.service.ai.QuizAiService;
import com.example.eduaceai.service.ai.RoadmapAiService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 3-tier fallback (verified ngày 13/4/2026 với prompt structured tiếng Việt):
 *   Tier 1: Groq llama-3.3-70b   (LPU, ~1.6s, ổn định nhất hiện tại)
 *   Tier 2: Gemini 2.5 Flash     (chất lượng cao, hay 503 trên free tier)
 *   Tier 3: Gemini 2.5 Flash Lite (luôn sống, ~3s)
 * Timeout ngắn (8-12s) để auto-failover nhanh trên sân khấu demo.
 */
@Configuration
public class AiConfig {

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    @Value("${GEMINI_MODEL_PRIMARY:gemini-2.5-flash}")
    private String geminiPrimaryModel;

    @Value("${GEMINI_MODEL_SECONDARY:gemini-2.5-flash-lite}")
    private String geminiSecondaryModel;

    @Value("${GROQ_API_KEY:}")
    private String groqApiKey;

    @Value("${GROQ_MODEL:llama-3.3-70b-versatile}")
    private String groqModel;

    @Bean(name = "geminiPrimary")
    public ChatLanguageModel geminiPrimary() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiPrimaryModel)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(12))
                .logRequestsAndResponses(false)
                .build();
    }

    @Bean(name = "geminiSecondary")
    public ChatLanguageModel geminiSecondary() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiSecondaryModel)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(10))
                .logRequestsAndResponses(false)
                .build();
    }

    @Bean(name = "groqTertiary")
    public ChatLanguageModel groqTertiary() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(groqApiKey == null || groqApiKey.isBlank() ? "missing" : groqApiKey)
                .modelName(groqModel)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(8))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    @Primary
    public ResilientChatModel resilientChatModel(
            ChatLanguageModel geminiPrimary,
            ChatLanguageModel geminiSecondary,
            ChatLanguageModel groqTertiary) {
        List<ResilientChatModel.TieredModel> tiers = new ArrayList<>();
        // Tier 1: Groq nếu có key (LPU 1.6s, ổn định nhất theo benchmark thực tế)
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            tiers.add(new ResilientChatModel.TieredModel(groqModel, groqTertiary));
        }
        // Tier 2 & 3: Gemini làm fallback
        tiers.add(new ResilientChatModel.TieredModel(geminiPrimaryModel, geminiPrimary));
        tiers.add(new ResilientChatModel.TieredModel(geminiSecondaryModel, geminiSecondary));
        return new ResilientChatModel(tiers);
    }

    @Bean
    public QuizAiService quizAiService(ResilientChatModel resilientChatModel) {
        return AiServices.create(QuizAiService.class, resilientChatModel);
    }

    @Bean
    public RoadmapAiService roadmapAiService(ResilientChatModel resilientChatModel) {
        return AiServices.create(RoadmapAiService.class, resilientChatModel);
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }
}
