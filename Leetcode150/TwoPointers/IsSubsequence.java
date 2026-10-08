/*
 * ============================================================================
 * LeetCode 392. Is Subsequence                                [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given two strings s and t, return true if s is a SUBSEQUENCE of t, or false
 * otherwise.
 *
 * A subsequence of a string is a new string that is formed from the original
 * string by deleting some (can be none) of the characters without disturbing
 * the relative positions of the remaining characters. (i.e., "ace" is a
 * subsequence of "abcde" while "aec" is not).
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   0 <= s.length <= 100
 *   0 <= t.length <= 10^4
 *   s and t consist only of lowercase English letters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "abc", t = "ahbgdc"
 *   Output: true
 *   Explanation: a..b...c appears in order within "ahbgdc".
 *
 * Example 2:
 *   Input:  s = "axc", t = "ahbgdc"
 *   Output: false
 *   Explanation: We can match a and c, but 'x' never appears, so false.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Subsequence keeps RELATIVE ORDER but need not be contiguous?
 *      -> Correct. Unlike a substring, gaps are allowed; order must hold.
 *
 *  Q2. Is the EMPTY string s a subsequence of any t?
 *      -> Yes. Deleting all of t (or matching nothing) leaves "", so empty s
 *         is always a subsequence -> true.
 *
 *  Q3. Case sensitivity?
 *      -> Lowercase only per constraints; plain char equality.
 *
 *  Q4. Can s be longer than t?
 *      -> Yes; then it can't possibly be a subsequence -> false (the two-pointer
 *         naturally returns false when s isn't fully consumed).
 *
 *  Q5. The FOLLOW-UP: many s values (say 10^9) against the same t — optimize?
 *      -> Yes, that's the classic follow-up. Preprocess t so each query is
 *         fast (binary search over per-character position lists, or a next-
 *         position DP table). I'll cover both.
 *
 *  Q6. Do duplicate characters matter?
 *      -> They're handled naturally: we advance through t consuming each s
 *         character in order, duplicates included.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

public class IsSubsequence {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — OPTIMAL FOR A SINGLE QUERY (two pointers)
     * ------------------------------------------------------------------------
     * Idea:
     *   Pointer i scans s, pointer j scans t. Whenever s.charAt(i) matches
     *   t.charAt(j), advance i (we've placed that character). Always advance j.
     *   If i reaches the end of s, every character was matched in order -> true.
     *
     * Time  : O(t.length)   -- single pass over t
     * Space : O(1)
     *
     * This is the natural, best answer for one (s, t) pair.
     */
    public boolean isSubsequence(String s, String t) {
        int i = 0, j = 0;
        int n = s.length(), m = t.length();
        while (i < n && j < m) {
            if (s.charAt(i) == t.charAt(j)) {
                i++;                    // matched this character of s
            }
            j++;                        // always move forward in t
        }
        return i == n;                  // consumed all of s?
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — BRUTE FORCE / RECURSIVE (for contrast)
     * ------------------------------------------------------------------------
     * Idea:
     *   At each step, if the current characters match, advance both; otherwise
     *   advance only t. Base cases: s exhausted -> true; t exhausted first ->
     *   false. Logically identical to the two-pointer, just recursive.
     *
     * Time  : O(t.length)
     * Space : O(t.length) recursion depth (or O(1) if tail-optimized manually)
     *
     * Included to show the same greedy match expressed recursively; the
     * iterative version is preferred (no stack overhead).
     */
    public boolean isSubsequenceRecursive(String s, String t) {
        return rec(s, t, 0, 0);
    }

    private boolean rec(String s, String t, int i, int j) {
        if (i == s.length()) return true;      // all of s matched
        if (j == t.length()) return false;     // t exhausted, s left over
        if (s.charAt(i) == t.charAt(j)) {
            return rec(s, t, i + 1, j + 1);
        }
        return rec(s, t, i, j + 1);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — FOLLOW-UP (preprocess t for MANY queries)
     * ------------------------------------------------------------------------
     * Idea:
     *   If we must answer k >> 1 different s against the SAME t, the O(t) per
     *   query becomes O(k * t). Instead, preprocess t ONCE:
     *     For each letter, store the sorted list of indices where it occurs in t.
     *   For a query s, keep a `prev` position (last matched index in t). For each
     *   character c of s, BINARY SEARCH its index list for the smallest index
     *   strictly greater than `prev`. If none exists -> false; else set prev to
     *   that index and continue.
     *
     * Preprocess : O(t.length)
     * Per query  : O(s.length * log t.length)
     *
     * This is the accepted answer to the standard "billions of s" follow-up.
     */
    static class SubsequenceMatcher {
        private final List<Integer>[] positions;   // positions['a'..'z']

        @SuppressWarnings("unchecked")
        SubsequenceMatcher(String t) {
            positions = new List[26];
            for (int c = 0; c < 26; c++) positions[c] = new ArrayList<>();
            for (int j = 0; j < t.length(); j++) {
                positions[t.charAt(j) - 'a'].add(j);   // indices are added in increasing order
            }
        }

        boolean isSubsequence(String s) {
            int prev = -1;                             // last matched index in t
            for (int i = 0; i < s.length(); i++) {
                List<Integer> list = positions[s.charAt(i) - 'a'];
                int idx = upperBound(list, prev);      // smallest index > prev
                if (idx == list.size()) return false;  // no valid position left
                prev = list.get(idx);                  // advance past it
            }
            return true;
        }

        // Returns the first position in `list` whose value is strictly > target.
        private int upperBound(List<Integer> list, int target) {
            int lo = 0, hi = list.size();
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (list.get(mid) <= target) lo = mid + 1;
                else hi = mid;
            }
            return lo;
        }
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "If there are 10^9 incoming s values, all against the same t?"
     *      -> Preprocess t once (Approach 3): per-character index lists + binary
     *         search per s character. O(t) build, O(|s| log t) per query. No
     *         re-scanning t for every query.
     *
     *  F2. "Alternative preprocessing to binary search?"
     *      -> A 'next-position' DP table: next[i][c] = index of the next
     *         occurrence of char c at or after position i in t. O(t * 26) space,
     *         O(|s|) per query with O(1) lookups (no log factor).
     *
     *  F3. "Why is the greedy two-pointer correct — could matching a later
     *       occurrence ever help?"
     *      -> No. Matching each s character at its EARLIEST available position
     *         leaves the most of t for the remaining characters; a later choice
     *         can never enable a match the earliest choice forbids.
     *
     *  F4. "Longest common subsequence / edit distance connection?"
     *      -> s is a subsequence of t iff LCS(s, t) == s.length(). The two-
     *         pointer is a specialized O(t) check; general LCS is O(|s|*|t|) DP.
     *
     *  F5. "Count HOW MANY distinct ways s appears as a subsequence of t."
     *      -> Different problem (LC115, Distinct Subsequences): DP where
     *         dp[i][j] counts ways. O(|s|*|t|).
     *
     *  F6. "Streaming t (arrives one char at a time)?"
     *      -> The two-pointer works online: hold i into s, advance it whenever
     *         the incoming t character matches s.charAt(i); done when i == |s|.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, String t, IsSubsequence sol) {
        SubsequenceMatcher matcher = new SubsequenceMatcher(t);
        System.out.println(label + " s=\"" + s + "\", t=\"" + t + "\""
                + " -> twoPtr=" + sol.isSubsequence(s, t)
                + ", recursive=" + sol.isSubsequenceRecursive(s, t)
                + ", preprocessed=" + matcher.isSubsequence(s));
    }

    public static void main(String[] args) {
        IsSubsequence sol = new IsSubsequence();

        // Examples
        report("Example 1 ->", "abc", "ahbgdc", sol);   // true
        report("Example 2 ->", "axc", "ahbgdc", sol);   // false

        // Edge cases
        report("Empty s          ->", "", "anything", sol);      // true
        report("Empty t          ->", "a", "", sol);             // false
        report("Both empty       ->", "", "", sol);              // true
        report("s longer than t  ->", "abcd", "abc", sol);       // false
        report("Exact match      ->", "abc", "abc", sol);        // true
        report("Duplicates in s  ->", "aab", "acadb", sol);      // true (a..a..b in order)
    }
}
