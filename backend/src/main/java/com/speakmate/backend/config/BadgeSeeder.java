package com.speakmate.backend.config;

import com.speakmate.backend.model.entity.Badge;
import com.speakmate.backend.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class BadgeSeeder implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    @Override
    public void run(String... args) {
        seedBadge("FIRST_LESSON",      "First Step",           "Complete your very first lesson",                             "school",                "LESSON");
        seedBadge("LESSON_5",          "Eager Learner",        "Complete 5 lessons",                                          "menu_book",             "LESSON");
        seedBadge("LESSON_10",         "Dedicated Student",    "Complete 10 lessons",                                         "auto_stories",          "LESSON");
        seedBadge("LESSON_25",         "Knowledge Seeker",     "Complete 25 lessons",                                         "local_library",         "LESSON");
        seedBadge("LESSON_50",         "Scholar",              "Complete 50 lessons",                                         "workspace_premium",     "LESSON");
        seedBadge("STREAK_3",          "On a Roll",            "Maintain a 3-day learning streak",                            "local_fire_department", "STREAK");
        seedBadge("STREAK_7",          "Week Warrior",         "Maintain a 7-day learning streak",                            "whatshot",              "STREAK");
        seedBadge("STREAK_30",         "Monthly Master",       "Maintain a 30-day learning streak",                           "emoji_events",          "STREAK");
        seedBadge("FIRST_INTERVIEW",   "Interview Ready",      "Complete your first mock interview",                          "work",                  "INTERVIEW");
        seedBadge("INTERVIEW_5",       "Interview Pro",        "Complete 5 mock interviews",                                  "psychology",            "INTERVIEW");
        seedBadge("INTERVIEW_SCORE_80","Top Performer",        "Score 80% or higher in an interview",                        "military_tech",         "INTERVIEW");
        seedBadge("FIRST_GD",          "Discussion Starter",   "Complete your first group discussion",                        "groups",                "GD");
        seedBadge("GD_5",              "Group Leader",         "Complete 5 group discussions",                                "record_voice_over",     "GD");
        seedBadge("MCQ_10",            "Quiz Beginner",        "Complete 10 daily MCQ sessions",                              "quiz",                  "MCQ");
        seedBadge("MCQ_PERFECT",       "Perfect Score",        "Score 10/10 on a daily MCQ",                                  "verified",              "MCQ");
        seedBadge("MCQ_STREAK_7",      "Daily Grinder",        "Complete MCQs 7 days in a row",                               "bolt",                  "MCQ");
        seedBadge("WEEKLY_ASSESSMENT", "Weekly Champion",      "Complete a full weekly assessment",                           "assignment_turned_in",  "GENERAL");
        seedBadge("ALL_MODULES",       "All-Rounder",          "Try all 6 learning modules at least once",                    "stars",                 "GENERAL");
        log.info("Badge seeding complete. Total badges: {}", badgeRepository.count());
    }

    private void seedBadge(String code, String name, String desc, String icon, String category) {
        if (badgeRepository.findByCode(code).isEmpty()) {
            badgeRepository.save(Badge.builder()
                    .code(code).name(name).description(desc).icon(icon).category(category)
                    .build());
        }
    }
}
