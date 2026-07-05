package com.speakmate.backend.controller;

import com.speakmate.backend.model.entity.*;
import com.speakmate.backend.repository.*;
import com.speakmate.backend.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserRepository userRepo;
    private final UserStatsRepository statsRepo;
    private final UserLessonProgressRepository lessonProgressRepo;
    private final InterviewSessionRepository interviewRepo;
    private final GdSessionRepository gdRepo;
    private final McqSessionRepository mcqRepo;
    private final BadgeService badgeService;
    private final PasswordEncoder passwordEncoder;
    private final LessonRepository lessonRepo;

    // ── GET /api/profile ──────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {

        UserStats stats = statsRepo.findByUser(user).orElse(null);

        // Recalculate streak from real activity before returning
        stats = recalculateStreak(user, stats);

        // Lesson progress
        long grammarCompleted = countCompleted(user, "GRAMMAR");
        long vocabCompleted   = countCompleted(user, "VOCABULARY");
        long pronCompleted    = countCompleted(user, "PRONUNCIATION");
        long totalCompleted   = grammarCompleted + vocabCompleted + pronCompleted;
        long totalLessons     = lessonRepo.count();

        int grammarPct = pct(grammarCompleted, lessonRepo.findByModuleOrderByOrderIndexAsc("GRAMMAR").size());
        int vocabPct   = pct(vocabCompleted,   lessonRepo.findByModuleOrderByOrderIndexAsc("VOCABULARY").size());
        int pronPct    = pct(pronCompleted,     lessonRepo.findByModuleOrderByOrderIndexAsc("PRONUNCIATION").size());
        int overallPct = pct(totalCompleted, totalLessons);

        // Interview best score
        OptionalInt bestInterviewScore = interviewRepo.findByUserAndStatus(user, "COMPLETED")
                .stream().filter(s -> s.getOverallScore() != null)
                .mapToInt(InterviewSession::getOverallScore).max();

        // MCQ avg
        OptionalDouble avgMcqScore = mcqRepo.findByUserAndStatus(user, "SUBMITTED")
                .stream().filter(s -> s.getScore() != null)
                .mapToDouble(s -> s.getScore() * 10.0).average();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id",                  user.getId());
        response.put("username",            user.getUsername());
        response.put("email",               user.getEmail());
        response.put("fullName",            user.getFullName() != null ? user.getFullName() : "");
        response.put("phone",               user.getPhone()    != null ? user.getPhone()    : "");
        response.put("address",             user.getAddress()  != null ? user.getAddress()  : "");
        response.put("bio",                 user.getBio()      != null ? user.getBio()       : "");
        response.put("profilePictureUrl",   user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : "");
        response.put("learningLevel",       user.getLearningLevel());
        response.put("memberSince",         user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "");
        response.put("currentStreak",       stats != null ? stats.getCurrentStreak() : 0);
        response.put("longestStreak",       stats != null ? stats.getLongestStreak()  : 0);
        response.put("xp",                  stats != null ? stats.getXp()             : 0);
        response.put("confidenceScore",     stats != null ? stats.getConfidenceScore(): 60);
        response.put("studyMinutes",        stats != null ? stats.getTotalStudyTimeSeconds() / 60 : 0);
        response.put("overallProgress",     overallPct);
        response.put("grammarPercent",      grammarPct);
        response.put("vocabPercent",        vocabPct);
        response.put("pronPercent",         pronPct);
        response.put("completedLessons",    totalCompleted);
        response.put("totalLessons",        totalLessons);
        response.put("completedInterviews", interviewRepo.countByUserAndStatus(user, "COMPLETED"));
        response.put("completedGds",        gdRepo.countByUserAndStatus(user, "COMPLETED"));
        response.put("completedMcqs",       mcqRepo.countByUserAndStatus(user, "SUBMITTED"));
        response.put("bestInterviewScore",  bestInterviewScore.isPresent() ? bestInterviewScore.getAsInt() : 0);
        response.put("avgMcqPercent",       avgMcqScore.isPresent() ? (int) avgMcqScore.getAsDouble() : 0);

        return ResponseEntity.ok(response);
    }

    // ── PATCH /api/profile ────────────────────────────────────────────────────
    @PatchMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> updates) {

        boolean changed = false;

        // Username
        String newUsername = updates.get("username");
        if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(user.getUsername())) {
            if (userRepo.existsByUsername(newUsername)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username already taken"));
            }
            user.setUsername(newUsername);
            changed = true;
        }

        // Full name
        String newFullName = updates.get("fullName");
        if (newFullName != null) {
            user.setFullName(newFullName.trim());
            changed = true;
        }

        // Phone
        String newPhone = updates.get("phone");
        if (newPhone != null) {
            user.setPhone(newPhone.trim());
            changed = true;
        }

        // Address
        String newAddress = updates.get("address");
        if (newAddress != null) {
            user.setAddress(newAddress.trim());
            changed = true;
        }

        // Bio
        String newBio = updates.get("bio");
        if (newBio != null) {
            user.setBio(newBio.trim());
            changed = true;
        }

        // Profile picture URL (data URI or external URL)
        String newPicUrl = updates.get("profilePictureUrl");
        if (newPicUrl != null && !newPicUrl.isBlank()) {
            user.setProfilePictureUrl(newPicUrl.trim());
            changed = true;
        }

        // Learning level
        String newLevel = updates.get("learningLevel");
        if (newLevel != null && List.of("A1","A2","B1","B2","C1","C2").contains(newLevel.toUpperCase())) {
            user.setLearningLevel(newLevel.toUpperCase());
            changed = true;
        }

        // Password (optional — only if provided)
        String newPassword = updates.get("password");
        if (newPassword != null && newPassword.length() >= 6) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            changed = true;
        }

        if (changed) userRepo.save(user);

        return ResponseEntity.ok(Map.of(
                "message",          "Profile updated successfully",
                "username",         user.getUsername(),
                "fullName",         user.getFullName()  != null ? user.getFullName() : "",
                "learningLevel",    user.getLearningLevel(),
                "profilePictureUrl",user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : ""
        ));
    }

    // ── Streak recalculation from real activity ───────────────────────────────
    /**
     * Derives the current streak from actual learning activity:
     * - Lesson completions (any module)
     * - MCQ sessions submitted today or past days
     * - Completed interviews
     * - Completed GD sessions
     *
     * Looks back up to 365 days and counts consecutive days with any activity.
     * Persists the updated streak so it's visible on next load.
     */
    private UserStats recalculateStreak(User user, UserStats stats) {
        if (stats == null) return null;
        try {
            // Build a set of dates that had any learning activity
            Set<LocalDate> activeDates = new HashSet<>();

            // Lesson completion dates (from updatedAt)
            lessonProgressRepo.findAll().stream()
                    .filter(p -> p.getUser().getId().equals(user.getId()) && "COMPLETED".equals(p.getStatus()))
                    .forEach(p -> {
                        if (p.getUpdatedAt() != null) activeDates.add(p.getUpdatedAt().toLocalDate());
                        else if (p.getCreatedAt() != null) activeDates.add(p.getCreatedAt().toLocalDate());
                    });

            // MCQ submission dates
            mcqRepo.findByUserAndStatus(user, "SUBMITTED")
                    .forEach(s -> { if (s.getSessionDate() != null) activeDates.add(s.getSessionDate()); });

            // Interview completion dates
            interviewRepo.findByUserAndStatus(user, "COMPLETED")
                    .forEach(s -> { if (s.getEndedAt() != null) activeDates.add(s.getEndedAt().toLocalDate()); });

            // GD session completion dates
            gdRepo.findByUserOrderByStartedAtDesc(user).stream()
                    .filter(s -> "COMPLETED".equals(s.getStatus()))
                    .forEach(s -> { if (s.getEndedAt() != null) activeDates.add(s.getEndedAt().toLocalDate()); });

            // Count streak: consecutive days ending today (or yesterday if nothing yet today)
            LocalDate today = LocalDate.now();
            int streak = 0;
            LocalDate check = activeDates.contains(today) ? today : today.minusDays(1);

            while (activeDates.contains(check)) {
                streak++;
                check = check.minusDays(1);
                if (streak > 365) break; // safety cap
            }

            if (streak != stats.getCurrentStreak()) {
                stats.setCurrentStreak(streak);
                if (streak > stats.getLongestStreak()) stats.setLongestStreak(streak);
                stats.setLastActivityDate(activeDates.contains(today) ? today : null);
                statsRepo.save(stats);
            }
        } catch (Exception e) {
            log.error("Streak recalculation error: {}", e.getMessage());
        }
        return stats;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private long countCompleted(User user, String module) {
        return lessonProgressRepo.findByUserAndLesson_Module(user, module).stream()
                .filter(p -> "COMPLETED".equals(p.getStatus())).count();
    }

    private int pct(long done, long total) {
        if (total == 0) return 0;
        return (int) Math.min(100, (done * 100) / total);
    }
}
