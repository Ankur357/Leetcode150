/*
 * ============================================================================
 * LeetCode 14. Longest Common Prefix                          [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Write a function to find the longest common prefix string amongst an array
 * of strings.
 *
 * If there is no common prefix, return an empty string "".
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= strs.length <= 200
 *   0 <= strs[i].length <= 200
 *   strs[i] consists of only lowercase English letters (per LeetCode).
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  strs = ["flower","flow","flight"]
 *   Output: "fl"
 *   Explanation: All three share the prefix "fl"; "flo" fails on "flight".
 *
 * Example 2:
 *   Input:  strs = ["dog","racecar","car"]
 *   Output: ""
 *   Explanation: There is no common prefix among the input strings.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. If there's no common prefix, return "" (not null)?
 *      -> Yes, empty string.
 *
 *  Q2. Can any string be EMPTY? Can the array have a single string?
 *      -> A string may be length 0 (then the LCP is "" immediately). A single
 *         string means the whole string is its own LCP.
 *
 *  Q3. Case sensitivity — is "Flower" vs "flower" a match?
 *      -> Per constraints inputs are lowercase, so no case handling needed.
 *         (If mixed case were allowed, I'd clarify whether to compare case-
 *         insensitively.)
 *
 *  Q4. Prefix must be CONTIGUOUS from index 0 (a true prefix), right?
 *      -> Yes, character-by-character from the start; not a subsequence.
 *
 *  Q5. Are there any non-letter characters?
 *      -> No, only lowercase letters. Comparison is a plain char equality.
 *
 *  Q6. Any preference on time/space?
 *      -> All approaches are ~O(S) where S = total characters. I'll present
 *         vertical scanning (early exit) as the primary answer.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class LongestCommonPrefix {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — HORIZONTAL SCANNING (fold the prefix across strings)
     * ------------------------------------------------------------------------
     * Idea:
     *   Start with prefix = strs[0]. For each subsequent string, shrink the
     *   prefix from the end until it is a prefix of that string
     *   (str.indexOf(prefix) == 0). If prefix becomes empty, stop early.
     *
     *   LCP(s0, s1, ..., sn) = LCP(LCP(s0, s1), s2, ...) — we fold left to right.
     *
     * Time  : O(S)   -- S = sum of all characters; each char compared O(1) times
     * Space : O(1)   -- (ignoring the substring the result needs)
     */
    public String longestCommonPrefixHorizontal(String[] strs) {
        if (strs.length == 0) return "";
        String prefix = strs[0];
        for (int i = 1; i < strs.length; i++) {
            // Shrink prefix until it matches the start of strs[i].
            while (strs[i].indexOf(prefix) != 0) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        }
        return prefix;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (vertical scanning, column by column)
     * ------------------------------------------------------------------------
     * Idea:
     *   Compare characters COLUMN by column across all strings. For column c,
     *   take the character from strs[0] and check every other string has the
     *   same character at c. The moment one differs (or a string ends), the LCP
     *   is strs[0].substring(0, c).
     *
     *   This early-exits at the first mismatch, so on inputs with a short common
     *   prefix it barely reads past it — the best practical behavior.
     *
     * Time  : O(S) worst case, but only O(minLen * numStrings) up to the first
     *         mismatch; often far less.
     * Space : O(1) extra.
     */
    public String longestCommonPrefix(String[] strs) {
        if (strs.length == 0) return "";
        // Walk columns of the FIRST string; it bounds the possible prefix length.
        for (int c = 0; c < strs[0].length(); c++) {
            char ch = strs[0].charAt(c);
            for (int i = 1; i < strs.length; i++) {
                // Mismatch if this string is too short OR differs at column c.
                if (c == strs[i].length() || strs[i].charAt(c) != ch) {
                    return strs[0].substring(0, c);
                }
            }
        }
        return strs[0];   // the whole first string is a common prefix
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — SORT + COMPARE ENDS (elegant trick)
     * ------------------------------------------------------------------------
     * Idea:
     *   Sort the strings lexicographically. Then only the FIRST and LAST strings
     *   need comparing: everything in between shares at least their common
     *   prefix. Compare strs[first] and strs[last] character by character.
     *
     *   Why it works: sorting groups strings so the two extremes are the most
     *   "different"; any prefix common to both extremes is common to all.
     *
     * Time  : O(S log n)   -- sorting dominates (n strings)
     * Space : O(1) extra (or O(n) depending on sort); mutates order of a copy.
     *
     * Slower asymptotically than vertical scan, but a slick answer to mention.
     */
    public String longestCommonPrefixSort(String[] strs) {
        if (strs.length == 0) return "";
        String[] copy = Arrays.copyOf(strs, strs.length);
        Arrays.sort(copy);
        String first = copy[0], last = copy[copy.length - 1];
        int i = 0;
        while (i < first.length() && i < last.length() && first.charAt(i) == last.charAt(i)) {
            i++;
        }
        return first.substring(0, i);
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Many prefix QUERIES against the same set of strings — precompute?"
     *      -> Build a Trie of all strings. The LCP is the path from the root
     *         until a node branches (>1 child) or marks a word end. O(S) build,
     *         then each LCP query is O(prefix length).
     *
     *  F2. "Why compare only the sorted first and last strings?"
     *      -> Lexicographic order places the most divergent strings at the ends;
     *         any character position where all strings agree must also agree
     *         between the two extremes, so their common prefix bounds the LCP.
     *
     *  F3. "Binary search on the prefix LENGTH?"
     *      -> The 'is L a common prefix?' predicate is monotonic (if length L
     *         works, so does any shorter). Binary search L in [0, minLen],
     *         checking all strings each time: O(S log(minLen)).
     *
     *  F4. "Divide and conquer?"
     *      -> LCP(range) = LCP(LCP(left half), LCP(right half)); recurse and
     *         merge. Same O(S) work, parallelizable.
     *
     *  F5. "Return the longest common SUFFIX instead."
     *      -> Mirror the vertical scan from the ends of the strings, or reverse
     *         all strings and reuse the prefix logic.
     *
     *  F6. "Handle Unicode / case-insensitive matching."
     *      -> Compare code points (or normalized forms) rather than chars, and
     *         lowercase both sides for case-insensitive matching. Watch surrogate
     *         pairs so you don't split a code point mid-prefix.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String[] strs, LongestCommonPrefix sol) {
        System.out.println(label + " " + Arrays.toString(strs)
                + " -> vertical=\"" + sol.longestCommonPrefix(strs) + "\""
                + ", horizontal=\"" + sol.longestCommonPrefixHorizontal(strs) + "\""
                + ", sort=\"" + sol.longestCommonPrefixSort(strs) + "\"");
    }

    public static void main(String[] args) {
        LongestCommonPrefix sol = new LongestCommonPrefix();

        // Examples
        report("Example 1 ->", new String[]{"flower", "flow", "flight"}, sol); // "fl"
        report("Example 2 ->", new String[]{"dog", "racecar", "car"}, sol);    // ""

        // Edge cases
        report("Single string     ->", new String[]{"alone"}, sol);                  // "alone"
        report("Identical strings ->", new String[]{"abc", "abc", "abc"}, sol);      // "abc"
        report("One empty string  ->", new String[]{"abc", "", "ab"}, sol);          // ""
        report("Full match short  ->", new String[]{"ab", "abc", "abcd"}, sol);      // "ab"
        report("Prefix is whole   ->", new String[]{"prefix", "prefixes"}, sol);     // "prefix"
        report("Differ at first   ->", new String[]{"a", "b", "c"}, sol);            // ""
    }
}
