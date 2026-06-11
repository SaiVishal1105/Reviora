package com.reviora.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionResultDto {
    private String stdout;
    private String stderr;
    private int exitCode;
    private int executionTime;
    private Long memoryUsed;
}
