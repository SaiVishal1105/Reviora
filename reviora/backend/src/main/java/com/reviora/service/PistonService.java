package com.reviora.service;

import com.reviora.dto.ExecutionResultDto;
import com.reviora.exception.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PistonService {

    private final WebClient.Builder webClientBuilder;

    @Value("${piston.api-url:http://localhost:2000/api/v2}")
    private String pistonApiUrl;

    @Value("${piston.timeout:15000}")
    private long timeoutMs;

    private static final Map<String, String> LANGUAGE_VERSION_MAP = Map.of(
            "python", "3.12.0",
            "java", "15.0.2",
            "c", "10.2.0",
            "cpp", "10.2.0",
            "c++", "10.2.0",
            "javascript", "20.11.1",
            "typescript", "5.0.3",
            "go", "1.16.2"
    );

    public ExecutionResultDto execute(String language, String code, String stdin) {

        String normalizedLang = normalizeLang(language);

        String version = LANGUAGE_VERSION_MAP.get(normalizedLang);

        if (version == null) {
            throw new ExecutionException("Unsupported language: " + normalizedLang);
        }

        WebClient client = webClientBuilder
                .baseUrl(pistonApiUrl)
                .build();

        // ✅ MINIMAL VALID PISTON REQUEST (IMPORTANT FIX)
        Map<String, Object> requestBody = Map.of(
                "language", normalizedLang,
                "version", version,
                "files", List.of(
                        Map.of(
                                "content", code
                        )
                ),
                "stdin", stdin != null ? stdin : ""
        );

        long startTime = System.currentTimeMillis();

        try {

            Map<String, Object> response = client.post()
                    .uri("/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            long execTime = System.currentTimeMillis() - startTime;

            if (response == null) {
                throw new ExecutionException("Empty response from Piston");
            }

            Map<String, Object> run = (Map<String, Object>) response.get("run");

            if (run == null) {

                Map<String, Object> compile = (Map<String, Object>) response.get("compile");

                String stderr = compile != null
                        ? (String) compile.getOrDefault("stderr", "Compilation failed")
                        : "Compilation failed";

                return ExecutionResultDto.builder()
                        .stdout("")
                        .stderr(stderr)
                        .exitCode(1)
                        .executionTime((int) execTime)
                        .build();
            }

            String stdout = (String) run.getOrDefault("stdout", "");
            String stderr = (String) run.getOrDefault("stderr", "");

            Object codeObj = run.get("code");

            int exitCode = codeObj instanceof Number
                    ? ((Number) codeObj).intValue()
                    : 0;

            return ExecutionResultDto.builder()
                    .stdout(stdout)
                    .stderr(stderr)
                    .exitCode(exitCode)
                    .executionTime((int) execTime)
                    .build();

        } catch (Exception e) {

            log.error("Piston execution failed", e);

            throw new ExecutionException(
                    "Execution failed: " + e.getMessage()
            );
        }
    }

    private String normalizeLang(String language) {

        if (language == null) return "cpp";

        return switch (language.toLowerCase()) {

            case "c++" -> "c++";   // ✅ FIXED (was wrong before)
            case "cpp" -> "c++";   // important alignment with runtime

            case "py", "python3" -> "python";

            case "js" -> "javascript";

            case "ts" -> "typescript";

            default -> language.toLowerCase();
        };
    }
}