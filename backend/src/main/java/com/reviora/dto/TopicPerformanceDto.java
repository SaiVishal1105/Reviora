package com.reviora.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TopicPerformanceDto {
    private String topic;
    private int solved;
    private int total;
    private BigDecimal score;
}
