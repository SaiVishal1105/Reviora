package com.reviora.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIReviewDto {
    private String currentApproach;
    private String suggestedOptimization;
    private String expectedComplexity;
    private String interviewNotes;
    private List<String> alternativeApproaches;
    private BigDecimal codeQualityScore;
}
