package com.speakmate.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weekly_assessments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "week_start"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Monday of the assessment week */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    /**
     * JSON object containing all 6 tasks:
     * {
     *   reading:    { sentences:[...], status, score, feedback },
     *   listening:  { sentences:[...], status, score, feedback },
     *   essay:      { topic, submission, status, score, feedback },
     *   errorSpot:  { sentences:[...], status, score, feedback },
     *   story:      { story, questions:[...], status, score, feedback },
     *   speech:     { topic, status, score, feedback }
     * }
     */
    @Column(name = "tasks_json", columnDefinition = "TEXT")
    private String tasksJson;

    // ── Final report ────────────────────────────────────────────────────────
    private Integer overallScore;
    private Integer readingScore;
    private Integer listeningScore;
    private Integer writingScore;
    private Integer errorSpottingScore;
    private Integer comprehensionScore;
    private Integer speechScore;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    /** ACTIVE | COMPLETED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
