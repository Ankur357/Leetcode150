/*
 * ============================================================================
 * LeetCode 76. Minimum Window Substring                         [Difficulty: Hard]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given two strings s and t of lengths m and n respectively, return the
 * MINIMUM WINDOW SUBSTRING of s such that every character in t (INCLUDING
 * DUPLICATES) is included in the window. If there is no such substring, return
 * the empty string "".
 *
 * The test cases are generated such that the answer is UNIQUE.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   m == s.length
 *   n == t.length
 *   1 <= m, n <= 10^5
 *   s and t consist of uppercase and lowercase English letters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "ADOBECODEBANC", t = "ABC"
 *   Output: "BANC"
 *   Explanation: The minimum window "BANC" includes 'A', 'B', and 'C' from t.
 *
 * Example 2:
 *   Input:  s = "a", t = "a"
 *   Output: "a"
 *   Explanation: The entire string s is the minimum window.
 *
 * Example 3:
 *   Input:  s = "a", t = "aa"
 *   Output: ""
 *   Explanation: Both 'a's from t must be included; s has only one 'a', so no
 *                valid window exists -> "".
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Do DUPLICATE characters in t matter?
 *      -> Yes. If t = "aa", the window must contain two 'a's. So I match a
 *         MULTISET (counts), not just a set of characters.
 *
 *  Q2. Character set? Upper + lower English letters — is it case-sensitive?
 *      -> Case-sensitive; 'A' != 'a'. An int[128] over ASCII covers it.
 *
 *  Q3. Return the substring itself, or its bounds?
 *      -> The actual substring; return "" when no window exists.
 *
 *  Q4. Is the answer guaranteed unique?
 *      -> Yes (stated). So I just track the single shortest window.
 *
 *  Q5. Can t be longer than s?
 *      -> Yes; then the answer is "" (can't cover t). Handled naturally, but I
 *         can short-circuit if t.length() > s.length().
 *
 *  Q6. Contiguous window (substring), not a subsequence?
 *      -> Contiguous — which is exactly what the sliding window relies on.
 *
 * ============================================================================
 */

import java.util.HashMap;
import java.util.Map;

public class MinimumWindowSubstring {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check every substring)
     * ------------------------------------------------------------------------
     * Idea:
     *   For every start i, extend end j and maintain a count map of the window.
     *   As soon as the window "covers" t (has >= the required count of every
     *   char in t), record its length and stop extending this start. Keep the
     *   global minimum window bounds.
     *
     * Time  : O(m^2 * |charset|) in the worst case (containment check per step)
     *         — practically O(m^2) with a running "have vs need" counter.
     * Space : O(|charset|)
     *
     * Correct but quadratic; the two-pointer window makes it linear.
     */
    public String minWindowBruteForce(String s, String t) {
        int m = s.length(), n = t.length();
        if (n == 0 || m < n) return "";

        int[] need = new int[128];
        for (int i = 0; i < n; i++) need[t.charAt(i)]++;

        int bestLen = Integer.MAX_VALUE, bestStart = -1;
        for (int i = 0; i < m; i++) {
            int[] have = new int[128];
            int covered = 0;                 // distinct t-chars fully satisfied
            int required = distinctRequired(need);
            for (int j = i; j < m; j++) {
                char c = s.charAt(j);
                if (need[c] > 0) {
                    have[c]++;
                    if (have[c] == need[c]) covered++;
                }
                if (covered == required) {   // window [i, j] covers t
                    if (j - i + 1 < bestLen) {
                        bestLen = j - i + 1;
                        bestStart = i;
                    }
                    break;                   // shortest window for this start
                }
            }
        }
        return bestStart == -1 ? "" : s.substring(bestStart, bestStart + bestLen);
    }

    private int distinctRequired(int[] need) {
        int d = 0;
        for (int v : need) if (v > 0) d++;
        return d;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (sliding window + have/need counts)  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Precompute need[c] = how many of char c the window must contain.
     *   Track `required` = number of DISTINCT chars we still must fully satisfy.
     *   Expand `right`, adding s[right] to have[]. When have[c] reaches need[c]
     *   for that char, one more requirement is met (formed++). Once formed ==
     *   required, the window is valid: try to SHRINK from the left, recording
     *   the smallest valid window, and stop shrinking the moment a requirement
     *   is broken.
     *
     *   Each character enters and leaves the window at most once -> O(m).
     *
     * Time  : O(m + n)
     * Space : O(|charset|)  -- two fixed int[128] arrays -> effectively O(1)
     */
    public String minWindow(String s, String t) {
        int m = s.length(), n = t.length();
        if (n == 0 || m < n) return "";

        int[] need = new int[128];
        int required = 0;                    // distinct chars that must be satisfied
        for (int i = 0; i < n; i++) {
            if (need[t.charAt(i)] == 0) required++;
            need[t.charAt(i)]++;
        }

        int[] have = new int[128];
        int formed = 0;                      // distinct chars currently satisfied
        int left = 0;
        int bestLen = Integer.MAX_VALUE, bestStart = 0;

        for (int right = 0; right < m; right++) {
            char c = s.charAt(right);
            have[c]++;
            if (need[c] > 0 && have[c] == need[c]) formed++;

            // While the window is valid, shrink from the left to minimize it.
            while (formed == required) {
                if (right - left + 1 < bestLen) {
                    bestLen = right - left + 1;
                    bestStart = left;
                }
                char lc = s.charAt(left);
                have[lc]--;
                if (need[lc] > 0 && have[lc] < need[lc]) formed--;  // requirement broken
                left++;
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — Generic HashMap variant (any Unicode charset)
     * ------------------------------------------------------------------------
     * Idea:
     *   Same algorithm as Approach 2 but backed by HashMap<Character,Integer>
     *   instead of int[128], so it works beyond ASCII (arbitrary chars). Kept
     *   here to show the interviewer the ASCII assumption is easily lifted.
     *
     * Time  : O(m + n)
     * Space : O(k)  -- k = distinct chars in t
     */
    public String minWindowMap(String s, String t) {
        int m = s.length(), n = t.length();
        if (n == 0 || m < n) return "";

        Map<Character, Integer> need = new HashMap<>();
        for (int i = 0; i < n; i++) need.merge(t.charAt(i), 1, Integer::sum);
        int required = need.size();

        Map<Character, Integer> have = new HashMap<>();
        int formed = 0, left = 0;
        int bestLen = Integer.MAX_VALUE, bestStart = 0;

        for (int right = 0; right < m; right++) {
            char c = s.charAt(right);
            have.merge(c, 1, Integer::sum);
            if (need.containsKey(c) && have.get(c).intValue() == need.get(c).intValue()) {
                formed++;
            }
            while (formed == required) {
                if (right - left + 1 < bestLen) {
                    bestLen = right - left + 1;
                    bestStart = left;
                }
                char lc = s.charAt(left);
                have.merge(lc, -1, Integer::sum);
                if (need.containsKey(lc) && have.get(lc) < need.get(lc)) formed--;
                left++;
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why track `formed`/`required` instead of comparing full maps each step?"
     *      -> Comparing maps is O(charset) per step -> O(m*charset). The
     *         formed/required counters turn the validity check into O(1), giving
     *         a true O(m) pass. formed rises only when a char hits its exact need
     *         and falls only when it drops below — so it flips O(m) times total.
     *
     *  F2. "Why does shrinking never skip a smaller valid window?"
     *      -> We shrink as long as the window stays valid, recording the length
     *         at each step, and stop exactly when validity breaks. So for every
     *         `right` we capture the shortest valid window ending at right; the
     *         global min over all rights is optimal.
     *
     *  F3. "Unicode / arbitrary charset instead of ASCII?"
     *      -> Swap int[128] for HashMap<Character,Integer> (Approach 3); iterate
     *         by code points if surrogate pairs are possible.
     *
     *  F4. "What if we needed the minimum window covering t as a SUBSEQUENCE
     *       (order preserved), not a multiset?"
     *      -> Different problem (LeetCode 727 'Minimum Window Subsequence'):
     *         two-pointer forward-then-backtrack, or DP; O(m*n) typical.
     *
     *  F5. "Return ALL minimum windows, or the count of valid windows?"
     *      -> Problem says the answer is unique, but in general keep a list and,
     *         on ties (len == bestLen), append; for counts, increment whenever
     *         a valid window is found before shrinking.
     *
     *  F6. "Streaming s where you can't random-access or go back?"
     *      -> The window is single-pass and only ever moves left/right forward,
     *         so it adapts to a stream if you buffer the current window's chars.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, String t, MinimumWindowSubstring sol) {
        System.out.println(label + " s=\"" + s + "\", t=\"" + t + "\""
                + " -> optimal=\"" + sol.minWindow(s, t) + "\""
                + ", map=\"" + sol.minWindowMap(s, t) + "\""
                + ", brute=\"" + sol.minWindowBruteForce(s, t) + "\"");
    }

    public static void main(String[] args) {
        MinimumWindowSubstring sol = new MinimumWindowSubstring();

        // Examples
        report("Example 1 ->", "ADOBECODEBANC", "ABC", sol);   // "BANC"
        report("Example 2 ->", "a", "a", sol);                 // "a"
        report("Example 3 ->", "a", "aa", sol);                // ""

        // Edge cases
        report("Duplicates in t   ->", "aaflslflsassekfabf", "aef", sol);  // some min window w/ a,e,f
        report("t == s            ->", "ABC", "ABC", sol);                 // "ABC"
        report("Window at start   ->", "ABCxyz", "CBA", sol);              // "ABC"
        report("Repeated need     ->", "aaaaaaaaaaaabbbb", "aaab", sol);   // "aaab" region -> "aaab"
        report("No coverage       ->", "abcdef", "xyz", sol);              // ""
        report("t longer than s   ->", "ab", "abc", sol);                  // ""
    }
}
