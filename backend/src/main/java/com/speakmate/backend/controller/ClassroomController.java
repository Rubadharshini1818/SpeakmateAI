package com.speakmate.backend.controller;

import com.speakmate.backend.model.dto.LessonResponseDto;
import com.speakmate.backend.model.dto.SectionDto;
import com.speakmate.backend.model.entity.Lesson;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.model.entity.UserLessonProgress;
import com.speakmate.backend.model.entity.UserStats;
import com.speakmate.backend.repository.LessonRepository;
import com.speakmate.backend.repository.UserLessonProgressRepository;
import com.speakmate.backend.repository.UserStatsRepository;
import com.speakmate.backend.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository progressRepository;
    private final UserStatsRepository statsRepository;
    private final BadgeService badgeService;

    @GetMapping("/lessons")
    public ResponseEntity<List<SectionDto>> getLessonsByModule(
            @RequestParam String module,
            @AuthenticationPrincipal User user) {

        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module.toUpperCase());
        List<UserLessonProgress> progresses = progressRepository.findByUserAndLesson_Module(user, module.toUpperCase());

        Map<UUID, String> progressMap = progresses.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), UserLessonProgress::getStatus));

        // Group by section, preserving insertion order
        Map<String, List<LessonResponseDto>> sectionsMap = new LinkedHashMap<>();

        for (Lesson lesson : lessons) {
            sectionsMap.putIfAbsent(lesson.getSection(), new ArrayList<>());
            sectionsMap.get(lesson.getSection()).add(LessonResponseDto.builder()
                    .id(lesson.getId())
                    .title(lesson.getTitle())
                    .status(progressMap.getOrDefault(lesson.getId(), "NOT_STARTED"))
                    .content(lesson.getContent())
                    .build());
        }

        List<SectionDto> sectionDtos = sectionsMap.entrySet().stream()
                .map(entry -> SectionDto.builder()
                        .section(entry.getKey())
                        .lessons(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(sectionDtos);
    }

    @PostMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable UUID lessonId,
            @RequestParam String status,
            @AuthenticationPrincipal User user) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Optional<UserLessonProgress> existingOpt = progressRepository.findByUserAndLesson(user, lesson);
        boolean isNewlyCompleted = false;

        UserLessonProgress progress;
        if (existingOpt.isPresent()) {
            progress = existingOpt.get();
            if ("COMPLETED".equalsIgnoreCase(status) && !"COMPLETED".equalsIgnoreCase(progress.getStatus())) {
                isNewlyCompleted = true;
            }
        } else {
            progress = UserLessonProgress.builder()
                    .user(user)
                    .lesson(lesson)
                    .build();
            if ("COMPLETED".equalsIgnoreCase(status)) {
                isNewlyCompleted = true;
            }
        }

        progress.setStatus(status.toUpperCase());
        progressRepository.save(progress);

        if (isNewlyCompleted) {
            UserStats stats = statsRepository.findByUser(user).orElseGet(() -> UserStats.builder()
                    .user(user)
                    .xp(0)
                    .totalStudyTimeSeconds(0)
                    .confidenceScore(60)
                    .build());
            stats.setXp(stats.getXp() + 50);
            stats.setTotalStudyTimeSeconds(stats.getTotalStudyTimeSeconds() + 300); // 5 mins
            statsRepository.save(stats);

            badgeService.checkAndAwardBadges(user);
        }

        return ResponseEntity.ok().build();
    }
}
