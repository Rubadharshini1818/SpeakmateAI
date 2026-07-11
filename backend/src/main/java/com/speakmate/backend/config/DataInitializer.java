package com.speakmate.backend.config;

import com.speakmate.backend.model.entity.Badge;
import com.speakmate.backend.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final BadgeRepository badgeRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing database with default data...");
            seedBadges();
            log.info("Database initialization complete.");
        };
    }

    private void seedBadges() {
        if (badgeRepository.count() == 0) {
            log.info("Seeding initial badges...");
            List<Badge> defaultBadges = List.of(
                    Badge.builder().code("FIRST_LESSON").name("First Step").description("Completed your first lesson").icon("school").category("LESSON").build(),
                    Badge.builder().code("LESSON_5").name("Knowledge Seeker").description("Completed 5 lessons").icon("menu_book").category("LESSON").build(),
                    Badge.builder().code("LESSON_10").name("Dedicated Learner").description("Completed 10 lessons").icon("auto_stories").category("LESSON").build(),
                    Badge.builder().code("LESSON_25").name("Learning Machine").description("Completed 25 lessons").icon("rocket_launch").category("LESSON").build(),
                    Badge.builder().code("LESSON_50").name("Master Student").description("Completed 50 lessons").icon("military_tech").category("LESSON").build(),
                    Badge.builder().code("STREAK_3").name("On a Roll").description("Maintained a 3-day streak").icon("local_fire_department").category("STREAK").build(),
                    Badge.builder().code("STREAK_7").name("Week Warrior").description("Maintained a 7-day streak").icon("whatshot").category("STREAK").build(),
                    Badge.builder().code("STREAK_30").name("Unstoppable").description("Maintained a 30-day streak").icon("bolt").category("STREAK").build(),
                    Badge.builder().code("FIRST_INTERVIEW").name("First Interview").description("Completed your first mock interview").icon("work").category("INTERVIEW").build(),
                    Badge.builder().code("INTERVIEW_5").name("Interview Pro").description("Completed 5 mock interviews").icon("record_voice_over").category("INTERVIEW").build(),
                    Badge.builder().code("INTERVIEW_SCORE_80").name("High Scorer").description("Scored 80+ in a mock interview").icon("emoji_events").category("INTERVIEW").build(),
                    Badge.builder().code("FIRST_GD").name("Group Discussion").description("Participated in your first GD session").icon("groups").category("GD").build(),
                    Badge.builder().code("GD_5").name("Discussion Leader").description("Completed 5 GD sessions").icon("forum").category("GD").build(),
                    Badge.builder().code("MCQ_10").name("Quiz Veteran").description("Submitted 10 daily quizzes").icon("quiz").category("MCQ").build(),
                    Badge.builder().code("MCQ_PERFECT").name("Perfect Score").description("Got 10/10 on a daily quiz").icon("star").category("MCQ").build(),
                    Badge.builder().code("MCQ_STREAK_7").name("Quiz Streak").description("Completed quizzes 7 days in a row").icon("calendar_month").category("MCQ").build(),
                    Badge.builder().code("ALL_MODULES").name("All-Rounder").description("Used all 4 learning modules").icon("verified").category("GENERAL").build()
            );
            badgeRepository.saveAll(defaultBadges);
            log.info("Successfully seeded {} badges.", defaultBadges.size());
        } else {
            log.info("Badges already exist, skipping seeding.");
        }
    }
}
