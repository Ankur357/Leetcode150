/*
 * ============================================================================
 * LeetCode 3. Longest Substring Without Repeating Characters  [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given a string s, find the length of the LONGEST SUBSTRING without repeating
 * characters.
 *
 * (A substring is a contiguous, non-empty sequence of characters. A subsequence
 * is NOT the same thing — substrings must be contiguous.)
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   0 <= s.length <= 5 * 10^4
 *   s consists of English letters, digits, symbols and spaces.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "abcabcbb"
 *   Output: 3
 *   Explanation: The answer is "abc", with the length of 3.
 *
 * Example 2:
 *   Input:  s = "bbbbb"
 *   Output: 1
 *   Explanation: The answer is "b", with the length of 1.
 *
 * Example 3:
 *   Input:  s = "pwwkew"
 *   Output: 3
 *   Explanation: The answer is "wke", with the length of 3.
 *                Note "pwke" is a SUBSEQUENCE, not a substring.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. What's the character set? ASCII, Unicode, letters-only?
 *      -> "English letters, digits, symbols and spaces" — treat as extended
 *         ASCII (256). I'll use a HashMap so it also works for any charset.
 *
 *  Q2. Is it case-sensitive? Does 'a' differ from 'A'?
 *      -> Yes, case-sensitive; 'a' and 'A' are distinct characters.
 *
 *  Q3. Can the input be empty?
 *      -> Yes (0 <= length). Empty string -> answer 0.
 *
 *  Q4. Return the LENGTH, or the actual substring?
 *      -> Just the length. (I'll show how to recover the substring in F4.)
 *
 *  Q5. Contiguous substring, not a subsequence?
 *      -> Contiguous. This is what allows the sliding-window approach.
 *
 *  Q6. If multiple longest substrings tie, does it matter which?
 *      -> No — only the length is asked, so ties are irrelevant.
 *
 * ============================================================================
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LongestSubstringWithoutRepeatingCharacters {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check every substring)
     * ------------------------------------------------------------------------
     * Idea:
     * For each start i, extend j while chars stay unique (tracked in a Set).
     * The moment a repeat appears, stop and record (j - i). Try every start.
     *
     * Time : O(n^2) -- n starts, each scanning forward up to n
     * Space : O(min(n, charsetSize)) -- the Set of the current window
     *
     * Correct but quadratic; the sliding window makes each char visited O(1)
     * amortized.
     */
    public int lengthOfLongestSubstringBruteForce(String s) {
        int n = s.length();
        int best = 0;
        for (int i = 0; i < n; i++) {
            Set<Character> seen = new HashSet<>();
            int j = i;
            while (j < n && !seen.contains(s.charAt(j))) {
                seen.add(s.charAt(j));
                j++;
            }
            best = Math.max(best, j - i);
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — SLIDING WINDOW + Set (shrink one step at a time)
     * ------------------------------------------------------------------------
     * Idea:
     * Keep a window [left, right] whose chars are all unique, stored in a Set.
     * Expand `right`; if s[right] is already in the window, shrink from the
     * left (removing chars) until the duplicate is gone, then add s[right].
     * Track the max window size.
     *
     * Each character is added once and removed at most once -> O(n) amortized.
     *
     * Time : O(n)
     * Space : O(min(n, charsetSize))
     */
    public int lengthOfLongestSubstringSet(String s) {
        int n = s.length();
        Set<Character> window = new HashSet<>();
        int left = 0, best = 0;
        for (int right = 0; right < n; right++) {
            char c = s.charAt(right);
            while (window.contains(c)) { // shrink until duplicate removed
                window.remove(s.charAt(left));
                left++;
            }
            window.add(c);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL SLIDING WINDOW + last-index map (jump left) <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     * Instead of shrinking one char at a time, remember the LAST INDEX where
     * each character was seen. When s[right] was seen at index p and p is
     * inside the current window (p >= left), JUMP left directly to p + 1.
     * This removes the inner while-loop entirely.
     *
     * The max(...) guard on `left` is essential: a stale last-index that lies
     * BEFORE the current left must NOT drag left backward.
     *
     * Time : O(n) -- single pass, each step O(1)
     * Space : O(min(n, charsetSize))
     */
    public int lengthOfLongestSubstring(String s) {
        int n = s.length();
        Map<Character, Integer> lastIndex = new HashMap<>();
        int left = 0, best = 0;
        for (int right = 0; right < n; right++) {
            char c = s.charAt(right);
            if (lastIndex.containsKey(c)) {
                // jump left past the previous occurrence, but never backward
                left = Math.max(left, lastIndex.get(c) + 1);
            }
            lastIndex.put(c, right);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 4 — OPTIMAL with a fixed int[] array (fastest, ASCII)
     * ------------------------------------------------------------------------
     * Idea:
     * Same as Approach 3 but back the last-index lookup with an int[128]
     * (ASCII) initialized to -1, avoiding HashMap overhead. Store index+1 or
     * use -1 sentinels; here we store the actual index and guard with left.
     *
     * Time : O(n)
     * Space : O(1) -- fixed 128-slot array, independent of input length
     */
    public int lengthOfLongestSubstringArray(String s) {
        int[] lastIndex = new int[128];
        java.util.Arrays.fill(lastIndex, -1);
        int left = 0, best = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastIndex[c] >= left) {
                left = lastIndex[c] + 1;
            }
            lastIndex[c] = right;
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why does the last-index jump never move `left` backward?"
     * -> A character's stored index may predate the current window (its
     * occurrence was already evicted). max(left, p+1) keeps left
     * monotonically non-decreasing, so the window never expands leftward.
     *
     * F2. "Full Unicode instead of ASCII?"
     * -> Use the HashMap version (Approach 3); iterate by code points
     * (s.codePointAt / offsetByCodePoints) so surrogate pairs count as
     * one character. The int[128] version assumes ASCII.
     *
     * F3. "Longest substring with AT MOST K distinct characters?"
     * -> Generalized sliding window: keep a count map; shrink from the left
     * while map.size() > K. O(n). (LeetCode 340.)
     *
     * F4. "Return the actual substring, not just the length."
     * -> Track bestLeft whenever best improves (best = right-left+1 →
     * record bestStart = left); return s.substring(bestStart,
     * bestStart + best).
     *
     * F5. "What about a streaming input you can't index randomly?"
     * -> The single-pass window still works; just feed chars one at a time
     * and keep the last-seen map + left pointer as running state.
     *
     * F6.
     * "Longest substring where each char appears an even number of times, etc.?"
     * -> Different technique: bitmask of parity + prefix states in a map
     * (LeetCode 1371). The plain uniqueness window no longer applies.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all four approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, LongestSubstringWithoutRepeatingCharacters sol) {
        System.out.println(label + " s=\"" + s + "\""
                + " -> map=" + sol.lengthOfLongestSubstring(s)
                + ", array=" + sol.lengthOfLongestSubstringArray(s)
                + ", set=" + sol.lengthOfLongestSubstringSet(s)
                + ", brute=" + sol.lengthOfLongestSubstringBruteForce(s));
    }

    public static void main(String[] args) {
        LongestSubstringWithoutRepeatingCharacters sol = new LongestSubstringWithoutRepeatingCharacters();

        // Examples
        report("Example 1 ->", "abcabcbb", sol); // 3 ("abc")
        report("Example 2 ->", "bbbbb", sol); // 1 ("b")
        report("Example 3 ->", "pwwkew", sol); // 3 ("wke")

        // Edge cases
        report("Empty          ->", "", sol); // 0
        report("Single char    ->", "a", sol); // 1
        report("All unique     ->", "abcdef", sol); // 6
        report("Spaces/symbols ->", "a b!a b!", sol); // 3 ("a b" / "b!a"... => 3)
        report("Stale index    ->", "abba", sol); // 2 (guards left backward)
        report("Repeat at end  ->", "tmmzuxt", sol); // 5 ("mzuxt")
    }
}
