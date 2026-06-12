package com.reviora.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "complexity_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplexityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "time_complexity", nullable = false, length = 30)
    private String timeComplexity;

    @Column(name = "space_complexity", nullable = false, length = 30)
    @Builder.Default
    private String spaceComplexity = "O(1)";

    @Column(length = 50)
    private String pattern;

    @Column(precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal confidence = BigDecimal.valueOf(0.85);

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "nesting_depth")
    @Builder.Default
    private int nestingDepth = 0;

    @Column(name = "loop_count")
    @Builder.Default
    private int loopCount = 0;

    @Column(name = "recursion_detected")
    @Builder.Default
    private boolean recursionDetected = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
