package com.reviora.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardDto {
    private int totalSubmissions;
    private int acceptedSubmissions;
    private int currentStreak;
    private int longestStreak;
    private BigDecimal averageComplexityScore;
    private List<TopicPerformanceDto> topicsPerformance;
    private List<SubmissionSummaryDto> recentSubmissions;
}
