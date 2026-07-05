package com.speakmate.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speakmate.backend.model.entity.GdSession;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.repository.GdSessionRepository;
import com.speakmate.backend.service.BadgeService;
import com.speakmate.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/gd")
@RequiredArgsConstructor
@Slf4j
public class GdController {

    private final GdSessionRepository gdRepo;
    private final GeminiService ai;
    private final BadgeService badgeService;
    private final com.speakmate.backend.repository.UserStatsRepository statsRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    // 3 distinct AI personas matching HTML cards
    private static final List<Map<String, String>> PERSONAS = List.of(
        Map.of("name", "James",  "style", "analytical and data-driven. You cite facts, statistics, and structured arguments. You are calm and methodical. Speak in a male voice."),
        Map.of("name", "Elena",  "style", "empathetic and people-focused. You always consider the human and social impact. You are warm and inclusive. Speak in a female voice."),
        Map.of("name", "Dr. Sarah",  "style", "bold and devil's advocate. You challenge ideas and push for deeper thinking. You are energetic and provocative. Speak in a female voice.")
    );

    private static final List<String> PRESET_TOPICS = List.of(
        "Should AI replace human jobs?",
        "Is remote work better than office work?",
        "Social media: boon or bane for society?",
        "Should college education be free?",
        "Climate change: individual responsibility or government policy?",
        "Is cricket more important than other sports in India?",
        "Should voting be made mandatory?",
        "Are startup jobs better than corporate jobs for fresh graduates?"
    );

    // ── GET /api/gd/topics ────────────────────────────────────────────────────
    @GetMapping("/topics")
    public ResponseEntity<?> getTopics(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("topics", PRESET_TOPICS));
    }

    // ── POST /api/gd/random-topic ─────────────────────────────────────────────
    @PostMapping("/random-topic")
    public ResponseEntity<?> randomTopic(@AuthenticationPrincipal User user) {
        String sys = "You generate unique, thought-provoking group discussion topics suitable for college students and job seekers.";
        String topic = ai.generateContent(sys, "Give me ONE interesting group discussion topic. Just the topic sentence, nothing else.");
        return ResponseEntity.ok(Map.of("topic", topic.trim()));
    }

    // ── POST /api/gd/start ────────────────────────────────────────────────────
    @PostMapping("/start")
    public ResponseEntity<?> startSession(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        String topic = body.getOrDefault("topic", "Technology and Society");

        // AI moderator opens the discussion
        String opening = generateModeratorOpening(topic);

        // First AI participant responds (James, index 0)
        Map<String, String> p1 = PERSONAS.get(0);
        String p1Response = generatePersonaResponse(p1, topic, opening, new ArrayList<>());

        List<Map<String, Object>> transcript = new ArrayList<>();
        transcript.add(Map.of("speaker", "Moderator", "content", opening, "timestamp", System.currentTimeMillis()));
        transcript.add(Map.of("speaker", p1.get("name"), "content", p1Response, "timestamp", System.currentTimeMillis()));

        GdSession session = GdSession.builder()
                .user(user)
                .topic(topic)
                .transcript(toJson(transcript))
                .status("ACTIVE")
                .build();
        session = gdRepo.save(session);

        return ResponseEntity.ok(Map.of(
                "sessionId",        session.getId(),
                "topic",            topic,
                "opening",          opening,
                // Include speakerIndex so frontend knows who spoke and who is next
                "aiTurns",          List.of(Map.of("speaker", p1.get("name"), "content", p1Response, "speakerIndex", 0)),
                "nextSpeakerIndex", 1,   // Elena (index 1) speaks next
                "personas",         PERSONAS.stream().map(p -> p.get("name")).toList()
        ));
    }

    // ── POST /api/gd/ai-turn ──────────────────────────────────────────────────
    // Called by the frontend every 45-60 seconds for autonomous AI turn rotation.
    @PostMapping("/ai-turn")
    @Transactional
    public ResponseEntity<?> aiTurn(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {

        UUID sessionId = UUID.fromString((String) body.get("sessionId"));
        int speakerIndex = body.containsKey("speakerIndex")
                ? ((Number) body.get("speakerIndex")).intValue() : 0;

        GdSession session = gdRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        if (!"ACTIVE".equals(session.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Session not active"));
        }

        List<Map<String, Object>> transcript = fromJson(session.getTranscript());

        Map<String, String> persona = PERSONAS.get(speakerIndex % PERSONAS.size());

        // Use the last spoken line as the direct prompt for this persona's response
        String lastSaid = transcript.isEmpty() ? session.getTopic()
                : (String) transcript.get(transcript.size() - 1).get("content");

        String response = generatePersonaResponse(persona, session.getTopic(), lastSaid, transcript);

        transcript.add(Map.of("speaker", persona.get("name"), "content", response, "timestamp", System.currentTimeMillis()));
        session.setTranscript(toJson(transcript));
        gdRepo.save(session);

        // Round-robin: next AI persona index
        int nextSpeakerIndex = (speakerIndex + 1) % PERSONAS.size();

        return ResponseEntity.ok(Map.of(
                "speaker",          persona.get("name"),
                "content",          response,
                "speakerIndex",     speakerIndex,
                "nextSpeakerIndex", nextSpeakerIndex
        ));
    }

    // ── POST /api/gd/speak ────────────────────────────────────────────────────
    @PostMapping("/speak")
    @Transactional
    public ResponseEntity<?> userSpeaks(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {

        UUID sessionId = UUID.fromString((String) body.get("sessionId"));
        String userText = (String) body.get("text");
        int speakingSeconds = body.containsKey("speakingSeconds")
                ? (int) body.get("speakingSeconds") : 0;

        GdSession session = gdRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        List<Map<String, Object>> transcript = fromJson(session.getTranscript());
        transcript.add(Map.of("speaker", user.getUsername(), "content", userText, "timestamp", System.currentTimeMillis()));

        // Determine which 2 AI participants respond this turn
        int turnIndex = (int) transcript.stream().filter(m -> "user".equals(m.get("speaker")) || user.getUsername().equals(m.get("speaker"))).count();
        List<Map<String, Object>> aiResponses = new ArrayList<>();

        // Pick 2 personas to respond (rotate)
        int p1Idx = turnIndex % 3;
        int p2Idx = (turnIndex + 1) % 3;

        for (int idx : new int[]{p1Idx, p2Idx}) {
            Map<String, String> persona = PERSONAS.get(idx);
            String response = generatePersonaResponse(persona, session.getTopic(), userText, transcript);
            transcript.add(Map.of("speaker", persona.get("name"), "content", response, "timestamp", System.currentTimeMillis()));
            aiResponses.add(Map.of("speaker", persona.get("name"), "content", response));
        }

        // Update speaking time
        int totalSeconds = (session.getSpeakingTimeSeconds() != null ? session.getSpeakingTimeSeconds() : 0) + speakingSeconds;
        session.setSpeakingTimeSeconds(totalSeconds);
        session.setTranscript(toJson(transcript));
        gdRepo.save(session);

        return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(),
                "aiTurns",   aiResponses
        ));
    }

    // ── POST /api/gd/end ──────────────────────────────────────────────────────
    @PostMapping("/end")
    @Transactional
    public ResponseEntity<?> endSession(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        UUID sessionId = UUID.fromString(body.get("sessionId"));
        GdSession session = gdRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        Map<String, Object> report = generateGdReport(session, user.getUsername());

        session.setStatus("COMPLETED");
        session.setEndedAt(LocalDateTime.now());
        session.setOverallScore((Integer) report.get("overallScore"));
        session.setGrammarScore((Integer) report.get("grammarScore"));
        session.setFluencyScore((Integer) report.get("fluencyScore"));
        session.setVocabularyScore((Integer) report.get("vocabularyScore"));
        session.setPronunciationScore((Integer) report.get("pronunciationScore"));
        session.setConfidenceScore((Integer) report.get("confidenceScore"));
        session.setAiFeedback((String) report.get("aiFeedback"));
        session.setStrengths((String) report.get("strengths"));
        session.setWeaknesses((String) report.get("weaknesses"));
        session.setSuggestions((String) report.get("suggestions"));
        gdRepo.save(session);

        com.speakmate.backend.model.entity.UserStats stats = statsRepo.findByUser(user).orElseGet(() -> com.speakmate.backend.model.entity.UserStats.builder()
                .user(user)
                .xp(0)
                .totalStudyTimeSeconds(0)
                .confidenceScore(60)
                .build());
        stats.setXp(stats.getXp() + 150);
        stats.setTotalStudyTimeSeconds(stats.getTotalStudyTimeSeconds() + 900); // 15 mins
        if (report.get("confidenceScore") instanceof Number) {
            stats.setConfidenceScore(((Number) report.get("confidenceScore")).intValue());
        }
        statsRepo.save(stats);

        badgeService.checkAndAwardBadges(user);
        report.put("sessionId", session.getId());
        return ResponseEntity.ok(report);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateModeratorOpening(String topic) {
        String sys = "You are a professional group discussion moderator. Open the discussion clearly, give a brief context for the topic, and invite participants to share views.";
        return ai.generateContent(sys, "Open a group discussion on: \"" + topic + "\". Keep it under 3 sentences.");
    }

    private String generatePersonaResponse(Map<String, String> persona,
                                            String topic,
                                            String lastSaid,
                                            List<Map<String, Object>> transcript) {
        StringBuilder ctx = new StringBuilder();
        int start = Math.max(0, transcript.size() - 8);
        for (int i = start; i < transcript.size(); i++) {
            Map<String, Object> m = transcript.get(i);
            ctx.append(m.get("speaker")).append(": ").append(m.get("content")).append("\n");
        }

        String sys = "You are " + persona.get("name") + " in a group discussion. Your style: " + persona.get("style") +
                " Keep your response to 2-3 sentences. Be direct and conversational. No labels, no introductions — just speak your point.";

        String prompt = "Topic: " + topic + "\n\nRecent discussion:\n" + ctx +
                "\nThe last person said: \"" + lastSaid + "\"\n\nYour response:";
        return ai.generateContent(sys, prompt);
    }

    private Map<String, Object> generateGdReport(GdSession session, String username) {
        List<Map<String, Object>> transcript = fromJson(session.getTranscript());

        // Extract only user's contributions for evaluation
        StringBuilder userContributions = new StringBuilder();
        int userTurnCount = 0;
        for (Map<String, Object> msg : transcript) {
            if (username.equals(msg.get("speaker"))) {
                userContributions.append(msg.get("content")).append("\n\n");
                userTurnCount++;
            }
        }

        String cleanedContributions = userContributions.toString().trim();
        // If candidate did not participate or contribution is negligible, strictly award zero/low scores
        if (cleanedContributions.length() < 10 || userTurnCount == 0) {
            return zeroGdReport();
        }

        String sys = """
            You are an expert English communication coach evaluating a student's group discussion performance.
            Evaluate the student STRICTLY on the quality, relevance, grammar, and fluency of their actual contributions.
            If the student's contributions are extremely short (e.g. single words, simple agreements like "yes", "I agree", or fragmented sentences), you MUST heavily penalize them and give low scores (under 30%).
            Be honest and objective. Do not inflate scores.
            """;
        String prompt = """
            Topic: %s
            
            The student's contributions during the GD:
            %s
            
            Speaking time: %d seconds
            
            Evaluate and respond ONLY as valid JSON (no markdown):
            {
              "overallScore": <0-100>,
              "grammarScore": <0-100>,
              "fluencyScore": <0-100>,
              "vocabularyScore": <0-100>,
              "pronunciationScore": <0-100>,
              "confidenceScore": <0-100>,
              "aiFeedback": "<2-3 sentences of overall feedback>",
              "strengths": "<2-3 bullet points separated by |>",
              "weaknesses": "<2-3 bullet points separated by |>",
              "suggestions": "<3 specific improvement tips separated by |>"
            }
            """.formatted(session.getTopic(), cleanedContributions.substring(0, Math.min(cleanedContributions.length(), 1500)),
                session.getSpeakingTimeSeconds() != null ? session.getSpeakingTimeSeconds() : 0);

        String raw = ai.generateContent(sys, prompt);
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(cleaned, Map.class);
            for (String key : List.of("overallScore","grammarScore","fluencyScore","vocabularyScore","pronunciationScore","confidenceScore")) {
                Object val = result.get(key);
                if (val instanceof Number) result.put(key, ((Number) val).intValue());
                else result.put(key, 0);
            }
            return result;
        } catch (Exception e) {
            log.error("GD report parse error: {}", e.getMessage());
            return defaultGdReport();
        }
    }

    private Map<String, Object> zeroGdReport() {
        return new HashMap<>(Map.of(
            "overallScore", 0, "grammarScore", 0, "fluencyScore", 0,
            "vocabularyScore", 0, "pronunciationScore", 0, "confidenceScore", 0,
            "aiFeedback", "You did not contribute or speak during the group discussion. To get feedback, please share your thoughts and participate actively.",
            "strengths", "None recorded",
            "weaknesses", "No participation|Did not speak",
            "suggestions", "Prepare points on the topic|Speak for at least 1-2 minutes|Use the mic to speak or type your answers"
        ));
    }

    private Map<String, Object> defaultGdReport() {
        return new HashMap<>(Map.of(
            "overallScore", 40, "grammarScore", 40, "fluencyScore", 40,
            "vocabularyScore", 40, "pronunciationScore", 40, "confidenceScore", 40,
            "aiFeedback", "Limited participation. Focus on developing your points more thoroughly and listening actively.",
            "strengths", "Clear point initiation",
            "weaknesses", "Need more elaboration|Improve listening skills",
            "suggestions", "Prepare topic-specific vocabulary|Practice speaking for 2 minutes continuously"
        ));
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { return "[]"; }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) { return new ArrayList<>(); }
    }
}
