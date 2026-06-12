package com.reviora.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_analytics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_submissions", nullable = false)
    @Builder.Default
    private int totalSubmissions = 0;

    @Column(name = "accepted_submissions", nullable = false)
    @Builder.Default
    private int acceptedSubmissions = 0;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private int longestStreak = 0;

    @Column(name = "average_complexity_score", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal averageComplexityScore = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
