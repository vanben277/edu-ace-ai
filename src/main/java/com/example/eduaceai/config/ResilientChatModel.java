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
    // Per-thread state — mỗi request web có 1 thread riêng nên không leak state sang request khác
    private final ThreadLocal<String> lastActiveTier = ThreadLocal.withInitial(() -> TIER_NONE);

    public ResilientChatModel(List<TieredModel> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("ResilientChatModel cần ít nhất 1 tier");
        }
        this.tiers = tiers;
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        // Reset state đầu mỗi call — tránh leak từ call trước của cùng thread
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

    /**
     * Trả về tên tier đã phục vụ call gần nhất trên THREAD HIỆN TẠI.
     * Dùng ngay sau khi generate() return trong cùng thread.
     */
    public String getActiveTier() {
        return lastActiveTier.get();
    }

    /**
     * Kiểm tra tier name có phải là một trong các tier configured hay không.
     * Dùng để sanitize trước khi expose ra response.
     */
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
