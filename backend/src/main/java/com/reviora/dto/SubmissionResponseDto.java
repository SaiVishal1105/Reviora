package com.reviora.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionResponseDto {
    private UUID id;
    private String language;
    private String status;
    private ExecutionResultDto executionResult;
    private ComplexityReportDto complexityReport;
    private OffsetDateTime createdAt;
}
