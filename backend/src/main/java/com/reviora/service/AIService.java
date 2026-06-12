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
            String prompt    = buildReviewPrompt(code, language, timeComplexity);
            String response  = callOpenRouter(prompt, 800);
            if (response == null || response.isBlank()) throw new RuntimeException("Empty AI response");
            return parseReviewResponse(response, timeComplexity);
        } catch (Exception e) {
            log.error("AI Review generation failed", e);
            return generateFallbackReview(code, language, timeComplexity);
        }
    }

    public List<TestCaseDto> generateTestCases(String code, String language) {
        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            return generateFallbackTestCases(code, language);
        }
        try {
            String prompt   = buildTestCasePrompt(code, language);
            String response = callOpenRouter(prompt, 1000);
            if (response == null || response.isBlank()) throw new RuntimeException("Empty AI response");
            return parseTestCaseResponse(response);
        } catch (Exception e) {
            log.error("Test case generation failed", e);
            return generateFallbackTestCases(code, language);
        }
    }

    // ─── OpenRouter call ──────────────────────────────────────────────────────

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
                            Map.of("role", "system",
                                   "content", "You are an expert DSA interviewer. Respond ONLY with valid JSON. No markdown, no explanation."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            Map<?, ?> response = client.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            if (response == null) throw new RuntimeException("Null response from OpenRouter");

            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) throw new RuntimeException("No choices returned");

            Map<?, ?> choice  = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            return message != null ? (String) message.get("content") : null;

        } catch (Exception e) {
            log.error("OpenRouter API error: {}", e.getMessage());
            return null;
        }
    }

    // ─── Prompts ──────────────────────────────────────────────────────────────

    private String buildReviewPrompt(String code, String language, String timeComplexity) {
        return String.format("""
You are a senior DSA interviewer.
Analyze the following %s code. Detected complexity: %s
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
[{"input":"","expectedOutput":"","description":"","category":"edge|normal|stress|adversarial"}]
Code:
%s
""", language, truncateCode(code));
    }

    // ─── Parsing ──────────────────────────────────────────────────────────────

    private AIReviewDto parseReviewResponse(String response, String fallbackComplexity) {
        try {
            String cleaned = cleanJson(response);
            Map<?, ?> data = objectMapper.readValue(cleaned, Map.class);
            return AIReviewDto.builder()
                    .currentApproach(str(data.get("currentApproach")))
                    .suggestedOptimization(str(data.get("suggestedOptimization")))
                    .expectedComplexity(str(data.get("expectedComplexity")))
                    .interviewNotes(str(data.get("interviewNotes")))
                    .alternativeApproaches(castStringList(data.get("alternativeApproaches")))
                    .codeQualityScore(parseBigDecimal(data.get("codeQualityScore"), BigDecimal.valueOf(6.0)))
                    .build();
        } catch (Exception e) {
            log.error("AI parsing failed. Raw: {}", response);
            return generateFallbackReview("", "", fallbackComplexity);
        }
    }

    private List<TestCaseDto> parseTestCaseResponse(String response) {
        try {
            String cleaned = cleanJson(response);
            List<?> data   = objectMapper.readValue(cleaned, List.class);
            return data.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> {
                        Map<?, ?> m = (Map<?, ?>) item;
                        return TestCaseDto.builder()
                                .input(str(m.get("input")))
                                .expectedOutput(str(m.get("expectedOutput")))
                                .description(str(m.get("description")))
                                .category(str(m.get("category")))
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Test case parsing failed. Raw: {}", response);
            return generateFallbackTestCases("", "");
        }
    }

    // ─── Smart fallback review — matches actual complexity ────────────────────

    private AIReviewDto generateFallbackReview(String code, String language, String timeComplexity) {
        String lowerCode = code == null ? "" : code.toLowerCase();

        return switch (timeComplexity) {

            case "O(1)" -> AIReviewDto.builder()
                    .currentApproach("Constant-time operation — result computed directly without iteration.")
                    .suggestedOptimization("Already optimal. No further time complexity improvement possible.")
                    .expectedComplexity("O(1)")
                    .interviewNotes("Explain why this is O(1): the number of operations does not grow with input size. Mention that space may still vary.")
                    .alternativeApproaches(List.of(
                            "No meaningful alternative — O(1) is the theoretical best.",
                            "Ensure space complexity is also O(1) if the problem requires it."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(9.0))
                    .build();

            case "O(log n)" -> AIReviewDto.builder()
                    .currentApproach("Logarithmic search — input is halved at each step (binary search or similar).")
                    .suggestedOptimization("Already optimal for sorted-input search problems. Ensure the array is sorted before applying.")
                    .expectedComplexity("O(log n)")
                    .interviewNotes("Interviewers expect you to recognize that binary search requires a sorted structure. Always mention the sorted pre-condition. Edge cases: empty array, single element, target not present.")
                    .alternativeApproaches(List.of(
                            "If input is unsorted: sort first O(n log n) then binary search O(log n).",
                            "Use a TreeSet/TreeMap for O(log n) insert+search if data is dynamic."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(8.5))
                    .build();

            case "O(n)" -> {
                boolean hasHashMap = lowerCode.contains("hashmap") || lowerCode.contains("map")
                        || lowerCode.contains("dict") || lowerCode.contains("seen")
                        || lowerCode.contains(".put(") || lowerCode.contains(".get(");
                boolean hasTwoPointer = lowerCode.contains("left") && lowerCode.contains("right")
                        || lowerCode.contains("lo") && lowerCode.contains("hi");

                String approach = hasHashMap
                        ? "Single-pass HashMap approach — each element stored for O(1) lookup of its complement."
                        : hasTwoPointer
                        ? "Two-pointer technique — left and right pointers converging from both ends."
                        : "Single linear scan — each element processed exactly once.";

                String optimization = hasHashMap
                        ? "Already optimal for unsorted input. If input is sorted, Two Pointers gives the same O(n) time with O(1) space instead of O(n)."
                        : "Consider HashMap if you need O(1) lookups. For sorted input, Two Pointers achieves O(1) space.";

                String score = hasHashMap ? "9.0" : "7.5";

                yield AIReviewDto.builder()
                        .currentApproach(approach)
                        .suggestedOptimization(optimization)
                        .expectedComplexity("O(n)")
                        .interviewNotes("This is the expected optimal solution for most linear problems. " +
                                "Highlight the time-space tradeoff: HashMap costs O(n) extra space. " +
                                "Mention edge cases: empty input, duplicates, negative numbers.")
                        .alternativeApproaches(List.of(
                                "Two Pointers (sorted input) — O(n) time, O(1) space.",
                                "Sorting + binary search — O(n log n) time, O(1) space, worse overall.",
                                "Brute force O(n²) — only acceptable as initial explanation."
                        ))
                        .codeQualityScore(new BigDecimal(score))
                        .build();
            }

            case "O(n log n)" -> AIReviewDto.builder()
                    .currentApproach("Sorting-based or divide-and-conquer approach — O(n log n) is optimal for comparison-based sorting.")
                    .suggestedOptimization("O(n log n) is optimal for comparison-based sorting. If values are bounded integers, Counting Sort or Radix Sort can achieve O(n).")
                    .expectedComplexity("O(n log n)")
                    .interviewNotes("FAANG interviewers consider O(n log n) acceptable for sorting problems. Be ready to explain why O(n) sort (counting/radix) only applies to bounded integer inputs. Mention stability of sort if order of equal elements matters.")
                    .alternativeApproaches(List.of(
                            "Counting Sort — O(n + k) for bounded integers, O(1) extra space.",
                            "Radix Sort — O(nk) for fixed-width integers.",
                            "Heap Sort — O(n log n) in-place, O(1) space."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(8.0))
                    .build();

            case "O(n²)" -> AIReviewDto.builder()
                    .currentApproach("Brute-force nested iteration — comparing every pair of elements with two nested loops.")
                    .suggestedOptimization("Replace inner loop with a HashMap for O(1) lookups → reduces overall complexity to O(n). " +
                            "If input is sorted, Two Pointers achieves O(n) with O(1) space.")
                    .expectedComplexity("O(n)")
                    .interviewNotes("O(n²) brute force is acceptable only as an initial explanation in FAANG interviews. " +
                            "You are expected to immediately follow up with the O(n) optimized solution. " +
                            "Always articulate the tradeoff: extra O(n) space for the HashMap buys you O(n) time savings.")
                    .alternativeApproaches(List.of(
                            "HashMap single-pass — O(n) time, O(n) space. Best for unsorted input.",
                            "Two Pointers (sorted input) — O(n) time, O(1) space.",
                            "Sorting + binary search — O(n log n) time, O(1) space."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(4.5))
                    .build();

            case "O(n³)" -> AIReviewDto.builder()
                    .currentApproach("Triple nested loops — cubic time complexity, extremely slow for large inputs.")
                    .suggestedOptimization("Fix the outermost loop and apply Two Pointers on the inner two loops to reduce from O(n³) to O(n²). " +
                            "Use a HashMap to reduce further to O(n) for specific problems like 3Sum.")
                    .expectedComplexity("O(n²)")
                    .interviewNotes("O(n³) is almost never acceptable in interviews. For 3Sum-type problems, the expected solution is O(n²) with sorting + two pointers. " +
                            "Always start with the brute-force to show understanding, then immediately optimize.")
                    .alternativeApproaches(List.of(
                            "Sort + Two Pointers — O(n²) time, O(1) extra space.",
                            "HashMap approach — O(n²) average with O(n) space.",
                            "For 4Sum extend Two Pointers to O(n³)."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(3.0))
                    .build();

            case "O(V+E)" -> AIReviewDto.builder()
                    .currentApproach("Graph traversal (BFS/DFS) — visits every vertex and edge exactly once.")
                    .suggestedOptimization("O(V+E) is optimal for unweighted graph traversal. " +
                            "For weighted shortest path use Dijkstra O((V+E) log V). " +
                            "Use BFS for shortest path in unweighted graphs, DFS for cycle detection or topological sort.")
                    .expectedComplexity("O(V+E)")
                    .interviewNotes("Always clarify whether the graph is directed/undirected, weighted/unweighted. " +
                            "Mention the visited set to avoid infinite loops in cyclic graphs. " +
                            "BFS uses a queue (iterative), DFS uses a stack or recursion.")
                    .alternativeApproaches(List.of(
                            "Dijkstra — O((V+E) log V) for weighted shortest path.",
                            "Bellman-Ford — O(VE) handles negative weights.",
                            "A* — O(E log V) with heuristic for pathfinding."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(8.0))
                    .build();

            case "O(2^n)" -> AIReviewDto.builder()
                    .currentApproach("Exponential recursion — each call branches into two sub-calls, doubling work at every level.")
                    .suggestedOptimization("Add memoization (top-down DP) to cache overlapping subproblems → reduces to O(n) time, O(n) space. " +
                            "Or use bottom-up DP with a table for O(n) time and O(1) space (rolling array).")
                    .expectedComplexity("O(n)")
                    .interviewNotes("Naive recursive Fibonacci is the classic example of exponential blowup. " +
                            "Interviewers will always ask for the optimized version. " +
                            "Show all three: O(2^n) recursive → O(n) memo → O(n) DP → O(1) rolling variables.")
                    .alternativeApproaches(List.of(
                            "Top-down DP with @lru_cache / HashMap memo — O(n) time, O(n) space.",
                            "Bottom-up DP table — O(n) time, O(n) space.",
                            "Rolling variables (Fibonacci only) — O(n) time, O(1) space.",
                            "Matrix exponentiation — O(log n) for Fibonacci."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(3.5))
                    .build();

            case "O(N!)" -> AIReviewDto.builder()
                    .currentApproach("Backtracking — explores all permutations or configurations, pruning invalid branches early.")
                    .suggestedOptimization("Backtracking is often optimal for constraint-satisfaction problems like N-Queens or permutations. " +
                            "Improve pruning heuristics to reduce constant factor. " +
                            "For subset/combination problems, use bitmask DP if n ≤ 20.")
                    .expectedComplexity("O(N!)")
                    .interviewNotes("Interviewers expect you to explain the pruning strategy clearly — how early termination reduces the practical search space. " +
                            "Always mention: N-Queens checks column + diagonal constraints before recursing. " +
                            "For permutations, using a visited[] array avoids rebuilding the set each time.")
                    .alternativeApproaches(List.of(
                            "Bitmask DP — O(2^n × n) for n ≤ 20, much faster than O(n!).",
                            "Better pruning heuristics (forward checking, arc consistency) for CSPs.",
                            "Iterative generation using next_permutation for enumeration only."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(7.5))
                    .build();

            default -> AIReviewDto.builder()
                    .currentApproach("Algorithm with detected complexity: " + timeComplexity)
                    .suggestedOptimization("Review algorithm choice and data structures for potential optimization.")
                    .expectedComplexity(timeComplexity)
                    .interviewNotes("Clearly explain your approach, time and space complexity, and any edge cases.")
                    .alternativeApproaches(List.of(
                            "HashMap for O(1) lookups if repeated search is needed.",
                            "Two Pointers for sorted array problems.",
                            "Binary Search to reduce O(n) search to O(log n)."
                    ))
                    .codeQualityScore(BigDecimal.valueOf(6.0))
                    .build();
        };
    }

    // ─── Smart fallback test cases — language-aware ───────────────────────────

    private List<TestCaseDto> generateFallbackTestCases(String code, String language) {
        boolean isPython = "python".equalsIgnoreCase(language) || "python3".equalsIgnoreCase(language);
        boolean isJava   = "java".equalsIgnoreCase(language);

        return List.of(
                TestCaseDto.builder()
                        .input("0\n")
                        .expectedOutput("")
                        .description("Empty / zero-size input")
                        .category("edge").build(),
                TestCaseDto.builder()
                        .input("1\n42\n")
                        .expectedOutput("42")
                        .description("Single element")
                        .category("edge").build(),
                TestCaseDto.builder()
                        .input("5\n2 7 11 15 3\n9")
                        .expectedOutput("0 1")
                        .description("Normal case — two sum target exists")
                        .category("normal").build(),
                TestCaseDto.builder()
                        .input("5\n3 3 3 3 3\n6")
                        .expectedOutput("0 1")
                        .description("All duplicate elements")
                        .category("edge").build(),
                TestCaseDto.builder()
                        .input("5\n5 4 3 2 1\n9")
                        .expectedOutput("0 3")
                        .description("Descending order input")
                        .category("normal").build(),
                TestCaseDto.builder()
                        .input("4\n-3 -2 -1 0\n-5")
                        .expectedOutput("0 2")
                        .description("Negative values")
                        .category("adversarial").build()
        );
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
            if (obj instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
            if (obj instanceof String s) return new BigDecimal(s);
        } catch (Exception ignored) {}
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object obj) {
        if (obj instanceof List<?> list) return (List<String>) list;
        return List.of();
    }

    private String truncateCode(String code) {
        if (code == null) return "";
        return code.length() > 1500 ? code.substring(0, 1500) + "\n// truncated" : code;
    }
}