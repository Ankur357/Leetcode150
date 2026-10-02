package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 242. Valid Anagram                                  [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given two strings s and t, return true if t is an ANAGRAM of s, and false
 * otherwise.
 *
 * An anagram is a word or phrase formed by rearranging the letters of a
 * different word or phrase, typically using ALL the original letters exactly
 * once.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length, t.length <= 5 * 10^4
 *   s and t consist of LOWERCASE English letters.
 *
 * Follow-up:
 *   What if the inputs contain UNICODE characters? How would you adapt your
 *   solution to such a case?
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "anagram", t = "nagaram"
 *   Output: true
 *
 * Example 2:
 *   Input:  s = "rat", t = "car"
 *   Output: false
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Does an anagram require the SAME MULTISET of letters (same counts)?
 *      -> Yes. Same letters, same frequencies, just reordered. So length must
 *         match and every letter's count must match.
 *
 *  Q2. Character set — lowercase English only?
 *      -> Yes per constraints, so int[26] suffices. The follow-up asks about
 *         Unicode; I'll cover a HashMap variant for that.
 *
 *  Q3. Case-sensitive? Whitespace/punctuation?
 *      -> Lowercase letters only; no case-folding or symbol handling needed.
 *
 *  Q4. Is a string an anagram of ITSELF (identical strings)?
 *      -> Yes — identical strings trivially have the same letter counts.
 *
 *  Q5. Quick reject?
 *      -> If lengths differ, it can't be an anagram -> return false immediately.
 *
 *  Q6. Return type?
 *      -> boolean: true iff t is an anagram of s.
 *
 * ============================================================================
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ValidAnagram {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — SORT BOTH STRINGS AND COMPARE [clean baseline]
     * ------------------------------------------------------------------------
     * Idea:
     * Two strings are anagrams iff their sorted character sequences are equal.
     * Sort both char arrays and compare.
     *
     * Time : O(n log n) -- dominated by the sort
     * Space : O(n) -- the char arrays (or O(log n) depending on sort)
     *
     * Simple and obviously correct; the counting approach beats it to O(n).
     */
    public boolean isAnagramSort(String s, String t) {
        if (s.length() != t.length())
            return false;
        char[] a = s.toCharArray();
        char[] b = t.toCharArray();
        Arrays.sort(a);
        Arrays.sort(b);
        return Arrays.equals(a, b);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: FREQUENCY COUNT with int[26] <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     * One pass: increment counts for s, decrement for t, in the same array.
     * If s and t are anagrams, every count returns to 0. A single final scan
     * (or an early bail if any count goes negative) confirms it.
     *
     * Time : O(n)
     * Space : O(1) -- fixed 26-slot array
     */
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length())
            return false;

        int[] counts = new int[26];
        for (int i = 0; i < s.length(); i++) {
            counts[s.charAt(i) - 'a']++;
            counts[t.charAt(i) - 'a']--;
        }
        for (int c : counts) {
            if (c != 0)
                return false; // some letter's counts didn't cancel
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — HASH MAP counts (Unicode-ready — answers the follow-up)
     * ------------------------------------------------------------------------
     * Idea:
     * Same count-and-cancel logic, but keyed by int CODE POINTS in a HashMap
     * so it handles arbitrary Unicode (including characters outside the BMP
     * represented as surrogate pairs). Increment for s, decrement for t, then
     * verify all counts are zero.
     *
     * Time : O(n)
     * Space : O(k) -- k = distinct code points
     */
    public boolean isAnagramUnicode(String s, String t) {
        if (s.codePointCount(0, s.length()) != t.codePointCount(0, t.length())) {
            // Note: length() differing is a fast reject; code-point count is the
            // precise check when surrogate pairs are involved.
            if (s.length() != t.length())
                return false;
        }

        Map<Integer, Integer> counts = new HashMap<>();
        s.codePoints().forEach(cp -> counts.merge(cp, 1, Integer::sum));
        t.codePoints().forEach(cp -> counts.merge(cp, -1, Integer::sum));

        for (int v : counts.values()) {
            if (v != 0)
                return false;
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Sorting vs. counting — trade-offs?"
     * -> Sorting is O(n log n) but tiny code and no charset assumption.
     * Counting is O(n) with O(1) space for a fixed alphabet — strictly
     * faster for large n. Prefer counting when the charset is bounded.
     *
     * F2. "Unicode inputs (the stated follow-up)?"
     * -> int[26] no longer works. Use a HashMap keyed by CODE POINTS
     * (Approach 3), iterating via s.codePoints() so surrogate pairs count
     * as one character rather than two chars.
     *
     * F3. "Can you avoid the second scan in the int[26] approach?"
     * -> Yes: while decrementing for t, if any count goes below 0 you can
     * return false immediately (since lengths are equal, one negative
     * implies another positive remains). Trades a final scan for an
     * inline check.
     *
     * F4. "Case-insensitive or ignore spaces/punctuation (real anagrams)?"
     * -> Normalize first: lowercase and strip non-letters, then run the same
     * count logic. 'Dormitory' vs 'Dirty room' becomes comparable.
     *
     * F5. "Group many strings by anagram (LeetCode 49)?"
     * -> Use the anagram SIGNATURE (sorted string, or a 26-length count key)
     * as a HashMap key to bucket anagrams together.
     *
     * F6. "Streaming / very long strings — memory?"
     * -> The int[26] count is already O(1) space and single-pass, ideal for
     * streaming; you just need both lengths (or stream both in parallel).
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, String t, ValidAnagram sol) {
        System.out.println(label + " s=\"" + s + "\", t=\"" + t + "\""
                + " -> count=" + sol.isAnagram(s, t)
                + ", sort=" + sol.isAnagramSort(s, t)
                + ", unicode=" + sol.isAnagramUnicode(s, t));
    }

    public static void main(String[] args) {
        ValidAnagram sol = new ValidAnagram();

        // Examples
        report("Example 1 ->", "anagram", "nagaram", sol); // true
        report("Example 2 ->", "rat", "car", sol); // false

        // Edge cases
        report("Different lengths ->", "ab", "abc", sol); // false
        report("Identical strings ->", "abc", "abc", sol); // true
        report("Single char equal ->", "a", "a", sol); // true
        report("Single char diff  ->", "a", "b", sol); // false
        report("Repeated letters  ->", "aabbcc", "abcabc", sol); // true
        report("Same set diff cnt ->", "aabb", "abbb", sol); // false (counts differ)
        report("Reversed          ->", "abcd", "dcba", sol); // true
    }
}
