package com.speakmate.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String module; // GRAMMAR, VOCABULARY, PRONUNCIATION

    @Column(nullable = false)
    private String section; // e.g. "Tenses"

    @Column(nullable = false)
    private String title; // e.g. "Simple Present"

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content; // Storing as JSON string for flexibility
}
