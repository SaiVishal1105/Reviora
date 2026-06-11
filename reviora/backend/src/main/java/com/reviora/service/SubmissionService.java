package com.reviora.service;

import com.reviora.dto.*;
import com.reviora.exception.ResourceNotFoundException;
import com.reviora.model.*;
import com.reviora.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final PistonService pistonService;
    private final ComplexityAnalysisEngine complexityEngine;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public SubmissionResponseDto submit(String userId, String code, String language, String stdin) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(UUID.fromString(userId)).orElse(null);
        }

        Submission submission = Submission.builder()
                .user(user)
                .code(code)
                .language(language)
                .stdin(stdin)
                .status(Submission.Status.RUNNING)
                .build();
        submission = submissionRepository.save(submission);

        ExecutionResultDto result;
        try {
            result = pistonService.execute(language, code, stdin);
            submission.setStatus(result.getExitCode() == 0
                    ? Submission.Status.COMPLETED
                    : Submission.Status.FAILED);
        } catch (Exception e) {
            log.error("Execution failed for {}: {}", submission.getId(), e.getMessage());
            result = ExecutionResultDto.builder()
                    .stdout("").stderr("Execution failed: " + e.getMessage())
                    .exitCode(-1).executionTime(0).build();
            submission.setStatus(Submission.Status.FAILED);
        }

        submissionRepository.save(submission);
        persistExecutionResult(submission, result);

        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .language(language)
                .status(submission.getStatus().name())
                .executionResult(result)
                .createdAt(submission.getCreatedAt())
                .build();
    }

    @Transactional
    public ComplexityReportDto analyzeSubmission(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        ComplexityReportDto report = complexityEngine.analyze(submission.getCode(), submission.getLanguage());
        persistComplexityReport(submission, report);
        return report;
    }

    public List<GrowthDataPointDto> getGrowthData(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        return computeGrowthData(submission.getCode(), submission.getLanguage());
    }

    public SubmissionResponseDto getById(UUID id, String userId) {
        Submission sub = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));
        return SubmissionResponseDto.builder()
                .id(sub.getId()).language(sub.getLanguage())
                .status(sub.getStatus().name()).createdAt(sub.getCreatedAt())
                .build();
    }

    public Page<SubmissionResponseDto> getMySubmissions(String userId, Pageable pageable) {
        return submissionRepository
                .findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId), pageable)
                .map(s -> SubmissionResponseDto.builder()
                        .id(s.getId()).language(s.getLanguage())
                        .status(s.getStatus().name()).createdAt(s.getCreatedAt())
                        .build());
    }

    // ── Helpers ───────────────────────────────────────

    private void persistExecutionResult(Submission submission, ExecutionResultDto dto) {
        try {
            entityManager.createNativeQuery(
                "INSERT INTO execution_results " +
                "(id, submission_id, stdout, stderr, exit_code, execution_time, memory_used) " +
                "VALUES (uuid_generate_v4(), :sid, :stdout, :stderr, :exitCode, :execTime, :mem)")
                    .setParameter("sid", submission.getId())
                    .setParameter("stdout", dto.getStdout())
                    .setParameter("stderr", dto.getStderr())
                    .setParameter("exitCode", dto.getExitCode())
                    .setParameter("execTime", dto.getExecutionTime())
                    .setParameter("mem", dto.getMemoryUsed())
                    .executeUpdate();
        } catch (Exception e) {
            log.warn("Could not persist execution result: {}", e.getMessage());
        }
    }

    private void persistComplexityReport(Submission submission, ComplexityReportDto dto) {
    try {
        // Check if a report already exists for this submission
        Long count = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM complexity_reports WHERE submission_id = :sid")
                .setParameter("sid", submission.getId())
                .getSingleResult();

        if (count > 0) return; // already analyzed, skip

        entityManager.createNativeQuery(
            "INSERT INTO complexity_reports " +
            "(id, submission_id, time_complexity, space_complexity, pattern, " +
            " confidence, explanation, nesting_depth, loop_count, recursion_detected) " +
            "VALUES (uuid_generate_v4(), :sid, :tc, :sc, :pattern, " +
            "        :conf, :expl, :nd, :lc, :rec)")
                .setParameter("sid", submission.getId())
                .setParameter("tc",   dto.getTimeComplexity())
                .setParameter("sc",   dto.getSpaceComplexity())
                .setParameter("pattern", dto.getPattern())
                .setParameter("conf", dto.getConfidence())
                .setParameter("expl", dto.getExplanation())
                .setParameter("nd",   dto.getNestingDepth())
                .setParameter("lc",   dto.getLoopCount())
                .setParameter("rec",  dto.isRecursionDetected())
                .executeUpdate();
    } catch (Exception e) {
        log.warn("Could not persist complexity report: {}", e.getMessage());
    }
}

    private List<GrowthDataPointDto> computeGrowthData(String code, String language) {
        ComplexityReportDto report = complexityEngine.analyze(code, language);
        String complexity = report.getTimeComplexity();
        int[] sizes = {10, 50, 100, 500, 1000, 5000, 10000};
        List<GrowthDataPointDto> data = new ArrayList<>();
        for (int n : sizes) {
            long ops  = estimateOps(complexity, n);
            double ms = estimateMs(complexity, n);
            data.add(GrowthDataPointDto.builder()
                    .inputSize(n).operations(ops)
                    .executionTimeMs(BigDecimal.valueOf(ms)).build());
        }
        return data;
    }

    private long estimateOps(String c, int n) {
        return switch (c) {
            case "O(1)"       -> 1L;
            case "O(log n)"   -> (long)(Math.log(n)/Math.log(2));
            case "O(n)"       -> n;
            case "O(n log n)" -> (long)(n * Math.log(n)/Math.log(2));
            case "O(n²)"      -> (long)n*n;
            case "O(n³)"      -> (long)n*n*n;
            case "O(2^n)"     -> n<=20 ? (1L<<n) : Long.MAX_VALUE/1000;
            default           -> n;
        };
    }

    private double estimateMs(String c, int n) {
        long ops = estimateOps(c, n);
        double safeOps = Math.min(ops, 1_000_000_000L);
        return (safeOps * 1.5) / 1_000_000.0;
    }
}
