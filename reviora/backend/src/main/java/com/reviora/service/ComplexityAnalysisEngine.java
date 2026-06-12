package com.reviora.service;

import com.reviora.dto.ComplexityReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ComplexityAnalysisEngine {

    // ─── Loop patterns ────────────────────────────────────────────────────────

    // C++/Java/JS: for(, while(, do {
    private static final Pattern FOR_LOOP_BRACE  = Pattern.compile("\\bfor\\s*\\(");
    private static final Pattern WHILE_LOOP      = Pattern.compile("\\bwhile\\s*\\(");
    private static final Pattern DO_WHILE        = Pattern.compile("\\bdo\\s*\\{");

    // Python/Go/Rust: for x in, for x, y in, foreach
    private static final Pattern FOR_LOOP_COLON  = Pattern.compile("\\bfor\\s+.+:");
    private static final Pattern FOREACH_LOOP    = Pattern.compile("\\bforeach\\s*\\(");

    // ─── Recursion ────────────────────────────────────────────────────────────
    private static final Pattern FUNC_DECL = Pattern.compile(
        "(?:def\\s+(\\w+)\\s*\\()" +                          // Python
        "|(?:(?:public|private|protected|static|void|int|long|boolean|String|List|" +
        "double|float|char|Object|var|fun)\\s+)+(\\w+)\\s*\\([^)]*\\)\\s*(?:throws\\s+\\w+\\s*)?\\{?" +  // Java/Kotlin
        "|(?:func(?:tion)?\\s+(\\w+)\\s*\\()" +              // JS/Go
        "|(?:fn\\s+(\\w+)\\s*\\()"                            // Rust
    );

    // ─── Data structures & algorithms ────────────────────────────────────────
    private static final Pattern HASHMAP = Pattern.compile(
        "\\b(?:HashMap|HashSet|LinkedHashMap|TreeMap|" +
        "unordered_map|unordered_set|" +
        "dict(?:\\s*=|\\s*\\()|defaultdict|Counter|" +
        "new\\s+Map|new\\s+Set|" +
        "\\.put\\(|\\.get\\(|map\\[|seen\\[|freq\\[)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BINARY_SEARCH = Pattern.compile(
        "\\b(?:binarySearch|binary_search|bisect_left|bisect_right|bisect|" +
        "mid\\s*=\\s*.*(?:left|lo|start|low).*(?:right|hi|end|high)|" +
        "mid\\s*=\\s*\\(|\\(lo\\s*\\+\\s*hi\\)|\\(left\\s*\\+\\s*right\\))\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SORTING = Pattern.compile(
        "\\b(?:Arrays\\.sort|Collections\\.sort|list\\.sort|sorted\\s*\\(|" +
        "\\.sort\\s*\\(|qsort|std::sort|sort\\s*\\()\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MERGE_SORT = Pattern.compile(
        "\\b(?:mergeSort|merge_sort|mergesort)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DYNAMIC_PROG = Pattern.compile(
        "\\b(?:dp\\s*=|dp\\s*\\[|memo\\s*=|memo\\s*\\[|cache\\s*=|cache\\s*\\[|" +
        "@lru_cache|@cache|functools\\.lru|memoize|tabulation|" +
        "dp\\.get|memo\\.get)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern GRAPH_BFS = Pattern.compile(
        "\\b(?:BFS|deque|ArrayDeque|LinkedList|Queue|queue\\.add|queue\\.poll|" +
        "queue\\.append|queue\\.popleft|visited|adj\\[|graph\\[|neighbors)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern GRAPH_DFS = Pattern.compile(
        "\\b(?:DFS|dfs\\s*\\(|stack\\.push|stack\\.pop|Stack|" +
        "visited|adj\\[|graph\\[|neighbors)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BACKTRACK = Pattern.compile(
        "\\b(?:backtrack|nqueen|n_queen|permut|permutation|" +
        "combination|subset|generateParenthesis|solveSudoku)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EXPONENTIAL_RECURSION = Pattern.compile(
        "\\b(?:fib|fibonacci|knapsack)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // ─── Public entry point ───────────────────────────────────────────────────

    public ComplexityReportDto analyze(String code, String language) {
        if (code == null || code.isBlank()) {
            return buildReport("O(1)", "O(1)", "Empty", 0, 0, false,
                    "No code to analyze.", 1.0);
        }

        String cleaned = removeComments(code, language);

        int     nestingDepth    = calculateNestingDepth(cleaned, language);
        int     loopCount       = countLoops(cleaned, language);
        boolean hasRecursion    = detectRecursion(cleaned);
        boolean hasHashMap      = HASHMAP.matcher(cleaned).find();
        boolean hasBinarySearch = BINARY_SEARCH.matcher(cleaned).find();
        boolean hasSorting      = SORTING.matcher(cleaned).find();
        boolean hasMergeSort    = MERGE_SORT.matcher(cleaned).find();
        boolean hasDP           = DYNAMIC_PROG.matcher(cleaned).find();
        boolean hasBFS          = GRAPH_BFS.matcher(cleaned).find();
        boolean hasDFS          = GRAPH_DFS.matcher(cleaned).find();
        boolean hasBFS_DFS      = hasBFS || hasDFS;
        boolean hasBacktrack    = BACKTRACK.matcher(cleaned).find();
        boolean hasExpRecursion = EXPONENTIAL_RECURSION.matcher(cleaned).find();

        log.debug("Analysis — lang:{} nesting:{} loops:{} recursion:{} hashmap:{} dp:{} sort:{}",
                language, nestingDepth, loopCount, hasRecursion,
                hasHashMap, hasDP, hasSorting);

        return classifyComplexity(
                nestingDepth, loopCount, hasRecursion, hasHashMap,
                hasBinarySearch, hasSorting, hasMergeSort, hasDP,
                hasBFS_DFS, hasBacktrack, hasExpRecursion
        );
    }

    // ─── Classification logic (ordered from most to least specific) ──────────

    private ComplexityReportDto classifyComplexity(
            int     nestingDepth,
            int     loopCount,
            boolean hasRecursion,
            boolean hasHashMap,
            boolean hasBinarySearch,
            boolean hasSorting,
            boolean hasMergeSort,
            boolean hasDP,
            boolean hasBFS_DFS,
            boolean hasBacktrack,
            boolean hasExpRecursion) {

        // ── 1. Backtracking: N-Queens, permutations, subsets ─────────────────
        if (hasBacktrack && hasRecursion) {
            return buildReport("O(N!)", "O(N)", "Backtracking",
                    nestingDepth, loopCount, true,
                    "Recursive backtracking detected. The search tree grows " +
                    "factorially — each state branches into N choices.", 0.92);
        }

        // ── 2. Exponential recursion: Fibonacci, Knapsack (no memo) ──────────
        if (hasExpRecursion && hasRecursion && !hasDP) {
            return buildReport("O(2^n)", "O(n)", "Exponential Recursion",
                    nestingDepth, loopCount, true,
                    "Unoptimized recursive branching detected. Each call spawns " +
                    "two sub-calls, doubling work at every level. " +
                    "Add memoization to reduce to O(n).", 0.88);
        }

        // ── 3. Dynamic Programming ────────────────────────────────────────────
        if (hasDP) {
            // DP with nested loops → O(n²) e.g. LCS, Edit Distance
            if (nestingDepth >= 2 && loopCount >= 2) {
                return buildReport("O(n²)", "O(n²)", "2D Dynamic Programming",
                        nestingDepth, loopCount, hasRecursion,
                        "2D DP table detected. Both dimensions iterated — O(n²) time " +
                        "and space for the memoization table.", 0.91);
            }
            return buildReport("O(n)", "O(n)", "Dynamic Programming",
                    nestingDepth, loopCount, hasRecursion,
                    "Memoization or DP table detected. Each subproblem computed " +
                    "once and cached — O(n) time and O(n) space.", 0.94);
        }

        // ── 4. Graph traversal: BFS / DFS ─────────────────────────────────────
        if (hasBFS_DFS) {
            return buildReport("O(V+E)", "O(V)", "Graph Traversal",
                    nestingDepth, loopCount, hasRecursion,
                    "BFS/DFS traversal detected. Every vertex (V) and edge (E) " +
                    "visited exactly once.", 0.95);
        }

        // ── 5. Merge sort ─────────────────────────────────────────────────────
        if (hasMergeSort) {
            return buildReport("O(n log n)", "O(n)", "Merge Sort",
                    nestingDepth, loopCount, hasRecursion,
                    "Merge sort pattern detected. Array halved recursively — " +
                    "O(log n) levels, O(n) work per level.", 0.95);
        }

        // ── 6. General sorting ────────────────────────────────────────────────
        if (hasSorting) {
            return buildReport("O(n log n)", "O(1)", "Comparison Sort",
                    nestingDepth, loopCount, hasRecursion,
                    "Built-in sort detected. Standard comparison sorts run in " +
                    "O(n log n) average-case time.", 0.90);
        }

        // ── 7. Triple nested loops ────────────────────────────────────────────
        if (nestingDepth >= 3 && loopCount >= 3) {
            return buildReport("O(n³)", "O(1)", "Triple Nested Iteration",
                    nestingDepth, loopCount, hasRecursion,
                    "Three nested loops detected. Each level multiplies " +
                    "complexity — look for a DP or greedy approach.", 0.95);
        }

        // ── 8. Double nested loops ────────────────────────────────────────────
        //    Check BEFORE HashMap so Bubble Sort isn't mis-classified
        if (nestingDepth >= 2 && loopCount >= 2) {
            return buildReport("O(n²)", "O(1)", "Nested Iteration",
                    nestingDepth, loopCount, hasRecursion,
                    "Two nested loops detected. Quadratic growth — consider a " +
                    "HashMap or two-pointer approach to reduce to O(n).", 0.95);
        }

        // ── 9. Binary search ──────────────────────────────────────────────────
        if (hasBinarySearch) {
            return buildReport("O(log n)", "O(1)", "Binary Search",
                    nestingDepth, loopCount, hasRecursion,
                    "Binary search pattern detected. Input size halved at each " +
                    "step — O(log n) comparisons.", 0.95);
        }

        // ── 10. HashMap single-pass ───────────────────────────────────────────
        if (hasHashMap && loopCount >= 1) {
            return buildReport("O(n)", "O(n)", "HashMap Optimization",
                    nestingDepth, loopCount, hasRecursion,
                    "Single-pass with O(1) HashMap lookup detected. One traversal " +
                    "over input with constant-time lookups — O(n) time, " +
                    "O(n) auxiliary space for the map.", 0.97);
        }

        // ── 11. Single loop ───────────────────────────────────────────────────
        if (loopCount >= 1) {
            String space = hasHashMap ? "O(n)" : "O(1)";
            return buildReport("O(n)", space, "Linear Scan",
                    nestingDepth, loopCount, hasRecursion,
                    "Single linear pass detected. Each element visited once.", 0.93);
        }

        // ── 12. Simple recursion (no loop, no special pattern) ────────────────
        if (hasRecursion) {
            return buildReport("O(n)", "O(n)", "Linear Recursion",
                    nestingDepth, loopCount, true,
                    "Linear recursive calls detected. Depth proportional to " +
                    "input size — O(n) time and stack space.", 0.82);
        }

        // ── 13. Constant time ─────────────────────────────────────────────────
        return buildReport("O(1)", "O(1)", "Constant Time",
                nestingDepth, loopCount, false,
                "No loops or recursion detected. Runtime is independent " +
                "of input size.", 0.98);
    }

    // ─── Nesting depth — works for braces AND Python indentation ─────────────

    private int calculateNestingDepth(String code, String language) {
        boolean isPython = "python".equalsIgnoreCase(language)
                        || "python3".equalsIgnoreCase(language);

        if (isPython) {
            return calculatePythonNestingDepth(code);
        }
        return calculateBraceNestingDepth(code);
    }

    /**
     * For brace-based languages (C++, Java, JS, Go, Rust, C).
     * Tracks which brace depth each loop opened at so we can
     * correctly decrement loopDepth when that brace closes.
     */
    private int calculateBraceNestingDepth(String code) {
        String[] lines   = code.split("\n");
        int braceDepth   = 0;   // total { depth
        int loopDepth    = 0;   // current nesting of loops
        int maxLoopDepth = 0;

        // Stack: at which braceDepth did each loop open?
        int[] loopOpenedAt = new int[512];
        int   stackTop     = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            boolean isLoop = FOR_LOOP_BRACE.matcher(trimmed).find()
                          || WHILE_LOOP.matcher(trimmed).find()
                          || DO_WHILE.matcher(trimmed).find()
                          || FOREACH_LOOP.matcher(trimmed).find();

            // Count { and } on this line
            int opens  = (int) trimmed.chars().filter(c -> c == '{').count();
            int closes = (int) trimmed.chars().filter(c -> c == '}').count();

            // If this line opens a loop, record the depth it opens at
            if (isLoop && opens > 0) {
                braceDepth += opens;
                loopDepth++;
                if (stackTop < loopOpenedAt.length) {
                    loopOpenedAt[stackTop++] = braceDepth;
                }
                maxLoopDepth = Math.max(maxLoopDepth, loopDepth);
                // Process remaining closes
                for (int i = 0; i < closes; i++) {
                    if (stackTop > 0 && loopOpenedAt[stackTop - 1] == braceDepth) {
                        stackTop--;
                        loopDepth--;
                    }
                    braceDepth = Math.max(0, braceDepth - 1);
                }
            } else {
                // Non-loop line — handle opens first, then closes
                braceDepth += opens;
                for (int i = 0; i < closes; i++) {
                    if (stackTop > 0 && loopOpenedAt[stackTop - 1] == braceDepth) {
                        stackTop--;
                        loopDepth = Math.max(0, loopDepth - 1);
                    }
                    braceDepth = Math.max(0, braceDepth - 1);
                }
            }
        }
        return maxLoopDepth;
    }

    /**
     * For Python — uses indentation level since there are no braces.
     * A loop keyword at indent N means nesting = (N / indentUnit) + 1.
     */
    private int calculatePythonNestingDepth(String code) {
        String[] lines   = code.split("\n");
        int maxDepth     = 0;
        int indentUnit   = 0;  // detected indent size (spaces per level)

        for (String line : lines) {
            if (line.isBlank()) continue;
            String trimmed = line.stripLeading();

            boolean isLoop = FOR_LOOP_COLON.matcher(trimmed).find()
                          || WHILE_LOOP.matcher(trimmed).find();

            if (isLoop) {
                int spaces = line.length() - trimmed.length();
                // Auto-detect indent unit from first indented loop
                if (indentUnit == 0 && spaces > 0) indentUnit = spaces;
                int depth = (indentUnit > 0) ? (spaces / indentUnit) + 1 : 1;
                maxDepth  = Math.max(maxDepth, depth);
            }
        }
        return maxDepth;
    }

    // ─── Loop counter — language-aware ───────────────────────────────────────

    private int countLoops(String code, String language) {
        boolean isPython = "python".equalsIgnoreCase(language)
                        || "python3".equalsIgnoreCase(language);
        int count = 0;
        if (isPython) {
            count += countMatches(code, FOR_LOOP_COLON);
            count += countMatches(code, WHILE_LOOP);
        } else {
            count += countMatches(code, FOR_LOOP_BRACE);
            count += countMatches(code, WHILE_LOOP);
            count += countMatches(code, DO_WHILE);
            count += countMatches(code, FOREACH_LOOP);
        }
        return count;
    }

    // ─── Recursion detection ──────────────────────────────────────────────────

    private boolean detectRecursion(String code) {
        Matcher m = FUNC_DECL.matcher(code);
        while (m.find()) {
            // Pick whichever capture group matched
            String name = m.group(1) != null ? m.group(1)
                        : m.group(2) != null ? m.group(2)
                        : m.group(3) != null ? m.group(3)
                        : m.group(4);
            if (name != null && !name.isBlank()
                    && !name.equals("main")
                    && !name.equals("init")) {
                String rest = code.substring(m.end());
                // Function calls itself
                if (rest.contains(name + "(")) return true;
            }
        }
        return false;
    }

    // ─── Comment removal ──────────────────────────────────────────────────────

    private String removeComments(String code, String language) {
        if (code == null) return "";
        String result = code;

        boolean isPython = "python".equalsIgnoreCase(language)
                        || "python3".equalsIgnoreCase(language);

        if (isPython) {
            // Remove # comments (but keep the newline so indentation is preserved)
            result = result.replaceAll("#[^\n]*", "");
            // Remove triple-quoted docstrings
            result = result.replaceAll("\"\"\"[\\s\\S]*?\"\"\"", "");
            result = result.replaceAll("'''[\\s\\S]*?'''", "");
        } else {
            // Remove // single-line comments
            result = result.replaceAll("//[^\n]*", "");
            // Remove /* */ multi-line comments
            result = result.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        }

        // Remove string literals to prevent false positives on keyword matches
        result = result.replaceAll("\"[^\"\\n]*\"", "\"\"");
        result = result.replaceAll("'[^'\\n]*'",   "''");
        return result;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private int countMatches(String code, Pattern pattern) {
        int count = 0;
        Matcher m = pattern.matcher(code);
        while (m.find()) count++;
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