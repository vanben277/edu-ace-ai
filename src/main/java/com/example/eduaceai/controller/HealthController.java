package com.example.eduaceai.controller;

import com.example.eduaceai.config.ResilientChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HealthController {

    private final ResilientChatModel resilientChatModel;

    @GetMapping("/health")
    public String healthCheck() {
        return "ALIVE";
    }

    @GetMapping("/ai-health")
    public Map<String, Object> aiHealth() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("configuredTiers", resilientChatModel.listTierNames());
        try {
            String pong = resilientChatModel.generate("Reply with the single word OK");
            payload.put("status", "UP");
            payload.put("activeTier", resilientChatModel.getActiveTier());
            payload.put("sampleReply", pong);
        } catch (Exception e) {
            payload.put("status", "DOWN");
            payload.put("activeTier", resilientChatModel.getActiveTier());
            payload.put("error", e.getMessage());
        }
        return payload;
    }
}
