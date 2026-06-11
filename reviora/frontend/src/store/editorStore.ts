import { create } from 'zustand'

export type Language = 'cpp' | 'java' | 'python' | 'javascript' | 'typescript' | 'go' | 'rust' | 'c'

export interface ExecutionResult {
  stdout: string
  stderr: string
  exitCode: number
  executionTime: number
  memoryUsed?: number
}

export interface ComplexityReport {
  timeComplexity: string
  spaceComplexity: string
  pattern: string
  confidence: number
  explanation: string
  nestingDepth: number
  loopCount: number
  recursionDetected: boolean
}

export interface AIReview {
  currentApproach: string
  suggestedOptimization: string
  expectedComplexity: string
  interviewNotes: string
  alternativeApproaches: string[]
  codeQualityScore: number
}

export interface TestCase {
  input: string
  expectedOutput: string
  description: string
  category: 'edge' | 'normal' | 'stress' | 'adversarial'
}

export interface GrowthDataPoint {
  inputSize: number
  operations: number
  executionTimeMs: number
}

interface EditorStore {
  code: string
  language: Language
  stdin: string
  isRunning: boolean
  isAnalyzing: boolean
  executionResult: ExecutionResult | null
  complexityReport: ComplexityReport | null
  aiReview: AIReview | null
  testCases: TestCase[]
  growthData: GrowthDataPoint[]
  submissionId: string | null

  setCode: (code: string) => void
  setLanguage: (lang: Language) => void
  setStdin: (stdin: string) => void
  setIsRunning: (val: boolean) => void
  setIsAnalyzing: (val: boolean) => void
  setExecutionResult: (result: ExecutionResult | null) => void
  setComplexityReport: (report: ComplexityReport | null) => void
  setAIReview: (review: AIReview | null) => void
  setTestCases: (cases: TestCase[]) => void
  setGrowthData: (data: GrowthDataPoint[]) => void
  setSubmissionId: (id: string | null) => void
  reset: () => void
}

const DEFAULT_CODE: Record<Language, string> = {
  cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {
    int n;
    cin >> n;
    
    // Two Sum - Brute Force O(n²)
    vector<int> arr(n);
    for (int i = 0; i < n; i++) cin >> arr[i];
    
    int target;
    cin >> target;
    
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (arr[i] + arr[j] == target) {
                cout << i << " " << j << endl;
                return 0;
            }
        }
    }
    
    return 0;
}`,
  java: `import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = sc.nextInt();
        int target = sc.nextInt();
        
        // Two Sum - O(n) with HashMap
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int complement = target - arr[i];
            if (map.containsKey(complement)) {
                System.out.println(map.get(complement) + " " + i);
                return;
            }
            map.put(arr[i], i);
        }
    }
}`,
  python: `def two_sum(nums, target):
    # O(n) HashMap approach
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []

n = int(input())
arr = list(map(int, input().split()))
target = int(input())
print(two_sum(arr, target))`,
  javascript: `const lines = require('fs').readFileSync('/dev/stdin', 'utf8').trim().split('\\n');
const n = parseInt(lines[0]);
const arr = lines[1].split(' ').map(Number);
const target = parseInt(lines[2]);

function twoSum(nums, target) {
    const map = new Map();
    for (let i = 0; i < nums.length; i++) {
        const complement = target - nums[i];
        if (map.has(complement)) return [map.get(complement), i];
        map.set(nums[i], i);
    }
    return [];
}

console.log(twoSum(arr, target).join(' '));`,
  typescript: `const lines = require('fs').readFileSync('/dev/stdin', 'utf8').trim().split('\\n');
const n = parseInt(lines[0]);
const arr: number[] = lines[1].split(' ').map(Number);
const target: number = parseInt(lines[2]);

function twoSum(nums: number[], target: number): number[] {
    const map = new Map<number, number>();
    for (let i = 0; i < nums.length; i++) {
        const complement = target - nums[i];
        if (map.has(complement)) return [map.get(complement)!, i];
        map.set(nums[i], i);
    }
    return [];
}

console.log(twoSum(arr, target).join(' '));`,
  go: `package main

import "fmt"

func twoSum(nums []int, target int) (int, int) {
    seen := make(map[int]int)
    for i, num := range nums {
        complement := target - num
        if j, ok := seen[complement]; ok {
            return j, i
        }
        seen[num] = i
    }
    return -1, -1
}

func main() {
    var n int
    fmt.Scan(&n)
    nums := make([]int, n)
    for i := range nums { fmt.Scan(&nums[i]) }
    var target int
    fmt.Scan(&target)
    i, j := twoSum(nums, target)
    fmt.Println(i, j)
}`,
  rust: `use std::collections::HashMap;
use std::io::{self, BufRead};

fn main() {
    let stdin = io::stdin();
    let mut lines = stdin.lock().lines();
    
    let n: usize = lines.next().unwrap().unwrap().trim().parse().unwrap();
    let nums: Vec<i64> = lines.next().unwrap().unwrap()
        .split_whitespace().map(|x| x.parse().unwrap()).collect();
    let target: i64 = lines.next().unwrap().unwrap().trim().parse().unwrap();
    
    let mut map: HashMap<i64, usize> = HashMap::new();
    for (i, &num) in nums.iter().enumerate() {
        let complement = target - num;
        if let Some(&j) = map.get(&complement) {
            println!("{} {}", j, i);
            return;
        }
        map.insert(num, i);
    }
}`,
  c: `#include <stdio.h>
#include <stdlib.h>

int main() {
    int n;
    scanf("%d", &n);
    int arr[n], target;
    for (int i = 0; i < n; i++) scanf("%d", &arr[i]);
    scanf("%d", &target);
    
    // O(n²) brute force
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (arr[i] + arr[j] == target) {
                printf("%d %d\\n", i, j);
                return 0;
            }
        }
    }
    return 0;
}`,
}

export const useEditorStore = create<EditorStore>((set) => ({
  code: DEFAULT_CODE['cpp'],
  language: 'cpp',
  stdin: '5\n2 7 11 15 3\n9',
  isRunning: false,
  isAnalyzing: false,
  executionResult: null,
  complexityReport: null,
  aiReview: null,
  testCases: [],
  growthData: [],
  submissionId: null,

  setCode: (code) => set({ code }),
  setLanguage: (language) => set({ language, code: DEFAULT_CODE[language] }),
  setStdin: (stdin) => set({ stdin }),
  setIsRunning: (isRunning) => set({ isRunning }),
  setIsAnalyzing: (isAnalyzing) => set({ isAnalyzing }),
  setExecutionResult: (executionResult) => set({ executionResult }),
  setComplexityReport: (complexityReport) => set({ complexityReport }),
  setAIReview: (aiReview) => set({ aiReview }),
  setTestCases: (testCases) => set({ testCases }),
  setGrowthData: (growthData) => set({ growthData }),
  setSubmissionId: (submissionId) => set({ submissionId }),
  reset: () =>
    set({
      executionResult: null,
      complexityReport: null,
      aiReview: null,
      testCases: [],
      growthData: [],
      submissionId: null,
    }),
}))

export { DEFAULT_CODE }
