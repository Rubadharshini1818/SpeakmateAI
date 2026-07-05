package com.speakmate.backend.controller;

import com.speakmate.backend.model.dto.DashboardSummaryResponse;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.model.entity.UserStats;
import com.speakmate.backend.repository.UserRepository;
import com.speakmate.backend.repository.UserStatsRepository;
import com.speakmate.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary(@AuthenticationPrincipal User authUser) {
        User user = userRepository.findById(authUser.getId()).orElse(authUser);
        UserStats stats = userStatsRepository.findByUser(user)
                .orElseGet(() -> {
                    // Fallback create stats if not present
                    UserStats s = UserStats.builder()
                            .user(user)
                            .currentStreak(0)
                            .longestStreak(0)
                            .totalStudyTimeSeconds(0)
                            .xp(0)
                            .confidenceScore(60)
                            .build();
                    return userStatsRepository.save(s);
                });

        // Request Gemini for a personalized dashboard tip/suggestion
        String systemPrompt = "You are SpeakMate AI English Mentor. Analyze user learning parameters: "
                + "User grammar level is " + user.getLearningLevel() + ", confidence score is " + stats.getConfidenceScore() + "/100. "
                + "Provide a short, encouraging one-sentence suggestion (maximum 20 words) for their dashboard home feed. "
                + "Surround key lesson recommendations in strong tags, e.g. 'Try the <strong>Past Narrative</strong> lesson'. Keep it concise and motivational.";
        
        String suggestionPrompt = "Give me today's mentor tip.";
        String aiSuggestion = geminiService.generateContent(systemPrompt, suggestionPrompt);

        // Fetch recent activity logs (mock/default data if database tables are not fully seeded yet)
        List<Map<String, String>> activities = new ArrayList<>();
        activities.add(Map.of(
                "time", "Today, 09:45 AM",
                "text", "Completed 'Daily Vocabulary Quiz' with a score of 95%."
        ));
        activities.add(Map.of(
                "time", "Yesterday, 06:12 PM",
                "text", "Practiced 'Presentation Delivery' with AI Friend."
        ));
        activities.add(Map.of(
                "time", "2 Days Ago",
                "text", "Unlocked 'Eloquent Speaker' badge."
        ));

        // Fetch badges (mock default list for the UI display)
        List<Map<String, String>> badges = new ArrayList<>();
        badges.add(Map.of(
                "name", "Early Bird",
                "icon", "military_tech",
                "desc", "Practice before 8 AM"
        ));
        badges.add(Map.of(
                "name", "Quiz Master",
                "icon", "emoji_events",
                "desc", "Complete 10 Daily MCQs"
        ));
        badges.add(Map.of(
                "name", "Grammar Pro",
                "icon", "psychology",
                "desc", "Score 100% on a Grammar lesson"
        ));
        badges.add(Map.of(
                "name", "Verified Learner",
                "icon", "verified",
                "desc", "Linked Google Account"
        ));

        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .username(user.getUsername())
                .learningLevel(user.getLearningLevel())
                .studyTimeMinutes(stats.getTotalStudyTimeSeconds() / 60)
                .dailyGoalMinutes(60) // default static daily goal
                .currentStreak(stats.getCurrentStreak())
                .longestStreak(stats.getLongestStreak())
                .confidenceScore(stats.getConfidenceScore())
                .xp(stats.getXp())
                .aiSuggestion(aiSuggestion)
                .recentActivity(activities)
                .latestBadges(badges)
                .build();

        return ResponseEntity.ok(response);
    }
}
