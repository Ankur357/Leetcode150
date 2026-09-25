package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 205. Isomorphic Strings                            [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given two strings s and t, determine if they are ISOMORPHIC.
 *
 * Two strings s and t are isomorphic if the characters in s can be REPLACED to
 * get t.
 *
 * All occurrences of a character must be replaced with another character while
 * PRESERVING THE ORDER of characters. No two characters may map to the same
 * character, but a character may map to itself. (I.e. the mapping must be a
 * ONE-TO-ONE, bijective correspondence between the characters actually used.)
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length <= 5 * 10^4
 *   t.length == s.length
 *   s and t consist of any valid ASCII characters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "egg", t = "add"
 *   Output: true
 *   Explanation: 'e' -> 'a', 'g' -> 'd'. Consistent and one-to-one.
 *
 * Example 2:
 *   Input:  s = "foo", t = "bar"
 *   Output: false
 *   Explanation: 'o' would need to map to both 'a' and 'r' -> not a function.
 *
 * Example 3:
 *   Input:  s = "paper", t = "title"
 *   Output: true
 *   Explanation: p->t, a->i, e->l, r->e. Consistent and one-to-one.
 *
 * A tricky one:
 *   s = "badc", t = "baba" -> false
 *   'd' and 'c' would both map to 'b'/'a' inconsistently, and two source chars
 *   would collide onto the same target. Requires checking BOTH directions.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Must the mapping be ONE-TO-ONE (bijective), or just a function s->t?
 *      -> One-to-one: no two distinct chars of s may map to the same char of t.
 *         This means I must check BOTH s->t and t->s consistency.
 *
 *  Q2. Can a character map to ITSELF?
 *      -> Yes ("a"->"a" is fine). Only cross-collisions are forbidden.
 *
 *  Q3. Are s and t guaranteed the same length?
 *      -> Yes per constraints. (I'll still guard with a length check.)
 *
 *  Q4. Character set — ASCII or Unicode?
 *      -> Any valid ASCII. An int[256] pair covers it; a HashMap generalizes.
 *
 *  Q5. Is this about counts or about POSITIONAL correspondence?
 *      -> Positional: s[i] must consistently correspond to t[i] at every index,
 *         preserving order. It's a per-position mapping check, not a frequency
 *         check.
 *
 *  Q6. Return type?
 *      -> boolean: true iff isomorphic.
 *
 * ============================================================================
 */

import java.util.HashMap;
import java.util.Map;

public class IsomorphicStrings {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — TWO HASH MAPS (forward + backward mapping) [clear baseline]
     * ------------------------------------------------------------------------
     * Idea:
     * Maintain two maps: mapST (s-char -> t-char) and mapTS (t-char -> s-char).
     * For each index i:
     * - If s[i] already maps to something, it must equal t[i]; else fail.
     * - If t[i] already maps to something, it must equal s[i]; else fail.
     * - Otherwise record both directions.
     * The backward map is what enforces one-to-one (no two s-chars share a
     * t-char).
     *
     * Time : O(n)
     * Space : O(k) -- k distinct characters
     */
    public boolean isIsomorphicTwoMaps(String s, String t) {
        if (s.length() != t.length())
            return false;

        Map<Character, Character> mapST = new HashMap<>();
        Map<Character, Character> mapTS = new HashMap<>();

        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i), b = t.charAt(i);

            if (mapST.containsKey(a)) {
                if (mapST.get(a) != b)
                    return false; // a maps to a different char
            } else {
                mapST.put(a, b);
            }

            if (mapTS.containsKey(b)) {
                if (mapTS.get(b) != a)
                    return false; // b already claimed by another a
            } else {
                mapTS.put(b, a);
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: LAST-SEEN-INDEX arrays (int[256] x2) <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     * Instead of storing the mapping, store the LAST INDEX (+1, so 0 means
     * "unseen") at which each character appeared, one array for s and one for
     * t. Two chars correspond consistently iff, at every index, their last-seen
     * positions match. If s[i] and t[i] were last seen at DIFFERENT positions,
     * the correspondence is broken -> not isomorphic.
     *
     * Using i+1 avoids the ambiguity of index 0 vs "never seen".
     *
     * Time : O(n)
     * Space : O(1) -- two fixed 256-slot arrays
     */
    public boolean isIsomorphic(String s, String t) {
        if (s.length() != t.length())
            return false;

        int[] lastS = new int[256];
        int[] lastT = new int[256];

        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i), b = t.charAt(i);
            // If their most recent occurrences differ, the pairing is inconsistent.
            if (lastS[a] != lastT[b])
                return false;
            lastS[a] = i + 1; // store i+1 so 0 stays the "unseen" sentinel
            lastT[b] = i + 1;
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — SINGLE FORWARD MAP + target "used" set (variant)
     * ------------------------------------------------------------------------
     * Idea:
     * Keep one map s->t plus a set of already-claimed target chars. When
     * introducing a NEW mapping, reject if the target is already claimed by a
     * different source. Equivalent to the two-map idea, phrased with a set.
     *
     * Time : O(n)
     * Space : O(k)
     */
    public boolean isIsomorphicMapAndSet(String s, String t) {
        if (s.length() != t.length())
            return false;

        Map<Character, Character> mapST = new HashMap<>();
        boolean[] usedTarget = new boolean[256];

        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i), b = t.charAt(i);
            if (mapST.containsKey(a)) {
                if (mapST.get(a) != b)
                    return false;
            } else {
                if (usedTarget[b])
                    return false; // b already claimed by another source
                mapST.put(a, b);
                usedTarget[b] = true;
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why isn't ONE map enough?"
     * -> A single s->t map only guarantees each s-char maps consistently,
     * but two different s-chars could still map to the SAME t-char
     * (e.g. "ab" -> "aa"). The reverse check (second map / used-set /
     * last-index array) enforces the one-to-one requirement.
     *
     * F2. "Explain the last-seen-index trick."
     * -> Two positions correspond iff s[i] and t[i] have identical 'history':
     * the last index each was seen. If they diverge, one repeated while
     * the other didn't, so no bijection can hold. Storing i+1 keeps 0 as
     * a clean 'never seen' marker.
     *
     * F3. "How does this relate to LeetCode 290, Word Pattern?"
     * -> Same bijection check, but mapping single chars of a pattern to
     * whole WORDS. Identical two-map logic, tokens instead of chars.
     *
     * F4. "Unicode instead of ASCII?"
     * -> Use HashMaps (Approach 1) or size the arrays to the code-point
     * range; iterate by code points for surrogate pairs.
     *
     * F5. "Is isomorphism symmetric? Does isIsomorphic(s,t) == isIsomorphic(t,s)?"
     * -> Yes — a bijection has an inverse, so the relation is symmetric.
     * The two-directional check makes this explicit.
     *
     * F6. "Could you do it with just character-frequency signatures?"
     * -> No — frequencies ignore ORDER/position. 'aab' and 'aba' need
     * positional correspondence; counting alone would misjudge them.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, String t, IsomorphicStrings sol) {
        System.out.println(label + " s=\"" + s + "\", t=\"" + t + "\""
                + " -> lastIndex=" + sol.isIsomorphic(s, t)
                + ", twoMaps=" + sol.isIsomorphicTwoMaps(s, t)
                + ", mapAndSet=" + sol.isIsomorphicMapAndSet(s, t));
    }

    public static void main(String[] args) {
        IsomorphicStrings sol = new IsomorphicStrings();

        // Examples
        report("Example 1 ->", "egg", "add", sol); // true
        report("Example 2 ->", "foo", "bar", sol); // false
        report("Example 3 ->", "paper", "title", sol); // true

        // Edge cases
        report("Two src -> one tgt ->", "ab", "aa", sol); // false (one-to-one violated)
        report("Map to self       ->", "abc", "abc", sol); // true
        report("Single char       ->", "a", "z", sol); // true
        report("Reverse collision ->", "badc", "baba", sol); // false
        report("All same s        ->", "aaa", "abc", sol); // false (a can't map to 3 targets)
        report("All same t        ->", "abc", "aaa", sol); // false (3 sources collide)
        report("Long consistent   ->", "abcabc", "xyzxyz", sol); // true
    }
}
