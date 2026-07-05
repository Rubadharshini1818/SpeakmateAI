package com.speakmate.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "total_study_time_seconds", nullable = false)
    private long totalStudyTimeSeconds;

    @Column(name = "xp", nullable = false)
    private int xp;

    @Column(name = "confidence_score", nullable = false)
    private int confidenceScore;

    @PrePersist
    protected void onCreate() {
        if (this.currentStreak == 0) this.currentStreak = 0;
        if (this.longestStreak == 0) this.longestStreak = 0;
        if (this.totalStudyTimeSeconds == 0) this.totalStudyTimeSeconds = 0;
        if (this.xp == 0) this.xp = 0;
        if (this.confidenceScore == 0) this.confidenceScore = 60; // baseline
    }
}
