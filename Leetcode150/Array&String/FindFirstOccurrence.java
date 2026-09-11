/*
 * ============================================================================
 * LeetCode 28. Find the Index of the First Occurrence in a String
 *                                                             [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given two strings needle and haystack, return the index of the FIRST
 * occurrence of needle in haystack, or -1 if needle is not part of haystack.
 *
 * (This is the classic "strStr" / substring search problem.)
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= haystack.length, needle.length <= 10^4
 *   haystack and needle consist of only lowercase English characters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  haystack = "sadbutsad", needle = "sad"
 *   Output: 0
 *   Explanation: "sad" occurs at index 0 and 6. The FIRST occurrence is at
 *                index 0, so we return 0.
 *
 * Example 2:
 *   Input:  haystack = "leetcode", needle = "leeto"
 *   Output: -1
 *   Explanation: "leeto" did not occur in "leetcode", so we return -1.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Return the FIRST (leftmost) match index, and -1 if absent?
 *      -> Yes. 0-based index of the start of the first match.
 *
 *  Q2. Can needle be longer than haystack?
 *      -> Yes; then it can't fit, so return -1. (Constraints allow either to be
 *         longer.)
 *
 *  Q3. Is an EMPTY needle possible?
 *      -> Constraints say length >= 1, so no. (If it were, convention returns 0,
 *         matching Java's indexOf("").)
 *
 *  Q4. Only lowercase letters — no case-folding needed?
 *      -> Correct, plain character equality.
 *
 *  Q5. Am I allowed to just call haystack.indexOf(needle)?
 *      -> In a real interview, no — they want the algorithm. I'll implement it
 *         and mention the library call exists.
 *
 *  Q6. Do they want optimal WORST-CASE time (KMP) or is brute force acceptable?
 *      -> I'll present brute force first (O(nm)) then KMP (O(n+m)) as the
 *         optimal answer with a linear worst-case guarantee.
 *
 * ============================================================================
 */

public class FindFirstOccurrence {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (sliding window, compare char by char)
     * ------------------------------------------------------------------------
     * Idea:
     *   Try every possible start i in haystack from 0 to n-m. At each i, compare
     *   the m characters of needle against haystack[i..i+m-1]. Return i on a full
     *   match; -1 if no start works.
     *
     * Time  : O(n * m) worst case  -- e.g. "aaaaaa" / "aaab" re-scans a lot
     * Space : O(1)
     *
     * Simple and often fast enough; the pathological repeats motivate KMP.
     */
    public int strStrBruteForce(String haystack, String needle) {
        int n = haystack.length(), m = needle.length();
        for (int i = 0; i + m <= n; i++) {          // last valid start is n - m
            int j = 0;
            while (j < m && haystack.charAt(i + j) == needle.charAt(j)) {
                j++;
            }
            if (j == m) return i;                    // matched all m characters
        }
        return -1;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (Knuth-Morris-Pratt, O(n + m))
     * ------------------------------------------------------------------------
     * Idea:
     *   Brute force wastes work by re-comparing characters after a mismatch. KMP
     *   precomputes, for the NEEDLE, an "LPS" (Longest Proper Prefix which is
     *   also a Suffix) array. On a mismatch at needle position j, instead of
     *   restarting, we fall back to lps[j-1] — the length of the longest prefix
     *   of needle that we've already matched — so haystack's pointer never moves
     *   backward.
     *
     *   Two phases:
     *     (1) Build lps for needle:      O(m)
     *     (2) Scan haystack once with it: O(n)
     *   Total O(n + m) time, O(m) space for the lps array.
     *
     * Time  : O(n + m)
     * Space : O(m)
     */
    public int strStr(String haystack, String needle) {
        int n = haystack.length(), m = needle.length();
        int[] lps = buildLps(needle);

        int i = 0;   // index into haystack (never decreases)
        int j = 0;   // index into needle (length of current match)
        while (i < n) {
            if (haystack.charAt(i) == needle.charAt(j)) {
                i++; j++;
                if (j == m) return i - m;            // full match ends here
            } else if (j > 0) {
                j = lps[j - 1];                      // fall back within needle
            } else {
                i++;                                 // no partial match; advance haystack
            }
        }
        return -1;
    }

    /*
     * lps[k] = length of the longest proper prefix of needle[0..k] that is also
     * a suffix of needle[0..k]. Built in O(m) with the standard two-pointer DP.
     */
    private int[] buildLps(String needle) {
        int m = needle.length();
        int[] lps = new int[m];                      // lps[0] is always 0
        int len = 0;                                 // length of the previous longest prefix-suffix
        int i = 1;
        while (i < m) {
            if (needle.charAt(i) == needle.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];                  // fall back and retry (don't advance i)
            } else {
                lps[i] = 0;                          // no prefix-suffix here
                i++;
            }
        }
        return lps;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Explain the LPS array — what does lps[k] mean and why does it help?"
     *      -> It's the longest proper prefix of needle[0..k] that is also a
     *         suffix. On a mismatch after matching j chars, those j chars share
     *         their suffix with needle's prefix of length lps[j-1], so we can
     *         resume from there instead of index 0 — no haystack backtracking.
     *
     *  F2. "Prove KMP is O(n + m)."
     *      -> haystack's pointer i only ever increases (n steps). j increases at
     *         most once per i-increment and only decreases via lps fallbacks;
     *         total decreases can't exceed total increases, so the work is
     *         linear. Building lps is similarly amortized O(m).
     *
     *  F3. "Alternative linear-ish algorithms?"
     *      -> Rabin-Karp (rolling hash): O(n+m) average, O(nm) worst on hash
     *         collisions; great for MULTIPLE pattern search. Also Z-algorithm
     *         and Boyer-Moore (sublinear in practice for large alphabets).
     *
     *  F4. "Find ALL occurrences, not just the first."
     *      -> With KMP, on a full match (j == m) record i-m, then set j = lps[m-1]
     *         and continue instead of returning. Still O(n + m).
     *
     *  F5. "What about overlapping matches, e.g. needle 'aa' in 'aaaa'?"
     *      -> The lps-continue trick above handles overlaps correctly, reporting
     *         indices 0,1,2 for 'aa' in 'aaaa'.
     *
     *  F6. "Case-insensitive or Unicode input?"
     *      -> Normalize/lowercase both strings first, or compare code points;
     *         beware surrogate pairs so a fallback never lands mid-code-point.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String haystack, String needle,
                               FindFirstOccurrence sol) {
        System.out.println(label + " haystack=\"" + haystack + "\", needle=\"" + needle + "\""
                + " -> kmp=" + sol.strStr(haystack, needle)
                + ", brute=" + sol.strStrBruteForce(haystack, needle));
    }

    public static void main(String[] args) {
        FindFirstOccurrence sol = new FindFirstOccurrence();

        // Examples
        report("Example 1 ->", "sadbutsad", "sad", sol);   // 0
        report("Example 2 ->", "leetcode", "leeto", sol);  // -1

        // Edge cases
        report("Match at end       ->", "hello", "llo", sol);            // 2
        report("Needle == haystack ->", "abc", "abc", sol);              // 0
        report("Needle longer      ->", "ab", "abc", sol);               // -1
        report("Single char hit    ->", "abcabc", "c", sol);             // 2
        report("No match           ->", "aaaaa", "aab", sol);            // -1
        report("Overlap-heavy      ->", "aaaaab", "aaab", sol);          // 2
        report("KMP fallback case  ->", "mississippi", "issip", sol);    // 4
    }
}
