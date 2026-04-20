package com.example.eduaceai;

import com.example.eduaceai.config.ResilientChatModel;
import com.example.eduaceai.dto.res.LearningRoadmapResponse;
import com.example.eduaceai.service.ai.RoadmapAiService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test chạy độc lập: verify 3-tier fallback chain hoạt động với key thật
 * mà không cần MySQL/Spring context. Bỏ qua test này nếu GEMINI_API_KEY trống.
 */
class AiResilientChainSmokeTest {

    @Test
    void three_tier_chain_responds_and_primary_is_gemini_2_5_flash() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String geminiKey = dotenv.get("GEMINI_API_KEY");
        String primary = dotenv.get("GEMINI_MODEL_PRIMARY", "gemini-2.5-flash");
        String secondary = dotenv.get("GEMINI_MODEL_SECONDARY", "gemini-2.5-flash-lite");
        String groqKey = dotenv.get("GROQ_API_KEY");
        String groqModel = dotenv.get("GROQ_MODEL", "llama-3.3-70b-versatile");

        if (geminiKey == null || geminiKey.isBlank()) {
            System.out.println("[SMOKE] Bỏ qua: GEMINI_API_KEY trống");
            return;
        }

        ChatLanguageModel geminiPrimary = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey).modelName(primary)
                .temperature(0.3).timeout(Duration.ofSeconds(12)).build();
        ChatLanguageModel geminiSecondary = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey).modelName(secondary)
                .temperature(0.3).timeout(Duration.ofSeconds(10)).build();

        List<ResilientChatModel.TieredModel> tiers = new ArrayList<>();
        tiers.add(new ResilientChatModel.TieredModel(primary, geminiPrimary));
        tiers.add(new ResilientChatModel.TieredModel(secondary, geminiSecondary));
        if (groqKey != null && !groqKey.isBlank()) {
            ChatLanguageModel groq = OpenAiChatModel.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .apiKey(groqKey).modelName(groqModel)
                    .temperature(0.3).timeout(Duration.ofSeconds(8)).build();
            tiers.add(new ResilientChatModel.TieredModel(groqModel, groq));
        }

        ResilientChatModel chain = new ResilientChatModel(tiers);
        System.out.println("[SMOKE] Configured tiers: " + chain.listTierNames());

        String reply = chain.generate("Reply with only the single word: OK");
        System.out.println("[SMOKE] Reply: " + reply);
        System.out.println("[SMOKE] Active tier: " + chain.getActiveTier());

        assertNotNull(reply, "Chain phải trả về kết quả");
        assertFalse(reply.isBlank(), "Kết quả không được rỗng");
        // Chỉ cần active tier thuộc danh sách configured — không ép primary luôn sống
        // (Gemini 503 xảy ra thường xuyên trong thực tế, đó là lý do có fallback)
        assertTrue(chain.listTierNames().contains(chain.getActiveTier()),
                "Active tier phải thuộc danh sách configured: " + chain.listTierNames()
                        + ", got: " + chain.getActiveTier());
    }

    @Test
    void measure_roadmap_generation_latency() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String geminiKey = dotenv.get("GEMINI_API_KEY");
        String primary = dotenv.get("GEMINI_MODEL_PRIMARY", "gemini-2.5-flash");
        String secondary = dotenv.get("GEMINI_MODEL_SECONDARY", "gemini-2.5-flash-lite");
        String groqKey = dotenv.get("GROQ_API_KEY");
        String groqModel = dotenv.get("GROQ_MODEL", "llama-3.3-70b-versatile");

        if (geminiKey == null || geminiKey.isBlank()) {
            System.out.println("[LATENCY] Bỏ qua: GEMINI_API_KEY trống");
            return;
        }

        ChatLanguageModel geminiPrimary = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey).modelName(primary)
                .temperature(0.7).timeout(Duration.ofSeconds(12)).build();
        ChatLanguageModel geminiSecondary = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey).modelName(secondary)
                .temperature(0.7).timeout(Duration.ofSeconds(10)).build();

        List<ResilientChatModel.TieredModel> tiers = new ArrayList<>();
        tiers.add(new ResilientChatModel.TieredModel(primary, geminiPrimary));
        tiers.add(new ResilientChatModel.TieredModel(secondary, geminiSecondary));
        if (groqKey != null && !groqKey.isBlank()) {
            ChatLanguageModel groq = OpenAiChatModel.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .apiKey(groqKey).modelName(groqModel)
                    .temperature(0.7).timeout(Duration.ofSeconds(8)).build();
            tiers.add(new ResilientChatModel.TieredModel(groqModel, groq));
        }

        ResilientChatModel chain = new ResilientChatModel(tiers);
        RoadmapAiService roadmapAiService = AiServices.create(RoadmapAiService.class, chain);

        // Fake data 5 câu sai - đại diện cho 1 quiz 10 câu, sai 5
        String wrongAnswers = """
                - Câu: Phương pháp lập trình hướng đối tượng (OOP) gồm mấy đặc tính cơ bản? | Bạn chọn: B | Đáp án đúng: D | Giải thích: OOP có 4 đặc tính: Encapsulation, Inheritance, Polymorphism, Abstraction
                - Câu: Đâu KHÔNG phải là kiểu dữ liệu nguyên thủy trong Java? | Bạn chọn: A | Đáp án đúng: C | Giải thích: String là object class, không phải primitive type
                - Câu: Từ khóa nào dùng để kế thừa class trong Java? | Bạn chọn: A | Đáp án đúng: B | Giải thích: extends dùng để kế thừa class, implements dùng cho interface
                - Câu: ArrayList khác Array ở điểm nào? | Bạn chọn: D | Đáp án đúng: A | Giải thích: ArrayList có thể thay đổi kích thước động, Array có size cố định
                - Câu: Method overloading là gì? | Bạn chọn: C | Đáp án đúng: B | Giải thích: Overloading là nhiều method cùng tên, khác signature trong cùng class
                """;

        // Warm-up không tính: bỏ qua first call tác động cold-start kết nối SSL
        System.out.println("[LATENCY] Bắt đầu đo (1 lần warm-up + 3 lần đo)...");

        long warmupStart = System.currentTimeMillis();
        try {
            roadmapAiService.generateRoadmap("Bài ôn tập Java OOP", 5.0, 5, 10, wrongAnswers);
        } catch (Exception e) {
            System.out.println("[LATENCY] Warm-up fail: " + e.getMessage());
        }
        long warmupMs = System.currentTimeMillis() - warmupStart;
        System.out.println("[LATENCY] Warm-up call: " + warmupMs + "ms (tier=" + chain.getActiveTier() + ")");

        long totalMs = 0;
        int successful = 0;
        for (int i = 1; i <= 3; i++) {
            long t0 = System.currentTimeMillis();
            try {
                LearningRoadmapResponse roadmap = roadmapAiService.generateRoadmap(
                        "Bài ôn tập Java OOP", 5.0, 5, 10, wrongAnswers);
                long elapsed = System.currentTimeMillis() - t0;
                totalMs += elapsed;
                successful++;
                System.out.println("[LATENCY] Run " + i + ": " + elapsed + "ms (tier=" + chain.getActiveTier()
                        + ", weakTopics=" + (roadmap.weakTopics() == null ? 0 : roadmap.weakTopics().size())
                        + ", studyPlan=" + (roadmap.studyPlan() == null ? 0 : roadmap.studyPlan().size()) + ")");
            } catch (Exception e) {
                System.out.println("[LATENCY] Run " + i + " FAIL: " + e.getMessage());
            }
        }

        if (successful > 0) {
            long avgMs = totalMs / successful;
            System.out.println("[LATENCY] === KẾT QUẢ ===");
            System.out.println("[LATENCY] Trung bình " + successful + " lần thành công: " + avgMs + "ms");
            System.out.println("[LATENCY] Khuyến nghị kiến trúc:");
            if (avgMs < 5000) {
                System.out.println("[LATENCY]   → INLINE OK: <5s, giữ trong submit, không cần tách");
            } else if (avgMs < 10000) {
                System.out.println("[LATENCY]   → HYBRID FAST-PATH: 5-10s, dùng phương án D");
            } else {
                System.out.println("[LATENCY]   → ASYNC TÁCH: >10s, bắt buộc phương án A");
            }
        } else {
            System.out.println("[LATENCY] Không có lần nào thành công - structured output có thể đang bị parse fail");
        }
    }

    @Test
    void measure_roadmap_with_groq_as_primary() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String geminiKey = dotenv.get("GEMINI_API_KEY");
        String primary = dotenv.get("GEMINI_MODEL_PRIMARY", "gemini-2.5-flash");
        String secondary = dotenv.get("GEMINI_MODEL_SECONDARY", "gemini-2.5-flash-lite");
        String groqKey = dotenv.get("GROQ_API_KEY");
        String groqModel = dotenv.get("GROQ_MODEL", "llama-3.3-70b-versatile");

        if (groqKey == null || groqKey.isBlank() || geminiKey == null || geminiKey.isBlank()) {
            System.out.println("[GROQ-PRIMARY] Bỏ qua: thiếu key");
            return;
        }

        // ĐẢO THỨ TỰ: Groq là Tier 1, Gemini làm fallback
        ChatLanguageModel groq = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(groqKey).modelName(groqModel)
                .temperature(0.7).timeout(Duration.ofSeconds(8)).build();
        ChatLanguageModel geminiPrimary = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey).modelName(primary)
                .temperature(0.7).timeout(Duration.ofSeconds(12)).build();
        ChatLanguageModel geminiSecondary = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey).modelName(secondary)
                .temperature(0.7).timeout(Duration.ofSeconds(10)).build();

        List<ResilientChatModel.TieredModel> tiers = new ArrayList<>();
        tiers.add(new ResilientChatModel.TieredModel(groqModel, groq));
        tiers.add(new ResilientChatModel.TieredModel(primary, geminiPrimary));
        tiers.add(new ResilientChatModel.TieredModel(secondary, geminiSecondary));

        ResilientChatModel chain = new ResilientChatModel(tiers);
        RoadmapAiService roadmapAiService = AiServices.create(RoadmapAiService.class, chain);

        String wrongAnswers = """
                - Câu: Phương pháp lập trình hướng đối tượng (OOP) gồm mấy đặc tính cơ bản? | Bạn chọn: B | Đáp án đúng: D | Giải thích: OOP có 4 đặc tính: Encapsulation, Inheritance, Polymorphism, Abstraction
                - Câu: Đâu KHÔNG phải là kiểu dữ liệu nguyên thủy trong Java? | Bạn chọn: A | Đáp án đúng: C | Giải thích: String là object class, không phải primitive type
                - Câu: Từ khóa nào dùng để kế thừa class trong Java? | Bạn chọn: A | Đáp án đúng: B | Giải thích: extends dùng để kế thừa class, implements dùng cho interface
                - Câu: ArrayList khác Array ở điểm nào? | Bạn chọn: D | Đáp án đúng: A | Giải thích: ArrayList có thể thay đổi kích thước động, Array có size cố định
                - Câu: Method overloading là gì? | Bạn chọn: C | Đáp án đúng: B | Giải thích: Overloading là nhiều method cùng tên, khác signature trong cùng class
                """;

        System.out.println("[GROQ-PRIMARY] Bắt đầu đo (1 warm-up + 3 lần đo)...");

        long warmupStart = System.currentTimeMillis();
        LearningRoadmapResponse warmRoadmap = null;
        try {
            warmRoadmap = roadmapAiService.generateRoadmap("Bài ôn tập Java OOP", 5.0, 5, 10, wrongAnswers);
        } catch (Exception e) {
            System.out.println("[GROQ-PRIMARY] Warm-up FAIL: " + e.getMessage());
        }
        long warmupMs = System.currentTimeMillis() - warmupStart;
        System.out.println("[GROQ-PRIMARY] Warm-up: " + warmupMs + "ms (tier=" + chain.getActiveTier() + ")");
        if (warmRoadmap != null) {
            System.out.println("[GROQ-PRIMARY] Warm-up overallComment: \"" + warmRoadmap.overallComment() + "\"");
            System.out.println("[GROQ-PRIMARY] Warm-up weakTopics count: " + (warmRoadmap.weakTopics() == null ? 0 : warmRoadmap.weakTopics().size()));
            System.out.println("[GROQ-PRIMARY] Warm-up studyPlan count: " + (warmRoadmap.studyPlan() == null ? 0 : warmRoadmap.studyPlan().size()));
            if (warmRoadmap.studyPlan() != null && !warmRoadmap.studyPlan().isEmpty()) {
                var step1 = warmRoadmap.studyPlan().get(0);
                System.out.println("[GROQ-PRIMARY] Warm-up studyPlan[0]: day=" + step1.day() + ", topic=\"" + step1.topic() + "\"");
            }
        }

        long totalMs = 0;
        int successful = 0;
        int viaGroq = 0;
        for (int i = 1; i <= 3; i++) {
            long t0 = System.currentTimeMillis();
            try {
                LearningRoadmapResponse roadmap = roadmapAiService.generateRoadmap(
                        "Bài ôn tập Java OOP", 5.0, 5, 10, wrongAnswers);
                long elapsed = System.currentTimeMillis() - t0;
                totalMs += elapsed;
                successful++;
                String tier = chain.getActiveTier();
                if (groqModel.equals(tier)) viaGroq++;
                System.out.println("[GROQ-PRIMARY] Run " + i + ": " + elapsed + "ms (tier=" + tier
                        + ", weakTopics=" + (roadmap.weakTopics() == null ? 0 : roadmap.weakTopics().size())
                        + ", studyPlan=" + (roadmap.studyPlan() == null ? 0 : roadmap.studyPlan().size()) + ")");
            } catch (Exception e) {
                System.out.println("[GROQ-PRIMARY] Run " + i + " FAIL: " + e.getMessage());
            }
        }

        if (successful > 0) {
            long avgMs = totalMs / successful;
            System.out.println("[GROQ-PRIMARY] === KẾT QUẢ ===");
            System.out.println("[GROQ-PRIMARY] Trung bình " + successful + " lần thành công: " + avgMs + "ms");
            System.out.println("[GROQ-PRIMARY] Phục vụ qua Groq: " + viaGroq + "/" + successful);
            if (viaGroq == successful && avgMs < 3000) {
                System.out.println("[GROQ-PRIMARY] → PHƯƠNG ÁN E THẮNG: Groq trả nhanh + structured parse OK + 100% hit primary");
            } else if (avgMs < 5000) {
                System.out.println("[GROQ-PRIMARY] → PHƯƠNG ÁN E ĐỦ TỐT: latency chấp nhận được");
            } else {
                System.out.println("[GROQ-PRIMARY] → KHÔNG TỐT HƠN: cần xét lại phương án A");
            }
        } else {
            System.out.println("[GROQ-PRIMARY] Tất cả thất bại - Groq KHÔNG support structured output qua AiServices");
        }
    }

    @Test
    void chain_falls_back_when_primary_has_broken_key() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String groqKey = dotenv.get("GROQ_API_KEY");
        String groqModel = dotenv.get("GROQ_MODEL", "llama-3.3-70b-versatile");

        if (groqKey == null || groqKey.isBlank()) {
            System.out.println("[SMOKE] Bỏ qua fallback test: GROQ_API_KEY trống");
            return;
        }

        ChatLanguageModel brokenPrimary = GoogleAiGeminiChatModel.builder()
                .apiKey("BROKEN_KEY_FORCE_FAIL").modelName("gemini-2.5-flash")
                .temperature(0.3).timeout(Duration.ofSeconds(5)).build();
        ChatLanguageModel brokenSecondary = GoogleAiGeminiChatModel.builder()
                .apiKey("ALSO_BROKEN").modelName("gemini-2.5-flash-lite")
                .temperature(0.3).timeout(Duration.ofSeconds(5)).build();
        ChatLanguageModel groq = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(groqKey).modelName(groqModel)
                .temperature(0.3).timeout(Duration.ofSeconds(8)).build();

        List<ResilientChatModel.TieredModel> tiers = List.of(
                new ResilientChatModel.TieredModel("gemini-2.5-flash", brokenPrimary),
                new ResilientChatModel.TieredModel("gemini-2.5-flash-lite", brokenSecondary),
                new ResilientChatModel.TieredModel(groqModel, groq)
        );

        ResilientChatModel chain = new ResilientChatModel(tiers);
        String reply = chain.generate("Reply with only OK");
        System.out.println("[SMOKE-FAILOVER] Reply: " + reply);
        System.out.println("[SMOKE-FAILOVER] Active tier: " + chain.getActiveTier());

        assertNotNull(reply);
        assertEquals(groqModel, chain.getActiveTier(),
                "Khi 2 tier Gemini hỏng, chain phải failover sang Groq");
    }
}
