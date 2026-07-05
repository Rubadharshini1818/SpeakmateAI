package com.speakmate.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.model.entity.WeeklyAssessment;
import com.speakmate.backend.repository.WeeklyAssessmentRepository;
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
@RequestMapping("/api/weekly")
@RequiredArgsConstructor
@Slf4j
public class WeeklyAssessmentController {

    private final WeeklyAssessmentRepository weeklyRepo;
    private final GeminiService ai;
    private final BadgeService badgeService;
    private final com.speakmate.backend.repository.UserStatsRepository statsRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    // ── GET /api/weekly/current ───────────────────────────────────────────────
    @GetMapping("/current")
    @Transactional
    public ResponseEntity<?> getCurrentAssessment(@AuthenticationPrincipal User user) {
        LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        Optional<WeeklyAssessment> existing = weeklyRepo.findByUserAndWeekStart(user, weekStart);

        if (existing.isPresent()) {
            return buildResponse(existing.get(), false);
        }

        // Generate new assessment for this week
        WeeklyAssessment assessment = generateAssessment(user, weekStart);
        return buildResponse(assessment, false);
    }

    // ── POST /api/weekly/submit-task ──────────────────────────────────────────
    @PostMapping("/submit-task")
    @Transactional
    public ResponseEntity<?> submitTask(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {

        UUID assessmentId = UUID.fromString((String) body.get("assessmentId"));
        String taskName   = (String) body.get("task");  // reading|listening|essay|errorSpot|story|speech
        Object submission = body.get("submission");

        WeeklyAssessment assessment = weeklyRepo.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        Map<String, Object> tasks = parseTasks(assessment.getTasksJson());
        @SuppressWarnings("unchecked")
        Map<String, Object> task = (Map<String, Object>) tasks.getOrDefault(taskName, new HashMap<>());

        // Evaluate the submission with AI
        Map<String, Object> evaluation = evaluateTask(taskName, task, submission, user.getLearningLevel());
        task.putAll(evaluation);
        task.put("status", "COMPLETED");
        task.put("submission", submission);
        tasks.put(taskName, task);

        assessment.setTasksJson(toJson(tasks));
        weeklyRepo.save(assessment);

        return ResponseEntity.ok(evaluation);
    }

    // ── POST /api/weekly/complete ─────────────────────────────────────────────
    @PostMapping("/complete")
    @Transactional
    public ResponseEntity<?> completeAssessment(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        UUID assessmentId = UUID.fromString(body.get("assessmentId"));
        WeeklyAssessment assessment = weeklyRepo.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        Map<String, Object> tasks = parseTasks(assessment.getTasksJson());

        // Compute scores from tasks
        int reading       = getTaskScore(tasks, "reading");
        int listening     = getTaskScore(tasks, "listening");
        int writing       = getTaskScore(tasks, "essay");
        int errorSpotting = getTaskScore(tasks, "errorSpot");
        int comprehension = getTaskScore(tasks, "story");
        int speech        = getTaskScore(tasks, "speech");

        int overall = (reading + listening + writing + errorSpotting + comprehension + speech) / 6;

        // Generate holistic AI feedback
        String feedbackPrompt = "A student completed a weekly English assessment. Scores: " +
                "Reading=" + reading + "%, Listening=" + listening + "%, " +
                "Essay Writing=" + writing + "%, Error Spotting=" + errorSpotting + "%, " +
                "Comprehension=" + comprehension + "%, Speech=" + speech + "%. " +
                "Their level is " + user.getLearningLevel() + ". " +
                "Give 3-4 sentences of encouraging and specific feedback with improvement areas.";
        String aiFeedback = ai.generateContent(
                "You are an expert English language coach giving weekly assessment feedback.",
                feedbackPrompt);

        assessment.setStatus("COMPLETED");
        assessment.setCompletedAt(LocalDateTime.now());
        assessment.setOverallScore(overall);
        assessment.setReadingScore(reading);
        assessment.setListeningScore(listening);
        assessment.setWritingScore(writing);
        assessment.setErrorSpottingScore(errorSpotting);
        assessment.setComprehensionScore(comprehension);
        assessment.setSpeechScore(speech);
        assessment.setAiFeedback(aiFeedback);
        weeklyRepo.save(assessment);

        com.speakmate.backend.model.entity.UserStats stats = statsRepo.findByUser(user).orElseGet(() -> com.speakmate.backend.model.entity.UserStats.builder()
                .user(user)
                .xp(0)
                .totalStudyTimeSeconds(0)
                .confidenceScore(60)
                .build());
        stats.setXp(stats.getXp() + 200);
        stats.setTotalStudyTimeSeconds(stats.getTotalStudyTimeSeconds() + 1800); // 30 mins
        statsRepo.save(stats);

        badgeService.checkAndAwardBadges(user);

        return ResponseEntity.ok(Map.of(
                "assessmentId",       assessment.getId(),
                "overallScore",       overall,
                "readingScore",       reading,
                "listeningScore",     listening,
                "writingScore",       writing,
                "errorSpottingScore", errorSpotting,
                "comprehensionScore", comprehension,
                "speechScore",        speech,
                "aiFeedback",         aiFeedback,
                "status",             "COMPLETED"
        ));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private WeeklyAssessment generateAssessment(User user, LocalDate weekStart) {
        String level = user.getLearningLevel();
        Map<String, Object> tasks = new LinkedHashMap<>();

        // Task 1: Reading — 5 sentences to read aloud
        tasks.put("reading", generateReadingTask(level));

        // Task 2: Listening — 5 sentences for the AI to speak, user repeats
        tasks.put("listening", generateListeningTask(level));

        // Task 3: Essay Writing
        tasks.put("essay", generateEssayTask(level));

        // Task 4: Error Spotting — 5 sentences with grammar errors
        tasks.put("errorSpot", generateErrorSpottingTask(level));

        // Task 5: Story Listening + Comprehension
        tasks.put("story", generateStoryTask(level));

        // Task 6: Final Speech
        tasks.put("speech", generateSpeechTask(level));

        WeeklyAssessment assessment = WeeklyAssessment.builder()
                .user(user)
                .weekStart(weekStart)
                .tasksJson(toJson(tasks))
                .status("ACTIVE")
                .build();
        return weeklyRepo.save(assessment);
    }

    private Map<String, Object> generateReadingTask(String level) {
        String prompt = "Generate 5 English sentences at CEFR " + level + " level for pronunciation/fluency reading practice. " +
                "Respond ONLY as JSON array: [\"sentence1\", \"sentence2\", ...]";
        String raw = ai.generateContent("You generate reading practice sentences for English learners.", prompt);
        List<String> sentences = parseStringList(raw);
        if (sentences.isEmpty()) sentences = List.of(
            "The quick brown fox jumps over the lazy dog.",
            "She sells seashells by the seashore.",
            "Communication is the key to success in any career.",
            "Technology has transformed the way we live and work.",
            "Practice makes perfect when learning a new language."
        );
        return new HashMap<>(Map.of("sentences", sentences, "status", "PENDING"));
    }

    private Map<String, Object> generateListeningTask(String level) {
        String prompt = "Generate 5 English sentences at CEFR " + level + " level for listen-and-repeat practice. " +
                "Vary the length — 2 short, 2 medium, 1 long. Respond ONLY as JSON array: [\"sentence1\", ...]";
        String raw = ai.generateContent("You generate listening practice sentences for English learners.", prompt);
        List<String> sentences = parseStringList(raw);
        if (sentences.isEmpty()) sentences = List.of(
            "Good morning, how are you today?",
            "I would like to apply for the software engineer position.",
            "The meeting has been rescheduled to Thursday afternoon at three o'clock.",
            "Please ensure all required documents are submitted before the deadline.",
            "Innovation and creativity are essential skills in the modern workplace."
        );
        return new HashMap<>(Map.of("sentences", sentences, "status", "PENDING"));
    }

    private Map<String, Object> generateEssayTask(String level) {
        String raw = ai.generateContent(
                "You generate essay writing topics for English learners.",
                "Give ONE interesting essay topic suitable for " + level + " level English learners. Just the topic sentence, nothing else.");
        String topic = raw.trim();
        if (topic.isBlank()) topic = "Describe a technology that has changed your life and explain why.";
        return new HashMap<>(Map.of("topic", topic, "status", "PENDING", "timeMinutes", 15));
    }

    private Map<String, Object> generateErrorSpottingTask(String level) {
        String prompt = "Generate 5 English sentences with grammatical errors suitable for " + level + " CEFR level. " +
                "Respond ONLY as JSON array of objects: [{\"sentence\": \"...\", \"error\": \"...\", \"correction\": \"...\"}]";
        String raw = ai.generateContent("You create error-spotting exercises for English learners.", prompt);
        List<Map<String, String>> items = parseMapList(raw);
        if (items.isEmpty()) items = List.of(
            Map.of("sentence","She don't like coffee.","error","don't","correction","doesn't"),
            Map.of("sentence","He have been working here since 5 years.","error","since","correction","for"),
            Map.of("sentence","I am knowing the answer.","error","am knowing","correction","know"),
            Map.of("sentence","The informations are incorrect.","error","informations","correction","information"),
            Map.of("sentence","Neither of the students were present.","error","were","correction","was")
        );
        return new HashMap<>(Map.of("items", items, "status", "PENDING"));
    }

    private Map<String, Object> generateStoryTask(String level) {
        String storyPrompt = "Write a short English story (4-5 sentences) at CEFR " + level + " level suitable for comprehension. Then generate 3 multiple-choice comprehension questions. " +
                "Respond ONLY as JSON: {\"story\": \"...\", \"questions\": [{\"question\": \"...\", \"options\": [\"A) ...\", \"B) ...\", \"C) ...\", \"D) ...\"], \"answer\": \"A) ...\"}]}";
        String raw = ai.generateContent("You create listening comprehension exercises for English learners.", storyPrompt);
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(cleaned, Map.class);
            Map<String, Object> task = new HashMap<>(parsed);
            task.put("status", "PENDING");
            return task;
        } catch (Exception e) {
            return new HashMap<>(Map.of(
                "story", "Tom woke up early and went for a run in the park. He saw a dog playing with a ball. A child ran to pick up the ball and smiled at Tom.",
                "questions", List.of(
                    Map.of("question","What did Tom do in the morning?","options",List.of("A) Studied","B) Went for a run","C) Cooked breakfast","D) Watched TV"),"answer","B) Went for a run"),
                    Map.of("question","Where did Tom go?","options",List.of("A) Library","B) Market","C) Park","D) School"),"answer","C) Park"),
                    Map.of("question","What was the dog doing?","options",List.of("A) Sleeping","B) Barking","C) Playing with a ball","D) Running"),"answer","C) Playing with a ball")
                ),
                "status", "PENDING"
            ));
        }
    }

    private Map<String, Object> generateSpeechTask(String level) {
        String raw = ai.generateContent(
                "You create speech practice topics for English learners.",
                "Give ONE speech topic for " + level + " CEFR level. The topic should be specific enough to speak about for 2 minutes. Just the topic, nothing else.");
        String topic = raw.trim();
        if (topic.isBlank()) topic = "Talk about a person who has inspired you and explain why.";
        return new HashMap<>(Map.of("topic", topic, "prepMinutes", 1, "speakMinutes", 2, "status", "PENDING"));
    }

    private Map<String, Object> evaluateTask(String taskName, Map<String, Object> task, Object submission, String level) {
        String sys = "You are an expert English teacher evaluating a student's performance on a language task.";
        String prompt;

        switch (taskName) {
            case "essay" -> {
                prompt = "Essay topic: " + task.get("topic") + "\nStudent's essay:\n" + submission +
                         "\n\nEvaluate for grammar, vocabulary, structure, and coherence. Score 0-100. " +
                         "Respond ONLY as JSON: {\"score\": <0-100>, \"feedback\": \"<2-3 sentences>\"}";
            }
            case "errorSpot" -> {
                prompt = "Error spotting submission: " + submission +
                         "\n\nScore their identification of errors 0-100. " +
                         "Respond ONLY as JSON: {\"score\": <0-100>, \"feedback\": \"<1-2 sentences>\"}";
            }
            case "story" -> {
                prompt = "Comprehension answers: " + submission +
                         "\n\nScore the comprehension 0-100. " +
                         "Respond ONLY as JSON: {\"score\": <0-100>, \"feedback\": \"<1-2 sentences>\"}";
            }
            case "speech" -> {
                prompt = "Speech topic: " + task.get("topic") + "\nTranscript of student speech:\n" + submission +
                         "\n\nEvaluate fluency, relevance, grammar, and vocabulary at " + level + " level. Score 0-100. " +
                         "Respond ONLY as JSON: {\"score\": <0-100>, \"feedback\": \"<2-3 sentences>\"}";
            }
            default -> {
                // reading/listening — score from the submission (match percentage passed from frontend)
                int score = 80; // default
                if (submission instanceof Number) score = ((Number) submission).intValue();
                return Map.of("score", score, "feedback", "Good effort! Keep practicing for better fluency.");
            }
        }

        String raw = ai.generateContent(sys, prompt);
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(cleaned, Map.class);
            if (result.get("score") instanceof Number) {
                result.put("score", ((Number) result.get("score")).intValue());
            }
            return result;
        } catch (Exception e) {
            return Map.of("score", 70, "feedback", "Good attempt! Review the task instructions for better performance.");
        }
    }

    private ResponseEntity<?> buildResponse(WeeklyAssessment assessment, boolean stripAnswers) {
        Map<String, Object> tasks = parseTasks(assessment.getTasksJson());
        return ResponseEntity.ok(Map.of(
                "assessmentId", assessment.getId(),
                "weekStart",    assessment.getWeekStart().toString(),
                "status",       assessment.getStatus(),
                "tasks",        tasks,
                "overallScore", assessment.getOverallScore() != null ? assessment.getOverallScore() : 0,
                "aiFeedback",   assessment.getAiFeedback() != null ? assessment.getAiFeedback() : ""
        ));
    }

    private int getTaskScore(Map<String, Object> tasks, String key) {
        @SuppressWarnings("unchecked")
        Map<String, Object> task = (Map<String, Object>) tasks.getOrDefault(key, Map.of());
        Object score = task.get("score");
        if (score instanceof Number) return ((Number) score).intValue();
        return 0;
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) { return "{}"; }
    }

    private Map<String, Object> parseTasks(String json) {
        try {
            if (json == null || json.isBlank()) return new LinkedHashMap<>();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) { return new LinkedHashMap<>(); }
    }

    private List<String> parseStringList(String raw) {
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            if (cleaned.startsWith("[")) return mapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseMapList(String raw) {
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            if (cleaned.startsWith("[")) return mapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }
}
