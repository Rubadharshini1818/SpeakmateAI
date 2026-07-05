package com.speakmate.backend.controller;

import com.speakmate.backend.model.entity.*;
import com.speakmate.backend.repository.*;
import com.speakmate.backend.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final UserStatsRepository statsRepo;
    private final UserLessonProgressRepository lessonProgressRepo;
    private final InterviewSessionRepository interviewRepo;
    private final GdSessionRepository gdRepo;
    private final McqSessionRepository mcqRepo;
    private final BadgeService badgeService;
    private final UserBadgeRepository userBadgeRepo;
    private final LessonRepository lessonRepo;

    // ── GET /api/progress/summary ─────────────────────────────────────────────
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@AuthenticationPrincipal User user) {

        UserStats stats = statsRepo.findByUser(user).orElse(null);

        // Lesson counts
        long grammarCompleted     = countCompleted(user, "GRAMMAR");
        long vocabCompleted       = countCompleted(user, "VOCABULARY");
        long pronCompleted        = countCompleted(user, "PRONUNCIATION");
        long totalLessonsCompleted = grammarCompleted + vocabCompleted + pronCompleted;
        long totalLessons          = lessonRepo.count();

        // Interview
        long completedInterviews = interviewRepo.countByUserAndStatus(user, "COMPLETED");
        OptionalDouble avgInterviewScore = interviewRepo.findByUserAndStatus(user, "COMPLETED")
                .stream().filter(s -> s.getOverallScore() != null)
                .mapToInt(InterviewSession::getOverallScore).average();

        // GD
        long completedGds = gdRepo.countByUserAndStatus(user, "COMPLETED");
        OptionalDouble avgGdScore = gdRepo.findByUserOrderByStartedAtDesc(user)
                .stream().filter(s -> "COMPLETED".equals(s.getStatus()) && s.getOverallScore() != null)
                .mapToInt(GdSession::getOverallScore).average();

        // MCQ
        long completedMcqs = mcqRepo.countByUserAndStatus(user, "SUBMITTED");
        OptionalDouble avgMcqScore = mcqRepo.findByUserAndStatus(user, "SUBMITTED")
                .stream().filter(s -> s.getScore() != null)
                .mapToDouble(s -> s.getScore() * 10.0).average();

        // Badges
        long badgeCount = userBadgeRepo.countByUser(user);

        // Study time
        long studyMinutes = stats != null ? stats.getTotalStudyTimeSeconds() / 60 : 0;

        // Recent MCQ performance (last 7 days)
        List<Map<String, Object>> mcqWeekly = buildMcqWeekly(user);

        // Skill breakdown (from lesson completion %)
        long grammarTotal = lessonRepo.findByModuleOrderByOrderIndexAsc("GRAMMAR").size();
        long vocabTotal   = lessonRepo.findByModuleOrderByOrderIndexAsc("VOCABULARY").size();
        long pronTotal    = lessonRepo.findByModuleOrderByOrderIndexAsc("PRONUNCIATION").size();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("username",            user.getUsername());
        response.put("learningLevel",       user.getLearningLevel());
        response.put("currentStreak",       stats != null ? stats.getCurrentStreak() : 0);
        response.put("longestStreak",       stats != null ? stats.getLongestStreak() : 0);
        response.put("totalStudyMinutes",   studyMinutes);
        response.put("xp",                  stats != null ? stats.getXp() : 0);
        response.put("confidenceScore",     stats != null ? stats.getConfidenceScore() : 60);
        response.put("totalLessonsCompleted", totalLessonsCompleted);
        response.put("totalLessons",         totalLessons);
        response.put("grammarCompleted",     grammarCompleted);
        response.put("vocabCompleted",       vocabCompleted);
        response.put("pronCompleted",        pronCompleted);
        response.put("grammarPercent",       pct(grammarCompleted, grammarTotal));
        response.put("vocabPercent",         pct(vocabCompleted, vocabTotal));
        response.put("pronPercent",          pct(pronCompleted, pronTotal));
        response.put("completedInterviews",  completedInterviews);
        response.put("avgInterviewScore",    avgInterviewScore.isPresent() ? (int) avgInterviewScore.getAsDouble() : 0);
        response.put("completedGds",         completedGds);
        response.put("avgGdScore",           avgGdScore.isPresent() ? (int) avgGdScore.getAsDouble() : 0);
        response.put("completedMcqs",        completedMcqs);
        response.put("avgMcqPercent",        avgMcqScore.isPresent() ? (int) avgMcqScore.getAsDouble() : 0);
        response.put("badgeCount",           badgeCount);
        response.put("mcqWeekly",            mcqWeekly);

        List<Map<String, Object>> recentInterviews = interviewRepo.findByUserAndStatus(user, "COMPLETED").stream()
                .sorted(Comparator.comparing(InterviewSession::getEndedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("type", s.getInterviewType());
                    m.put("score", s.getOverallScore());
                    m.put("date", s.getEndedAt() != null ? s.getEndedAt().toString() : "");
                    return m;
                })
                .collect(Collectors.toList());
        response.put("recentInterviews", recentInterviews);
        response.put("monthlyActivity", buildMonthlyActivity(user));

        return ResponseEntity.ok(response);
    }

    // ── GET /api/progress/badges ──────────────────────────────────────────────
    @GetMapping("/badges")
    public ResponseEntity<?> getBadges(@AuthenticationPrincipal User user) {
        List<UserBadge> earned = badgeService.getUserBadges(user);
        List<Map<String, Object>> result = earned.stream().map(ub -> {
            Badge b = ub.getBadge();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code",        b.getCode());
            m.put("name",        b.getName());
            m.put("description", b.getDescription());
            m.put("icon",        b.getIcon());
            m.put("category",    b.getCategory());
            m.put("earnedAt",    ub.getEarnedAt().toString());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("badges", result, "total", result.size()));
    }

    // ── POST /api/progress/update-streak ──────────────────────────────────────
    @PostMapping("/update-streak")
    public ResponseEntity<?> updateStreak(@AuthenticationPrincipal User user) {
        UserStats stats = statsRepo.findByUser(user)
                .orElse(UserStats.builder().user(user).currentStreak(0).longestStreak(0)
                        .totalStudyTimeSeconds(0).xp(0).confidenceScore(60).build());

        LocalDate today = LocalDate.now();
        LocalDate lastActivity = stats.getLastActivityDate();

        if (lastActivity == null || lastActivity.isBefore(today.minusDays(1))) {
            // Streak broken or first activity
            if (lastActivity != null && lastActivity.equals(today.minusDays(1))) {
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            } else {
                stats.setCurrentStreak(1);
            }
        } else if (lastActivity.equals(today)) {
            // Already counted today — no change
        }

        if (stats.getCurrentStreak() > stats.getLongestStreak()) {
            stats.setLongestStreak(stats.getCurrentStreak());
        }

        stats.setLastActivityDate(today);
        stats.setXp(stats.getXp() + 10); // 10 XP per activity
        statsRepo.save(stats);

        badgeService.checkAndAwardBadges(user);

        return ResponseEntity.ok(Map.of(
                "currentStreak", stats.getCurrentStreak(),
                "longestStreak", stats.getLongestStreak(),
                "xp",            stats.getXp()
        ));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private long countCompleted(User user, String module) {
        return lessonProgressRepo.findByUserAndLesson_Module(user, module).stream()
                .filter(p -> "COMPLETED".equals(p.getStatus())).count();
    }

    private int pct(long completed, long total) {
        if (total == 0) return 0;
        return (int) Math.min(100, (completed * 100) / total);
    }

    private List<Map<String, Object>> buildMcqWeekly(User user) {
        List<Map<String, Object>> week = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            Optional<McqSession> session = mcqRepo.findByUserAndSessionDate(user, day);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date",      day.toString());
            entry.put("dayLabel",  day.getDayOfWeek().toString().substring(0, 3));
            entry.put("completed", session.map(s -> "SUBMITTED".equals(s.getStatus())).orElse(false));
            entry.put("score",     session.filter(s -> s.getScore() != null).map(McqSession::getScore).orElse(0));
            week.add(entry);
        }
        return week;
    }

    private List<Map<String, Object>> buildMonthlyActivity(User user) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 4; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            String monthLabel = monthDate.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
            
            LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(23, 59, 59);
            
            long lessons = lessonProgressRepo.findByUser(user).stream()
                    .filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()) && p.getUpdatedAt() != null &&
                            !p.getUpdatedAt().isBefore(start) && !p.getUpdatedAt().isAfter(end))
                    .count();
            
            long quizzes = mcqRepo.findByUserOrderBySessionDateDesc(user).stream()
                    .filter(s -> "SUBMITTED".equalsIgnoreCase(s.getStatus()) && s.getSubmittedAt() != null &&
                            !s.getSubmittedAt().isBefore(start) && !s.getSubmittedAt().isAfter(end))
                    .count();
            
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("monthLabel", monthLabel);
            m.put("lessons", lessons);
            m.put("quizzes", quizzes);
            list.add(m);
        }
        return list;
    }
}
