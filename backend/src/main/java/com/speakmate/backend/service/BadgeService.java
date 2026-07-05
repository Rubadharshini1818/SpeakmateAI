package com.speakmate.backend.service;

import com.speakmate.backend.model.entity.*;
import com.speakmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserLessonProgressRepository lessonProgressRepo;
    private final InterviewSessionRepository interviewRepo;
    private final GdSessionRepository gdRepo;
    private final McqSessionRepository mcqRepo;
    private final UserStatsRepository statsRepo;

    /**
     * Evaluates all badge milestones for this user and awards any newly earned ones.
     * Safe to call after any activity — it never double-awards.
     */
    @Transactional
    public void checkAndAwardBadges(User user) {
        try {
            // ── Lesson badges ─────────────────────────────────────────────
            long completedLessons = lessonProgressRepo.findByUserAndLesson_Module(user, "GRAMMAR").stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus())).count()
                + lessonProgressRepo.findByUserAndLesson_Module(user, "VOCABULARY").stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus())).count()
                + lessonProgressRepo.findByUserAndLesson_Module(user, "PRONUNCIATION").stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus())).count();

            if (completedLessons >= 1)  award(user, "FIRST_LESSON");
            if (completedLessons >= 5)  award(user, "LESSON_5");
            if (completedLessons >= 10) award(user, "LESSON_10");
            if (completedLessons >= 25) award(user, "LESSON_25");
            if (completedLessons >= 50) award(user, "LESSON_50");

            // ── Streak badges ─────────────────────────────────────────────
            statsRepo.findByUser(user).ifPresent(stats -> {
                int streak = stats.getCurrentStreak();
                if (streak >= 3)  award(user, "STREAK_3");
                if (streak >= 7)  award(user, "STREAK_7");
                if (streak >= 30) award(user, "STREAK_30");
            });

            // ── Interview badges ──────────────────────────────────────────
            long completedInterviews = interviewRepo.countByUserAndStatus(user, "COMPLETED");
            if (completedInterviews >= 1) award(user, "FIRST_INTERVIEW");
            if (completedInterviews >= 5) award(user, "INTERVIEW_5");

            interviewRepo.findByUserAndStatus(user, "COMPLETED").stream()
                    .filter(s -> s.getOverallScore() != null && s.getOverallScore() >= 80)
                    .findFirst()
                    .ifPresent(s -> award(user, "INTERVIEW_SCORE_80"));

            // ── GD badges ─────────────────────────────────────────────────
            long completedGds = gdRepo.countByUserAndStatus(user, "COMPLETED");
            if (completedGds >= 1) award(user, "FIRST_GD");
            if (completedGds >= 5) award(user, "GD_5");

            // ── MCQ badges ────────────────────────────────────────────────
            long submittedMcqs = mcqRepo.countByUserAndStatus(user, "SUBMITTED");
            if (submittedMcqs >= 10) award(user, "MCQ_10");

            mcqRepo.findByUserAndStatus(user, "SUBMITTED").stream()
                    .filter(s -> s.getScore() != null && s.getScore() == 10)
                    .findFirst()
                    .ifPresent(s -> award(user, "MCQ_PERFECT"));

            // MCQ 7-day streak: check last 7 calendar days
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean mcqStreak7 = true;
            for (int i = 0; i < 7; i++) {
                java.time.LocalDate day = today.minusDays(i);
                boolean done = mcqRepo.findByUserAndSessionDate(user, day)
                        .map(s -> "SUBMITTED".equals(s.getStatus())).orElse(false);
                if (!done) { mcqStreak7 = false; break; }
            }
            if (mcqStreak7) award(user, "MCQ_STREAK_7");

            // ── All-rounder badge ─────────────────────────────────────────
            boolean triedAllModules =
                    completedLessons >= 1 &&
                    completedInterviews >= 1 &&
                    completedGds >= 1 &&
                    submittedMcqs >= 1;
            if (triedAllModules) award(user, "ALL_MODULES");

        } catch (Exception e) {
            log.error("Badge check error for user {}: {}", user.getId(), e.getMessage());
        }
    }

    private void award(User user, String code) {
        if (!userBadgeRepository.existsByUserAndBadge_Code(user, code)) {
            badgeRepository.findByCode(code).ifPresent(badge -> {
                userBadgeRepository.save(UserBadge.builder().user(user).badge(badge).build());
                log.info("Badge '{}' awarded to user {}", code, user.getUsername());
            });
        }
    }

    public List<UserBadge> getUserBadges(User user) {
        return userBadgeRepository.findByUserOrderByEarnedAtDesc(user);
    }
}
