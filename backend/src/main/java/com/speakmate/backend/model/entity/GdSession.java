package com.speakmate.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gd_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GdSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String topic;

    /** JSON array — full discussion transcript [{speaker, content, timestamp}] */
    @Column(columnDefinition = "TEXT")
    private String transcript;

    /** ACTIVE | COMPLETED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    // ── Report scores ───────────────────────────────────────────────────────
    private Integer overallScore;
    private Integer grammarScore;
    private Integer fluencyScore;
    private Integer vocabularyScore;
    private Integer pronunciationScore;
    private Integer confidenceScore;

    /** Seconds the user spoke */
    @Column(name = "speaking_time_seconds")
    private Integer speakingTimeSeconds;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }
}
