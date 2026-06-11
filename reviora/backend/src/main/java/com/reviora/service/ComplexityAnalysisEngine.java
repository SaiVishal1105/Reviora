package com.reviora.service;

import com.reviora.dto.ComplexityReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Reviora Static Complexity Analysis Engine
 *
 * Performs AST-level complexity detection on submitted code WITHOUT AI.
 * Supports: C++, Java, Python, JavaScript, TypeScript, Go, Rust, C
 *
 * Algorithm:
 *  1. Count loop nesting depth
 *  2. Detect recursion patterns
 *  3. Detect special data-structure usage
 *  4. Map detected patterns to Big-O complexity
 */
@Slf4j
@Service
public class ComplexityAnalysisEngine {

    // ─── Pattern matchers ─────────────────────────────

    // For loops: for(...), while(...), do {
    private static final Pattern FOR_LOOP      = Pattern.compile("\\bfor\\s*\\(");
    private static final Pattern WHILE_LOOP    = Pattern.compile("\\bwhile\\s*\\(");
    private static final Pattern DO_WHILE      = Pattern.compile("\\bdo\\s*\\{");
    private static final Pattern FOREACH_LOOP  = Pattern.compile("\\bfor\\s+\\w+\\s+in\\b|foreach\\s*\\(|for\\s*\\(.*:\\s*");

    // Recursion: function calling itself
    private static final Pattern RECURSION_CPP = Pattern.compile("\\b(\\w+)\\s*\\([^)]*\\)\\s*\\{[^}]*\\1\\s*\\(");
    private static final Pattern RECURSION_PY  = Pattern.compile("def\\s+(\\w+).*?\\n.*?\\1\\s*\\(");
    private static final Pattern GENERIC_RECURSION = Pattern.compile(
        "(?:return|=)\\s+(?:self\\.)?\\w+\\s*\\([^;{]*\\)");

    // Data structure lookups
    private static final Pattern HASHMAP       = Pattern.compile("\\b(?:HashMap|unordered_map|dict|Map|Object\\.keys)\\b");
    private static final Pattern BINARY_SEARCH = Pattern.compile("\\b(?:binarySearch|binary_search|bisect)\\b");
    private static final Pattern SORTING       = Pattern.compile("\\b(?:sort|Arrays\\.sort|sorted|qsort|Collections\\.sort)\\b");
    private static final Pattern MERGE_SORT    = Pattern.compile("\\b(?:mergeSort|merge_sort)\\b");
    private static final Pattern DYNAMIC_PROG  = Pattern.compile("\\b(?:dp\\[|memo\\[|cache\\[|lru|memoize)\\b");
    private static final Pattern GRAPH_BFS     = Pattern.compile("\\b(?:BFS|bfs|queue\\.push|deque|Queue)\\b");
    private static final Pattern GRAPH_DFS     = Pattern.compile("\\b(?:DFS|dfs|stack\\.push|Stack)\\b");
    private static final Pattern DIVIDE_CONQ   = Pattern.compile("\\b(?:mid\\s*=|left.*right|divide|conquer)\\b");

    // Opening braces for nesting depth calculation
    private static final Pattern OPEN_BRACE    = Pattern.compile("\\{");
    private static final Pattern CLOSE_BRACE   = Pattern.compile("\\}");

    public ComplexityReportDto analyze(String code, String language) {
        if (code == null || code.isBlank()) {
            return buildReport("O(1)", "O(1)", "Empty", 0, 0, false, "No code to analyze.", 1.0);
        }

        // Clean code — remove comments and string literals to avoid false positives
        String cleaned = removeComments(code, language);

        int nestingDepth   = calculateNestingDepth(cleaned);
        int loopCount      = countLoops(cleaned);
        boolean hasRecursion = detectRecursion(cleaned);
        boolean hasHashMap   = HASHMAP.matcher(cleaned).find();
        boolean hasBinarySearch = BINARY_SEARCH.matcher(cleaned).find();
        boolean hasSorting   = SORTING.matcher(cleaned).find();
        boolean hasMergeSort = MERGE_SORT.matcher(cleaned).find();
        boolean hasDP        = DYNAMIC_PROG.matcher(cleaned).find();
        boolean hasBFS_DFS   = GRAPH_BFS.matcher(cleaned).find() || GRAPH_DFS.matcher(cleaned).find();
        boolean hasDivide    = DIVIDE_CONQ.matcher(cleaned).find();

        log.debug("Analysis — nesting:{} loops:{} recursion:{} hashmap:{} sort:{}",
                nestingDepth, loopCount, hasRecursion, hasHashMap, hasSorting);

        return classifyComplexity(
                nestingDepth, loopCount, hasRecursion, hasHashMap,
                hasBinarySearch, hasSorting, hasMergeSort, hasDP, hasBFS_DFS, hasDivide,
                cleaned
        );
    }

    private ComplexityReportDto classifyComplexity(
        int nestingDepth,
        int loopCount,
        boolean hasRecursion,
        boolean hasHashMap,
        boolean hasBinarySearch,
        boolean hasSorting,
        boolean hasMergeSort,
        boolean hasDP,
        boolean hasBFS_DFS,
        boolean hasDivide,
        String code) {

    String lowerCode = code.toLowerCase();

    // =====================================================
    // BACKTRACKING (N-Queens, Permutations, Subsets, etc.)
    // =====================================================
    if (hasRecursion &&
            (lowerCode.contains("nqueen")
                    || lowerCode.contains("backtrack")
                    || lowerCode.contains("permut")
                    || lowerCode.contains("subset")
                    || lowerCode.contains("combination"))) {

        return buildReport(
                "O(N!)",
                "O(N)",
                "Backtracking",
                nestingDepth,
                loopCount,
                true,
                "Recursive backtracking detected. Search tree grows factorially.",
                0.92
        );
    }

    // =====================================================
    // EXPONENTIAL RECURSION
    // =====================================================
    if (hasRecursion &&
            (lowerCode.contains("fib")
                    || lowerCode.contains("fibonacci")
                    || lowerCode.contains("knapsack"))) {

        return buildReport(
                "O(2^n)",
                "O(n)",
                "Exponential Recursion",
                nestingDepth,
                loopCount,
                true,
                "Recursive branching detected. Complexity grows exponentially.",
                0.88
        );
    }

    // =====================================================
    // DYNAMIC PROGRAMMING
    // =====================================================
    if (hasDP) {

        return buildReport(
                "O(n)",
                "O(n)",
                "Dynamic Programming",
                nestingDepth,
                loopCount,
                hasRecursion,
                "DP or memoization detected. Repeated states are cached.",
                0.94
        );
    }

    // =====================================================
    // GRAPH TRAVERSAL
    // =====================================================
    if (hasBFS_DFS) {

        return buildReport(
                "O(V+E)",
                "O(V)",
                "Graph Traversal",
                nestingDepth,
                loopCount,
                hasRecursion,
                "BFS/DFS traversal detected.",
                0.95
        );
    }

    // =====================================================
    // MERGE SORT
    // =====================================================
    if (hasMergeSort) {

        return buildReport(
                "O(n log n)",
                "O(n)",
                "Merge Sort",
                nestingDepth,
                loopCount,
                hasRecursion,
                "Merge sort detected.",
                0.95
        );
    }

    // =====================================================
    // SORTING
    // =====================================================
    if (hasSorting) {

        return buildReport(
                "O(n log n)",
                "O(1)",
                "Sorting",
                nestingDepth,
                loopCount,
                hasRecursion,
                "Sorting operation detected.",
                0.90
        );
    }

    // =====================================================
    // DIVIDE & CONQUER
    // =====================================================
    if (hasDivide && hasRecursion) {

        return buildReport(
                "O(n log n)",
                "O(log n)",
                "Divide & Conquer",
                nestingDepth,
                loopCount,
                true,
                "Divide and conquer recursion detected.",
                0.90
        );
    }

    // =====================================================
    // BINARY SEARCH
    // =====================================================
    if (hasBinarySearch ||
            (hasDivide && !hasRecursion && loopCount <= 1)) {

        return buildReport(
                "O(log n)",
                "O(1)",
                "Binary Search",
                nestingDepth,
                loopCount,
                hasRecursion,
                "Input size halves at each step.",
                0.95
        );
    }

    // =====================================================
    // HASHMAP OPTIMIZATION
    // =====================================================
    if (hasHashMap && nestingDepth <= 1 && loopCount >= 1) {

        return buildReport(
                "O(n)",
                "O(n)",
                "HashMap Optimization",
                nestingDepth,
                loopCount,
                hasRecursion,
                "Single-pass HashMap lookup detected. Average lookup cost O(1).",
                0.97
        );
    }

    // =====================================================
    // TRIPLE NESTED LOOPS
    // =====================================================
    if (nestingDepth >= 3) {

        return buildReport(
                "O(n³)",
                "O(1)",
                "Triple Nested Iteration",
                nestingDepth,
                loopCount,
                hasRecursion,
                "Three nested loops detected.",
                0.95
        );
    }

    // =====================================================
    // DOUBLE NESTED LOOPS
    // =====================================================
    if (nestingDepth == 2) {

        return buildReport(
                "O(n²)",
                "O(1)",
                "Double Nested Iteration",
                nestingDepth,
                loopCount,
                hasRecursion,
                "Two nested loops detected.",
                0.95
        );
    }

    // =====================================================
    // SINGLE LOOP
    // =====================================================
    if (loopCount >= 1) {

        String spaceComplexity = hasHashMap ? "O(n)" : "O(1)";

        return buildReport(
                "O(n)",
                spaceComplexity,
                "Linear Scan",
                nestingDepth,
                loopCount,
                hasRecursion,
                buildLinearExplanation(hasHashMap, false, false),
                0.93
        );
    }

    // =====================================================
    // SIMPLE RECURSION
    // =====================================================
    if (hasRecursion) {

        return buildReport(
                "O(n)",
                "O(n)",
                "Recursion",
                nestingDepth,
                loopCount,
                true,
                "Recursive solution detected.",
                0.82
        );
    }

    // =====================================================
    // CONSTANT TIME
    // =====================================================
    return buildReport(
            "O(1)",
            "O(1)",
            "Constant Time",
            nestingDepth,
            loopCount,
            false,
            "No loops or recursion detected.",
            0.98
    );
}

    private String buildLinearExplanation(boolean hashMap, boolean bfsDfs, boolean dp) {
        if (bfsDfs) return "Graph traversal detected. BFS/DFS visits each node and edge once — O(V+E), simplified as O(n).";
        if (dp) return "Dynamic programming detected. Memoization reduces repeated computation — O(n) with O(n) space.";
        if (hashMap) return "Single loop with O(1) HashMap lookups detected. Overall O(n) time, O(n) auxiliary space.";
        return "Single linear iteration detected. Each element processed exactly once.";
    }

    private int calculateNestingDepth(String code) {
    int maxLoopDepth  = 0;
    int currentDepth  = 0;  // brace depth (tracks all {})
    int loopDepth     = 0;  // how many loop-opening braces are on the stack

    // Track at which brace-depth each loop opened so we can pop it on close
    int[] loopOpenedAtDepth = new int[256];
    int loopStackTop = 0;

    for (char ch : code.toCharArray()) {
        if (ch == '{') {
            currentDepth++;
        } else if (ch == '}') {
            // If a loop opened at this depth, pop it
            if (loopStackTop > 0 && loopOpenedAtDepth[loopStackTop - 1] == currentDepth) {
                loopStackTop--;
                loopDepth--;
            }
            currentDepth = Math.max(0, currentDepth - 1);
        }
    }

    // Simpler reliable version: just count max consecutive loop keywords
    // by scanning line by line and tracking indent/brace depth per loop
    String[] lines = code.split("\n");
    int depth = 0;
    int max   = 0;
    for (String line : lines) {
        String trimmed = line.trim();
        boolean isLoop = FOR_LOOP.matcher(trimmed).find()
                || WHILE_LOOP.matcher(trimmed).find()
                || DO_WHILE.matcher(trimmed).find()
                || FOREACH_LOOP.matcher(trimmed).find();

        long opens  = trimmed.chars().filter(c -> c == '{').count();
        long closes = trimmed.chars().filter(c -> c == '}').count();

        if (isLoop) {
            depth++;
            max = Math.max(max, depth);
        }
        // When braces close more than they open, we've exited loop bodies
        depth = Math.max(0, depth - (int)(closes - opens));
    }
    return max; // 0 means no loops — do NOT force min of 1
}

    private int countLoops(String code) {
        int count = 0;
        count += countMatches(code, FOR_LOOP);
        count += countMatches(code, WHILE_LOOP);
        count += countMatches(code, DO_WHILE);
        return count;
    }

    private boolean detectRecursion(String code) {
        // Simple heuristic: find function declarations, then check if name appears in body
        Pattern funcDecl = Pattern.compile(
            "(?:def\\s+(\\w+)|(?:public|private|protected|static)?\\s+\\w+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{|(\\w+)\\s*:=\\s*func)");
        var matcher = funcDecl.matcher(code);
        while (matcher.find()) {
            String funcName = matcher.group(1) != null ? matcher.group(1)
                    : matcher.group(2) != null ? matcher.group(2)
                    : matcher.group(3);
            if (funcName != null && !funcName.isBlank()) {
                // Check if function name appears again in code (after its declaration)
                int start = matcher.end();
                if (start < code.length()) {
                    String rest = code.substring(start);
                    if (rest.contains(funcName + "(")) return true;
                }
            }
        }
        return GENERIC_RECURSION.matcher(code).find();
    }

    private String removeComments(String code, String language) {
        if (code == null) return "";
        // Remove single-line comments
        String result = code.replaceAll("//.*", "");
        // Remove multi-line comments
        result = result.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        if ("python".equalsIgnoreCase(language) || "python3".equalsIgnoreCase(language)) {
            result = result.replaceAll("#.*", "");
            result = result.replaceAll("\"\"\"[\\s\\S]*?\"\"\"", "");
        }
        // Remove string literals (simplified)
        result = result.replaceAll("\"[^\"]*\"", "\"\"");
        result = result.replaceAll("'[^']*'", "''");
        return result;
    }

    private boolean containsPattern(String code, String pattern) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(code).find();
    }

    private int countMatches(String code, Pattern pattern) {
        int count = 0;
        var matcher = pattern.matcher(code);
        while (matcher.find()) count++;
        return count;
    }

    private ComplexityReportDto buildReport(
            String time, String space, String pattern,
            int nestingDepth, int loopCount, boolean recursion,
            String explanation, double confidence) {
        return ComplexityReportDto.builder()
                .timeComplexity(time)
                .spaceComplexity(space)
                .pattern(pattern)
                .nestingDepth(nestingDepth)
                .loopCount(loopCount)
                .recursionDetected(recursion)
                .explanation(explanation)
                .confidence(BigDecimal.valueOf(confidence))
                .build();
    }
}
