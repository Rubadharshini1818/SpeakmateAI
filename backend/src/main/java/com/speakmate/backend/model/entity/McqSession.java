package com.speakmate.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mcq_sessions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "session_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class McqSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The calendar date this MCQ set belongs to */
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    /** JSON array of 10 questions:
     *  [{id, category, difficulty, question, options:[A,B,C,D], answer, explanation}] */
    @Column(columnDefinition = "TEXT")
    private String questionsJson;

    /** JSON object of submitted answers: {questionId: selectedOption, ...} */
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

    /** 0-10 */
    private Integer score;

    /** PENDING | SUBMITTED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "difficulty_used", length = 10)
    @Builder.Default
    private String difficultyUsed = "MEDIUM";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
