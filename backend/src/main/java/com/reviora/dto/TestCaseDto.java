package com.reviora.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TestCaseDto {
    private String input;
    private String expectedOutput;
    private String description;
    private String category;
}
