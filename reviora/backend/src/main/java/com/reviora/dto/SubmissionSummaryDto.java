package com.reviora.dto;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SubmissionSummaryDto {
    private UUID id;
    private String problem;
    private String complexity;
    private String status;
    private String language;
    private OffsetDateTime createdAt;
}
