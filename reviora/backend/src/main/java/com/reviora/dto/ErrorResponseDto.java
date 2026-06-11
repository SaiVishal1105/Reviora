package com.reviora.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ErrorResponseDto {
    private String message;
    private int status;
    private String timestamp;
    private String path;
}
