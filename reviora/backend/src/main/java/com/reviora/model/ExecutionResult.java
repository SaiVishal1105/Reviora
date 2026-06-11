package com.reviora.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "execution_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExecutionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(columnDefinition = "TEXT")
    private String stdout;

    @Column(columnDefinition = "TEXT")
    private String stderr;

    @Column(name = "exit_code", nullable = false)
    @Builder.Default
    private int exitCode = 0;

    @Column(name = "execution_time", nullable = false)
    @Builder.Default
    private int executionTime = 0;

    @Column(name = "memory_used")
    private Long memoryUsed;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
