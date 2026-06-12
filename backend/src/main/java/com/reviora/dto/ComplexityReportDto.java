package com.reviora.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplexityReportDto {
    private String timeComplexity;
    private String spaceComplexity;
    private String pattern;
    private BigDecimal confidence;
    private String explanation;
    private int nestingDepth;
    private int loopCount;
    private boolean recursionDetected;
}
