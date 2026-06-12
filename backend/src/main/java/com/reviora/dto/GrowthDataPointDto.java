package com.reviora.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GrowthDataPointDto {
    private int inputSize;
    private long operations;
    private BigDecimal executionTimeMs;
}
