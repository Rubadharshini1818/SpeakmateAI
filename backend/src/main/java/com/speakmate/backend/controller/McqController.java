package com.speakmate.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speakmate.backend.model.entity.McqSession;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.repository.McqSessionRepository;
import com.speakmate.backend.service.BadgeService;
import com.speakmate.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/mcq")
@RequiredArgsConstructor
@Slf4j
public class McqController {

    private final McqSessionRepository mcqRepo;
    private final GeminiService ai;
    private final BadgeService badgeService;
    private final com.speakmate.backend.repository.UserStatsRepository statsRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    // ── GET /api/mcq/today ────────────────────────────────────────────────────
    @GetMapping("/today")
    @Transactional
    public ResponseEntity<?> getTodayMcq(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "forceNew", required = false, defaultValue = "false") boolean forceNew) {
        LocalDate today = LocalDate.now();
        if (forceNew) {
            mcqRepo.findByUserAndSessionDate(user, today).ifPresent(session -> {
                mcqRepo.delete(session);
                mcqRepo.flush();
            });
        }
        Optional<McqSession> existing = mcqRepo.findByUserAndSessionDate(user, today);

        if (existing.isPresent()) {
            McqSession session = existing.get();
            List<Map<String, Object>> questions = parseQuestions(session.getQuestionsJson());
            // Strip answers if not yet submitted (don't leak correct answers)
            if (!"SUBMITTED".equals(session.getStatus())) {
                questions = stripAnswers(questions);
            }
            return ResponseEntity.ok(Map.of(
                    "sessionId",   session.getId(),
                    "status",      session.getStatus(),
                    "difficulty",  session.getDifficultyUsed(),
                    "questions",   questions,
                    "score",       session.getScore() != null ? session.getScore() : -1
            ));
        }

        // Determine difficulty based on recent performance (adaptive)
        String difficulty = computeAdaptiveDifficulty(user);

        // Generate fresh questions via AI
        List<Map<String, Object>> questions = generateMcqs(user.getLearningLevel(), difficulty);

        McqSession session = McqSession.builder()
                .user(user)
                .sessionDate(today)
                .questionsJson(toJson(questions))
                .status("PENDING")
                .difficultyUsed(difficulty)
                .build();
        session = mcqRepo.save(session);

        return ResponseEntity.ok(Map.of(
                "sessionId",   session.getId(),
                "status",      "PENDING",
                "difficulty",  difficulty,
                "questions",   stripAnswers(questions),
                "score",       -1
        ));
    }

    // ── POST /api/mcq/submit ──────────────────────────────────────────────────
    @PostMapping("/submit")
    @Transactional
    public ResponseEntity<?> submitAnswers(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {

        UUID sessionId = UUID.fromString((String) body.get("sessionId"));
        @SuppressWarnings("unchecked")
        Map<String, String> userAnswers = (Map<String, String>) body.get("answers");

        McqSession session = mcqRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("MCQ session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        if ("SUBMITTED".equals(session.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Already submitted"));
        }

        List<Map<String, Object>> questions = parseQuestions(session.getQuestionsJson());

        // Score the answers
        int correct = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> q : questions) {
            String qId = (String) q.get("id");
            String correctAnswer = (String) q.get("answer");
            String userAnswer = userAnswers != null ? userAnswers.getOrDefault(qId, "") : "";
            boolean isCorrect = correctAnswer != null && correctAnswer.equalsIgnoreCase(userAnswer.trim());
            if (isCorrect) correct++;
            results.add(Map.of(
                    "id",          qId,
                    "question",    q.get("question"),
                    "options",     q.get("options"),
                    "answer",      correctAnswer,
                    "userAnswer",  userAnswer,
                    "correct",     isCorrect,
                    "explanation", q.getOrDefault("explanation", ""),
                    "category",    q.getOrDefault("category", "")
            ));
        }

        session.setAnswersJson(toJson(userAnswers));
        session.setScore(correct);
        session.setStatus("SUBMITTED");
        session.setSubmittedAt(LocalDateTime.now());
        mcqRepo.save(session);

        com.speakmate.backend.model.entity.UserStats stats = statsRepo.findByUser(user).orElseGet(() -> com.speakmate.backend.model.entity.UserStats.builder()
                .user(user)
                .xp(0)
                .totalStudyTimeSeconds(0)
                .confidenceScore(60)
                .build());
        stats.setXp(stats.getXp() + (correct * 10));
        stats.setTotalStudyTimeSeconds(stats.getTotalStudyTimeSeconds() + 300); // 5 mins
        statsRepo.save(stats);

        badgeService.checkAndAwardBadges(user);

        return ResponseEntity.ok(Map.of(
                "sessionId",   session.getId(),
                "score",       correct,
                "total",       questions.size(),
                "percentage",  (correct * 100) / Math.max(1, questions.size()),
                "difficulty",  session.getDifficultyUsed(),
                "results",     results
        ));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String computeAdaptiveDifficulty(User user) {
        List<McqSession> recent = mcqRepo.findByUserOrderBySessionDateDesc(user);
        if (recent.isEmpty()) return "MEDIUM";

        // Look at last 3 submitted sessions
        List<McqSession> submitted = recent.stream()
                .filter(s -> "SUBMITTED".equals(s.getStatus()))
                .limit(3)
                .toList();

        if (submitted.isEmpty()) return "MEDIUM";

        double avgScore = submitted.stream()
                .mapToInt(s -> s.getScore() != null ? s.getScore() : 5)
                .average()
                .orElse(5.0);

        if (avgScore >= 8.5) return "HARD";
        if (avgScore <= 4.0) return "EASY";
        return "MEDIUM";
    }

    private List<Map<String, Object>> generateMcqs(String level, String difficulty) {
        String sys = """
            You are an English language test designer for Indian college students preparing for campus placements.
            Generate exactly 10 MCQ questions. Cover these categories (2 each):
            Grammar, Vocabulary, Error Spotting, Verbal Ability, Communication.
            Difficulty: %s. CEFR Level: %s.
            
            Respond ONLY as a valid JSON array (no markdown, no extra text):
            [
              {
                "id": "q1",
                "category": "Grammar",
                "difficulty": "%s",
                "question": "...",
                "options": ["A) ...", "B) ...", "C) ...", "D) ..."],
                "answer": "A) ...",
                "explanation": "..."
              },
              ...
            ]
            Make sure 'answer' is the EXACT text of the correct option from 'options'.
            """.formatted(difficulty, level, difficulty);

        String raw = ai.generateContent(sys, "Generate 10 MCQ questions now.");
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            // Sometimes AI wraps in object — handle both array and object
            if (cleaned.startsWith("{")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> wrapped = mapper.readValue(cleaned, Map.class);
                Object arr = wrapped.values().iterator().next();
                cleaned = toJson(arr);
            }
            return mapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("MCQ generation parse error: {}", e.getMessage());
            return fallbackQuestions(difficulty);
        }
    }

    private List<Map<String, Object>> fallbackQuestions(String difficulty) {
        List<Map<String, Object>> questions = new ArrayList<>();
        Object[][] data = {
            {"q1","Grammar","Choose the correct form: She _____ to school every day.","A) go","B) goes","C) going","D) gone","B) goes","'She' is third-person singular, so 'goes' is correct."},
            {"q2","Grammar","Which sentence is correct?","A) He don't know me","B) He doesn't knows me","C) He doesn't know me","D) He not know me","C) He doesn't know me","Third-person singular uses 'doesn't' + base verb."},
            {"q3","Vocabulary","What does 'eloquent' mean?","A) Rude","B) Fluent and persuasive","C) Silent","D) Confused","B) Fluent and persuasive","Eloquent means well-spoken and persuasive."},
            {"q4","Vocabulary","Choose the correct synonym for 'obstinate':","A) Helpful","B) Stubborn","C) Generous","D) Timid","B) Stubborn","Obstinate means stubbornly refusing to change."},
            {"q5","Error Spotting","Find the error: 'I am knowing him since 2010.'","A) I am","B) knowing","C) him since","D) 2010","B) knowing","'Know' is a stative verb — use 'have known' not 'am knowing'."},
            {"q6","Error Spotting","Find the error: 'Neither of the boys are ready.'","A) Neither","B) of the boys","C) are","D) ready","C) are","'Neither' takes singular verb — 'is ready'."},
            {"q7","Verbal Ability","Complete: Light : Sun :: Water : ___","A) Rain","B) Ocean","C) River","D) Tap","B) Ocean","Sun is the primary source of light; Ocean is the primary body of water."},
            {"q8","Verbal Ability","If BOOK = 2,15,15,11 then DOOR = ?","A) 4,15,15,18","B) 4,14,14,17","C) 3,15,14,18","D) 4,15,14,17","A) 4,15,15,18","Each letter maps to its position: D=4, O=15, O=15, R=18."},
            {"q9","Communication","In a formal email, which opening is most appropriate?","A) Hey there!","B) Yo,","C) Dear Mr. Sharma,","D) Sup,","C) Dear Mr. Sharma,","Formal emails use 'Dear [Title] [Name]'."},
            {"q10","Communication","What does 'ASAP' mean in professional communication?","A) As Slow As Possible","B) As Soon As Possible","C) As Simple As Possible","D) As Safe As Possible","B) As Soon As Possible","ASAP = As Soon As Possible, common workplace abbreviation."}
        };
        for (Object[] d : data) {
            questions.add(new HashMap<>(Map.of(
                "id", d[0], "category", d[1], "difficulty", difficulty,
                "question", d[2],
                "options", List.of(d[3], d[4], d[5], d[6]),
                "answer", d[7], "explanation", d[8]
            )));
        }
        return questions;
    }

    private List<Map<String, Object>> stripAnswers(List<Map<String, Object>> questions) {
        List<Map<String, Object>> stripped = new ArrayList<>();
        for (Map<String, Object> q : questions) {
            Map<String, Object> safe = new LinkedHashMap<>(q);
            safe.remove("answer");
            safe.remove("explanation");
            stripped.add(safe);
        }
        return stripped;
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { return "[]"; }
    }

    private List<Map<String, Object>> parseQuestions(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) { return new ArrayList<>(); }
    }
}
