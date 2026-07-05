package com.speakmate.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "badges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;           // e.g. "FIRST_LESSON", "STREAK_7"

    @Column(nullable = false, length = 100)
    private String name;           // e.g. "First Step"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String icon;           // material-symbols name e.g. "military_tech"

    @Column(length = 20)
    private String category;       // STREAK, LESSON, INTERVIEW, GD, MCQ, GENERAL
}
