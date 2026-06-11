package com.reviora.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviora.dto.AIReviewDto;
import com.reviora.dto.TestCaseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.api-key:}")
    private String openRouterApiKey;

    @Value("${openrouter.api-url:https://openrouter.ai/api/v1}")
    private String openRouterUrl;

    @Value("${openrouter.model:anthropic/claude-3-haiku}")
    private String model;

    public AIReviewDto generateReview(String code, String language, String timeComplexity) {

        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            return generateFallbackReview(code, language, timeComplexity);
        }

        try {
            String prompt = buildReviewPrompt(code, language, timeComplexity);
            String response = callOpenRouter(prompt, 800);

            if (response == null || response.isBlank()) {
                throw new RuntimeException("Empty AI response");
            }

            return parseReviewResponse(response, timeComplexity);

        } catch (Exception e) {
            log.error("AI Review generation failed", e);
            return generateFallbackReview(code, language, timeComplexity);
        }
    }

    public List<TestCaseDto> generateTestCases(String code, String language) {

        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            return generateFallbackTestCases();
        }

        try {
            String prompt = buildTestCasePrompt(code, language);
            String response = callOpenRouter(prompt, 1000);

            if (response == null || response.isBlank()) {
                throw new RuntimeException("Empty AI response");
            }

            return parseTestCaseResponse(response);

        } catch (Exception e) {
            log.error("Test case generation failed", e);
            return generateFallbackTestCases();
        }
    }

    private String callOpenRouter(String prompt, int maxTokens) {

        try {
            WebClient client = webClientBuilder
                    .baseUrl(openRouterUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openRouterApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("HTTP-Referer", "https://reviora.com")
                    .defaultHeader("X-Title", "Reviora DSA Platform")
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are an expert DSA interviewer. Respond ONLY with valid JSON. No markdown, no explanation."
                            ),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            Map<String, Object> response = client.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            if (response == null) {
                throw new RuntimeException("Null response from OpenRouter");
            }

            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No AI choices returned");
            }

            Map<String, Object> choice = (Map<String, Object>) choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");

            return message != null ? (String) message.get("content") : null;

        } catch (Exception e) {
            log.error("OpenRouter API error", e);
            return null;
        }
    }

    // ================= PROMPTS =================

    private String buildReviewPrompt(String code, String language, String timeComplexity) {
        return String.format("""
You are a senior DSA interviewer.

Analyze the following %s code.

Detected complexity: %s

Return ONLY valid JSON:

{
  "currentApproach": "",
  "suggestedOptimization": "",
  "expectedComplexity": "",
  "interviewNotes": "",
  "alternativeApproaches": [],
  "codeQualityScore": 0
}

CODE:
%s
""", language, timeComplexity, truncateCode(code));
    }

    private String buildTestCasePrompt(String code, String language) {
        return String.format("""
Generate 6 test cases for this %s code.

Return ONLY JSON array:

[
  {
    "input": "",
    "expectedOutput": "",
    "description": "",
    "category": "edge|normal|stress|adversarial"
  }
]

Code:
%s
""", language, truncateCode(code));
    }

    // ================= PARSING =================

    private AIReviewDto parseReviewResponse(String response, String fallbackComplexity) {

        try {
            String cleaned = cleanJson(response);

            Map<String, Object> data =
                    objectMapper.readValue(cleaned, Map.class);

            return AIReviewDto.builder()
                    .currentApproach(str(data.get("currentApproach")))
                    .suggestedOptimization(str(data.get("suggestedOptimization")))
                    .expectedComplexity(str(data.get("expectedComplexity")))
                    .interviewNotes(str(data.get("interviewNotes")))
                    .alternativeApproaches(castStringList(data.get("alternativeApproaches")))
                    .codeQualityScore(parseBigDecimal(
                            data.get("codeQualityScore"),
                            BigDecimal.valueOf(6.0)
                    ))
                    .build();

        } catch (Exception e) {
            log.error("AI parsing failed. Raw response: {}", response);
            return generateFallbackReview("", "", fallbackComplexity);
        }
    }

    private List<TestCaseDto> parseTestCaseResponse(String response) {

        try {
            String cleaned = cleanJson(response);

            List<?> data = objectMapper.readValue(cleaned, List.class);

            return data.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> {
                        Map<String, Object> m = (Map<String, Object>) item;

                        return TestCaseDto.builder()
                                .input(str(m.get("input")))
                                .expectedOutput(str(m.get("expectedOutput")))
                                .description(str(m.get("description")))
                                .category(str(m.get("category")))
                                .build();
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Test case parsing failed. Raw response: {}", response);
            return generateFallbackTestCases();
        }
    }

    // ================= HELPERS =================

    private String cleanJson(String response) {
        return response
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private BigDecimal parseBigDecimal(Object obj, BigDecimal fallback) {
        try {
            if (obj instanceof Number n) {
                return BigDecimal.valueOf(n.doubleValue());
            }
            if (obj instanceof String s) {
                return new BigDecimal(s);
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object obj) {
        if (obj instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    private String truncateCode(String code) {
        if (code == null) return "";
        return code.length() > 1500
                ? code.substring(0, 1500) + "\n// truncated"
                : code;
    }

    // ================= FALLBACKS =================

    private AIReviewDto generateFallbackReview(String code, String language, String timeComplexity) {
        return AIReviewDto.builder()
                .currentApproach("Brute-force or basic implementation")
                .suggestedOptimization("Use optimized data structures like HashMap or Two Pointers")
                .expectedComplexity(timeComplexity)
                .interviewNotes("Explain time-space tradeoff clearly in interviews")
                .alternativeApproaches(List.of(
                        "HashMap optimization",
                        "Two-pointer technique",
                        "Sorting + binary search"
                ))
                .codeQualityScore(BigDecimal.valueOf(6.0))
                .build();
    }

    private List<TestCaseDto> generateFallbackTestCases() {
        return List.of(
                TestCaseDto.builder().input("[]").expectedOutput("[]").description("Empty input").category("edge").build(),
                TestCaseDto.builder().input("1").expectedOutput("1").description("Single element").category("edge").build(),
                TestCaseDto.builder().input("1 2 3").expectedOutput("3").description("Normal case").category("normal").build(),
                TestCaseDto.builder().input("5 5 5").expectedOutput("5").description("Duplicates").category("edge").build(),
                TestCaseDto.builder().input("5 4 3 2 1").expectedOutput("5").description("Descending order").category("normal").build(),
                TestCaseDto.builder().input("-1 -2 -3").expectedOutput("-1").description("Negative values").category("adversarial").build()
        );
    }
}