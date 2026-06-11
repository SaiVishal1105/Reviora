package com.reviora.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "growth_data")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GrowthData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "input_size", nullable = false)
    private int inputSize;

    @Column(nullable = false)
    private long operations;

    @Column(name = "execution_time_ms", nullable = false, precision = 10, scale = 3)
    private BigDecimal executionTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
