package com.reviora.controller;

import com.reviora.dto.AIReviewDto;
import com.reviora.dto.ComplexityReportDto;
import com.reviora.dto.TestCaseDto;
import com.reviora.exception.ResourceNotFoundException;
import com.reviora.model.Submission;
import com.reviora.repository.SubmissionRepository;
import com.reviora.service.AIService;
import com.reviora.service.ComplexityAnalysisEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI", description = "AI-powered code review and test generation")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final SubmissionRepository submissionRepository;
    private final ComplexityAnalysisEngine complexityEngine;

    @PostMapping("/review/{submissionId}")
    @Operation(summary = "Get AI optimization review for a submission")
    public ResponseEntity<AIReviewDto> review(
            @PathVariable UUID submissionId,
            Authentication auth) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        // Get complexity first to give AI context
        ComplexityReportDto complexity = complexityEngine.analyze(
                submission.getCode(), submission.getLanguage());

        AIReviewDto review = aiService.generateReview(
                submission.getCode(),
                submission.getLanguage(),
                complexity.getTimeComplexity());

        return ResponseEntity.ok(review);
    }

    @PostMapping("/tests/{submissionId}")
    @Operation(summary = "Generate test cases for a submission")
    public ResponseEntity<List<TestCaseDto>> generateTests(
            @PathVariable UUID submissionId,
            Authentication auth) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        List<TestCaseDto> tests = aiService.generateTestCases(
                submission.getCode(), submission.getLanguage());

        return ResponseEntity.ok(tests);
    }
}
