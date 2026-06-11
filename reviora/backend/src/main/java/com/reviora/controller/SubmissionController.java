package com.reviora.controller;

import com.reviora.dto.*;
import com.reviora.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submissions", description = "Code submission and execution")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @Operation(summary = "Submit code for execution + analysis")
    public ResponseEntity<SubmissionResponseDto> submit(
            @Valid @RequestBody SubmitRequest req,
            Authentication auth) {
        String userId = auth != null ? auth.getName() : null;
        SubmissionResponseDto result = submissionService.submit(
                userId, req.getCode(), req.getLanguage(), req.getStdin());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/guest")
    @Operation(summary = "Guest submission — no auth required")
    public ResponseEntity<SubmissionResponseDto> submitGuest(
            @Valid @RequestBody SubmitRequest req) {
        SubmissionResponseDto result = submissionService.submit(
                null, req.getCode(), req.getLanguage(), req.getStdin());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger static complexity analysis for a submission")
    public ResponseEntity<ComplexityReportDto> analyze(
            @PathVariable UUID id, Authentication auth) {
        return ResponseEntity.ok(submissionService.analyzeSubmission(id));
    }

    @GetMapping("/{id}/growth")
    @Operation(summary = "Get growth data points for chart")
    public ResponseEntity<List<GrowthDataPointDto>> getGrowthData(
            @PathVariable UUID id, Authentication auth) {
        return ResponseEntity.ok(submissionService.getGrowthData(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get submission by ID")
    public ResponseEntity<SubmissionResponseDto> getById(
            @PathVariable UUID id, Authentication auth) {
        String userId = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(submissionService.getById(id, userId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's submissions")
    public ResponseEntity<Page<SubmissionResponseDto>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(
                submissionService.getMySubmissions(auth.getName(), PageRequest.of(page, size)));
    }

    // ── Inner request DTO ──────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SubmitRequest {
        @NotBlank(message = "Code is required")
        private String code;

        @NotBlank(message = "Language is required")
        @Pattern(regexp = "^(cpp|c|java|python|python3|javascript|typescript|go|rust)$",
                 message = "Unsupported language")
        private String language;

        private String stdin;
    }
}
