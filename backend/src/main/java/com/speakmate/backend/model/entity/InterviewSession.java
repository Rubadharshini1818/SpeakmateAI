package com.speakmate.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interview_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** HR, TECHNICAL, BEHAVIORAL, RESUME, COMPANY, MOCK */
    @Column(name = "interview_type", nullable = false, length = 30)
    private String interviewType;

    /** EASY, MEDIUM, HARD */
    @Column(nullable = false, length = 10)
    private String difficulty;

    /** Parsed resume text (plain text extracted from uploaded PDF/DOCX) */
    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;

    /** JSON array — the full Q&A transcript [{role, content}] */
    @Column(columnDefinition = "TEXT")
    private String transcript;

    /** ACTIVE | COMPLETED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    // ── Report scores (0-100, null until session ends) ──────────────────────
    private Integer overallScore;
    private Integer communicationScore;
    private Integer technicalScore;
    private Integer grammarScore;
    private Integer confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "ai_suggestions", columnDefinition = "TEXT")
    private String aiSuggestions;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }
}
