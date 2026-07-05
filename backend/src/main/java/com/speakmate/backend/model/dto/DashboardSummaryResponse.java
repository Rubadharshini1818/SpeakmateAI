package com.speakmate.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private String username;
    private String learningLevel;
    private long studyTimeMinutes;
    private int dailyGoalMinutes;
    private int currentStreak;
    private int longestStreak;
    private int confidenceScore;
    private int xp;
    private String aiSuggestion;
    private List<Map<String, String>> recentActivity;
    private List<Map<String, String>> latestBadges;
}
