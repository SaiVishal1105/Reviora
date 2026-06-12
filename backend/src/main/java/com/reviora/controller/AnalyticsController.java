package com.reviora.controller;

import com.reviora.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "User performance analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    @GetMapping("/dashboard")
    @Operation(summary = "Get user dashboard analytics")
    public ResponseEntity<DashboardDto> getDashboard(Authentication auth) {
        // In a full implementation, this would query user_analytics and topic_performance tables
        // For now, returns structured mock data that mirrors what the frontend expects
        DashboardDto dashboard = DashboardDto.builder()
                .totalSubmissions(142)
                .acceptedSubmissions(118)
                .currentStreak(7)
                .longestStreak(23)
                .averageComplexityScore(BigDecimal.valueOf(7.4))
                .topicsPerformance(List.of(
                        TopicPerformanceDto.builder().topic("Arrays").solved(28).total(35).score(BigDecimal.valueOf(80)).build(),
                        TopicPerformanceDto.builder().topic("Strings").solved(18).total(22).score(BigDecimal.valueOf(82)).build(),
                        TopicPerformanceDto.builder().topic("Trees").solved(15).total(25).score(BigDecimal.valueOf(60)).build(),
                        TopicPerformanceDto.builder().topic("Graphs").solved(8).total(20).score(BigDecimal.valueOf(40)).build(),
                        TopicPerformanceDto.builder().topic("DP").solved(6).total(18).score(BigDecimal.valueOf(33)).build(),
                        TopicPerformanceDto.builder().topic("Sorting").solved(20).total(22).score(BigDecimal.valueOf(91)).build()
                ))
                .build();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/streak")
    @Operation(summary = "Get user streak data")
    public ResponseEntity<Object> getStreak(Authentication auth) {
        return ResponseEntity.ok(java.util.Map.of(
                "currentStreak", 7,
                "longestStreak", 23,
                "lastSubmissionDate", java.time.LocalDate.now().toString()
        ));
    }

    @GetMapping("/topics")
    @Operation(summary = "Get topic performance breakdown")
    public ResponseEntity<List<TopicPerformanceDto>> getTopics(Authentication auth) {
        return ResponseEntity.ok(List.of(
                TopicPerformanceDto.builder().topic("Arrays").solved(28).total(35).score(BigDecimal.valueOf(80)).build(),
                TopicPerformanceDto.builder().topic("Dynamic Programming").solved(6).total(18).score(BigDecimal.valueOf(33)).build(),
                TopicPerformanceDto.builder().topic("Graph Traversal").solved(8).total(20).score(BigDecimal.valueOf(40)).build()
        ));
    }
}
