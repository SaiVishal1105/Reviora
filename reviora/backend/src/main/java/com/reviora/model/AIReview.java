package com.reviora.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AIReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "current_approach", columnDefinition = "TEXT")
    private String currentApproach;

    @Column(name = "suggested_optimization", columnDefinition = "TEXT")
    private String suggestedOptimization;

    @Column(name = "expected_complexity", length = 30)
    private String expectedComplexity;

    @Column(name = "interview_notes", columnDefinition = "TEXT")
    private String interviewNotes;

    @Column(name = "alternative_approaches", columnDefinition = "TEXT[]")
    private String[] alternativeApproaches;

    @Column(name = "code_quality_score", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal codeQualityScore = BigDecimal.valueOf(5.0);

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
