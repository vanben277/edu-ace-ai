package com.example.eduaceai.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ResilientChatModel implements ChatLanguageModel {

    private static final String TIER_NONE = "none";
    private static final String TIER_FAILED = "failed";

    private final List<TieredModel> tiers;
    private final ThreadLocal<String> lastActiveTier = ThreadLocal.withInitial(() -> TIER_NONE);

    public ResilientChatModel(List<TieredModel> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("ResilientChatModel cần ít nhất 1 tier");
        }
        this.tiers = tiers;
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        lastActiveTier.set(TIER_NONE);

        RuntimeException lastError = null;
        for (TieredModel tier : tiers) {
            try {
                long start = System.currentTimeMillis();
                Response<AiMessage> response = tier.model().generate(messages);
                long elapsed = System.currentTimeMillis() - start;
                lastActiveTier.set(tier.name());
                log.info("[AI-FAILOVER] Tier {} trả lời thành công trong {}ms", tier.name(), elapsed);
                return response;
            } catch (RuntimeException e) {
                lastError = e;
                log.warn("[AI-FAILOVER] Tier {} thất bại: {} → fallback tier kế tiếp",
                        tier.name(), rootMessage(e));
            }
        }
        lastActiveTier.set(TIER_FAILED);
        throw new RuntimeException("Tất cả AI provider đều thất bại", lastError);
    }

    @Override
    public Response<AiMessage> generate(ChatMessage... messages) {
        return generate(List.of(messages));
    }

    public String getActiveTier() {
        return lastActiveTier.get();
    }

    public boolean isKnownTier(String name) {
        if (name == null) return false;
        return tiers.stream().anyMatch(t -> t.name().equals(name));
    }

    public List<String> listTierNames() {
        return tiers.stream().map(TieredModel::name).toList();
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        String msg = cur.getMessage();
        return msg == null ? cur.getClass().getSimpleName() : msg;
    }

    public record TieredModel(String name, ChatLanguageModel model) {}
}
