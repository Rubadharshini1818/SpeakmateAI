package com.speakmate.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speakmate.backend.model.entity.InterviewSession;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.repository.InterviewSessionRepository;
import com.speakmate.backend.service.BadgeService;
import com.speakmate.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final InterviewSessionRepository sessionRepo;
    private final GeminiService ai;
    private final BadgeService badgeService;
    private final com.speakmate.backend.repository.UserStatsRepository statsRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    // ── POST /api/interview/start ─────────────────────────────────────────────
    @PostMapping("/start")
    public ResponseEntity<?> startInterview(
            @AuthenticationPrincipal User user,
            @RequestParam String type,
            @RequestParam(defaultValue = "MEDIUM") String difficulty,
            @RequestParam(required = false) MultipartFile resume) {

        String resumeText = "";
        if (resume != null && !resume.isEmpty()) {
            resumeText = extractText(resume);
        }

        // Build the opening question
        String firstQuestion = generateFirstQuestion(type, difficulty, resumeText, user.getLearningLevel());

        // Seed transcript with first AI question
        List<Map<String, String>> transcript = new ArrayList<>();
        transcript.add(Map.of("role", "assistant", "content", firstQuestion));
        String transcriptJson = toJson(transcript);

        InterviewSession session = InterviewSession.builder()
                .user(user)
                .interviewType(type.toUpperCase())
                .difficulty(difficulty.toUpperCase())
                .resumeText(resumeText)
                .transcript(transcriptJson)
                .status("ACTIVE")
                .build();
        session = sessionRepo.save(session);

        return ResponseEntity.ok(Map.of(
                "sessionId",     session.getId(),
                "question",      firstQuestion,
                "questionNumber", 1
        ));
    }

    // ── POST /api/interview/answer ────────────────────────────────────────────
    @PostMapping("/answer")
    @Transactional
    public ResponseEntity<?> submitAnswer(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        UUID sessionId = UUID.fromString(body.get("sessionId"));
        String userAnswer = body.get("answer");

        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        if ("COMPLETED".equals(session.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Session already completed"));
        }

        List<Map<String, String>> transcript = fromJson(session.getTranscript());

        // Add user answer to transcript
        transcript.add(Map.of("role", "user", "content", userAnswer));

        int questionCount = (int) transcript.stream().filter(m -> "assistant".equals(m.get("role"))).count();

        // After 6 questions end the session, otherwise ask next question
        if (questionCount >= 6) {
            session.setTranscript(toJson(transcript));
            session = sessionRepo.save(session);
            return ResponseEntity.ok(Map.of(
                    "sessionId",  session.getId(),
                    "done",       true,
                    "message",    "Great interview! Click 'End Interview' to see your results."
            ));
        }

        String nextQuestion = generateNextQuestion(session, transcript);
        transcript.add(Map.of("role", "assistant", "content", nextQuestion));
        session.setTranscript(toJson(transcript));
        sessionRepo.save(session);

        return ResponseEntity.ok(Map.of(
                "sessionId",      session.getId(),
                "question",       nextQuestion,
                "questionNumber", questionCount + 1,
                "done",           false
        ));
    }

    // ── POST /api/interview/end ───────────────────────────────────────────────
    @PostMapping("/end")
    @Transactional
    public ResponseEntity<?> endInterview(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        UUID sessionId = UUID.fromString(body.get("sessionId"));
        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        // Generate report
        Map<String, Object> report = generateReport(session);

        session.setStatus("COMPLETED");
        session.setEndedAt(LocalDateTime.now());
        session.setOverallScore((Integer) report.get("overallScore"));
        session.setCommunicationScore((Integer) report.get("communicationScore"));
        session.setTechnicalScore((Integer) report.get("technicalScore"));
        session.setGrammarScore((Integer) report.get("grammarScore"));
        session.setConfidenceScore((Integer) report.get("confidenceScore"));
        session.setStrengths((String) report.get("strengths"));
        session.setWeaknesses((String) report.get("weaknesses"));
        session.setAiSuggestions((String) report.get("aiSuggestions"));
        sessionRepo.save(session);

        com.speakmate.backend.model.entity.UserStats stats = statsRepo.findByUser(user).orElseGet(() -> com.speakmate.backend.model.entity.UserStats.builder()
                .user(user)
                .xp(0)
                .totalStudyTimeSeconds(0)
                .confidenceScore(60)
                .build());
        stats.setXp(stats.getXp() + 100);
        stats.setTotalStudyTimeSeconds(stats.getTotalStudyTimeSeconds() + 600); // 10 mins
        if (report.get("confidenceScore") instanceof Number) {
            stats.setConfidenceScore(((Number) report.get("confidenceScore")).intValue());
        }
        statsRepo.save(stats);

        badgeService.checkAndAwardBadges(user);
        report.put("sessionId", session.getId());
        return ResponseEntity.ok(report);
    }

    // ── GET /api/interview/report/{sessionId} ─────────────────────────────────
    @GetMapping("/report/{sessionId}")
    public ResponseEntity<?> getReport(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId) {

        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("sessionId",          session.getId());
        r.put("type",               session.getInterviewType());
        r.put("overallScore",       session.getOverallScore());
        r.put("communicationScore", session.getCommunicationScore());
        r.put("technicalScore",     session.getTechnicalScore());
        r.put("grammarScore",       session.getGrammarScore());
        r.put("confidenceScore",    session.getConfidenceScore());
        r.put("strengths",          orEmpty(session.getStrengths()));
        r.put("weaknesses",         orEmpty(session.getWeaknesses()));
        r.put("aiSuggestions",      orEmpty(session.getAiSuggestions()));
        r.put("completedAt",        session.getEndedAt() != null ? session.getEndedAt().toString() : "");
        return ResponseEntity.ok(r);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateFirstQuestion(String type, String difficulty, String resumeText, String level) {
        String sys = buildInterviewerSystem(type, difficulty, level, resumeText);
        String prompt = "Start the interview with your opening question. Do not include any preamble — just ask the first interview question directly.";
        return ai.generateContent(sys, prompt);
    }

    private String generateNextQuestion(InterviewSession session, List<Map<String, String>> transcript) {
        String sys = buildInterviewerSystem(
                session.getInterviewType(), session.getDifficulty(),
                session.getUser().getLearningLevel(), session.getResumeText());

        StringBuilder ctx = new StringBuilder();
        for (Map<String, String> msg : transcript) {
            String role = "assistant".equals(msg.get("role")) ? "Interviewer" : "Candidate";
            ctx.append(role).append(": ").append(msg.get("content")).append("\n\n");
        }

        String prompt = "Conversation so far:\n" + ctx + "\nNow ask the next interview question. Ask only ONE question.";
        return ai.generateContent(sys, prompt);
    }

    private String buildInterviewerSystem(String type, String difficulty, String level, String resumeText) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a professional interviewer conducting a ").append(type)
          .append(" interview. The candidate's English level is ").append(level)
          .append(" and the difficulty is ").append(difficulty).append(".\n\n");

        switch (type.toUpperCase()) {
            case "TECHNICAL" -> {
                if (resumeText != null && !resumeText.isBlank()) {
                    sb.append("The candidate's resume:\n").append(resumeText.substring(0, Math.min(resumeText.length(), 1500)));
                    sb.append("\n\nAsk questions based on the skills and projects in their resume.");
                } else {
                    sb.append("Ask technical questions on data structures, algorithms, and software engineering concepts.");
                }
            }
            case "HR" -> sb.append("Ask HR questions covering motivation, teamwork, communication, strengths, weaknesses, and cultural fit.");
            case "BEHAVIORAL" -> sb.append("Use the STAR method. Ask about past situations, tasks, actions and results.");
            default -> sb.append("Conduct a balanced interview covering both technical and HR topics.");
        }

        sb.append("\n\nRules:\n- Ask ONE question at a time.\n- Keep questions concise and direct.\n- React naturally to the candidate's previous answers.\n- Do not explain what you're doing — just ask questions.");
        return sb.toString();
    }

    private Map<String, Object> generateReport(InterviewSession session) {
        List<Map<String, String>> transcript = fromJson(session.getTranscript());
        StringBuilder ctx = new StringBuilder();
        int userTurnCount = 0;
        for (Map<String, String> msg : transcript) {
            String role = "assistant".equals(msg.get("role")) ? "Interviewer" : "Candidate";
            ctx.append(role).append(": ").append(msg.get("content")).append("\n\n");
            if ("user".equals(msg.get("role"))) {
                userTurnCount++;
            }
        }

        // If candidate did not answer questions or contribution is negligible, award zero/low scores
        if (ctx.toString().trim().length() < 10 || userTurnCount == 0) {
            return zeroInterviewReport();
        }

        String sys = """
            You are an expert interview coach evaluating a candidate's mock interview performance.
            Evaluate the candidate STRICTLY on the quality, relevance, grammar, and fluency of their actual answers.
            If the candidate's answers are extremely short (e.g. single words, simple agreements like "yes", "I agree", or fragmented sentences), you MUST heavily penalize them and give low scores (under 30%).
            Be honest and objective. Do not inflate scores.
            """;
        String prompt = """
            Interview transcript:
            %s
            
            Evaluate the candidate on these criteria and respond ONLY as valid JSON (no markdown, no extra text):
            {
              "overallScore": <0-100>,
              "communicationScore": <0-100>,
              "technicalScore": <0-100>,
              "grammarScore": <0-100>,
              "confidenceScore": <0-100>,
              "strengths": "<2-3 bullet points separated by |>",
              "weaknesses": "<2-3 bullet points separated by |>",
              "aiSuggestions": "<3-4 specific improvement tips separated by |>"
            }
            """.formatted(ctx.toString().substring(0, Math.min(ctx.length(), 3000)));

        String raw = ai.generateContent(sys, prompt);
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(cleaned, Map.class);
            // Ensure all scores are integers
            for (String key : List.of("overallScore","communicationScore","technicalScore","grammarScore","confidenceScore")) {
                Object val = result.get(key);
                if (val instanceof Number) result.put(key, ((Number) val).intValue());
                else result.put(key, 0);
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to parse report JSON: {}", e.getMessage());
            return defaultInterviewReport();
        }
    }

    private Map<String, Object> zeroInterviewReport() {
        return Map.of(
            "overallScore", 0, "communicationScore", 0,
            "technicalScore", 0, "grammarScore", 0, "confidenceScore", 0,
            "strengths", "None recorded",
            "weaknesses", "No answers provided|Did not participate",
            "aiSuggestions", "Speak or type answers to interviewer questions|Practice structured interview responses|Take mock interviews seriously"
        );
    }

    private Map<String, Object> defaultInterviewReport() {
        return Map.of(
            "overallScore", 40, "communicationScore", 40,
            "technicalScore", 40, "grammarScore", 40, "confidenceScore", 40,
            "strengths", "Basic participation",
            "weaknesses", "Need more structure|Reduce filler words|Elaborate more",
            "aiSuggestions", "Practice STAR method|Record yourself speaking|Study common interview questions"
        );
    }

    private String extractText(MultipartFile file) {
        try {
            String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            String text = "";
            if (name.endsWith(".pdf")) {
                try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
                    text = new PDFTextStripper().getText(doc).trim();
                }
            } else {
                // Read text directly for plain text files
                text = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
            }
            
            // Critical Fix: Sanitize null bytes (0x00) which cause PostgreSQL UTF-8 invalid byte sequence errors
            text = text.replace("\u0000", "").replace("\0", "");
            
            log.info("EXTRACTED RESUME TEXT: \n{}", text.substring(0, Math.min(text.length(), 1000)));
            return text;
        } catch (Exception e) {
            log.warn("Could not parse resume file: {}", e.getMessage());
            return "";
        }
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { return "[]"; }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) { return new ArrayList<>(); }
    }

    private String orEmpty(String s) { return s != null ? s : ""; }
}
