package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 383. Ransom Note                                    [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given two strings ransomNote and magazine, return true if ransomNote can be
 * constructed by using the letters from magazine and false otherwise.
 *
 * Each letter in magazine can only be used ONCE in ransomNote.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= ransomNote.length, magazine.length <= 10^5
 *   ransomNote and magazine consist of LOWERCASE English letters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  ransomNote = "a", magazine = "b"
 *   Output: false
 *
 * Example 2:
 *   Input:  ransomNote = "aa", magazine = "ab"
 *   Output: false
 *   Explanation: magazine has only one 'a', but the note needs two.
 *
 * Example 3:
 *   Input:  ransomNote = "aa", magazine = "aab"
 *   Output: true
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Each magazine letter usable only ONCE?
 *      -> Yes. So it's a MULTISET containment check: for every letter, the note
 *         needs no more copies than the magazine provides.
 *
 *  Q2. Character set — lowercase English only?
 *      -> Yes per constraints. A fixed int[26] count array suffices (no hashing
 *         needed), though a HashMap generalizes to arbitrary charsets.
 *
 *  Q3. Case-sensitive? Spaces/punctuation?
 *      -> Only lowercase letters, so no case-folding or symbol handling needed.
 *
 *  Q4. Does ORDER matter?
 *      -> No — only the COUNT of each letter matters, not arrangement.
 *
 *  Q5. Quick impossibility check?
 *      -> If ransomNote is longer than magazine, it can't possibly be built ->
 *         return false immediately.
 *
 *  Q6. Return type?
 *      -> boolean: true iff the note is constructible.
 *
 * ============================================================================
 */

import java.util.HashMap;
import java.util.Map;

public class RansomNote {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (mutate a magazine char buffer)
     * ------------------------------------------------------------------------
     * Idea:
     * Copy magazine into a mutable char[]. For each character of ransomNote,
     * scan the buffer for a matching, not-yet-consumed slot; consume it (mark
     * used). If any note char can't find a slot, return false.
     *
     * Time : O(len(note) * len(magazine)) -- linear scan per note char
     * Space : O(len(magazine)) -- the mutable buffer
     *
     * Correct but quadratic; counting collapses it to linear.
     */
    public boolean canConstructBruteForce(String ransomNote, String magazine) {
        if (ransomNote.length() > magazine.length())
            return false;
        char[] mag = magazine.toCharArray();
        boolean[] used = new boolean[mag.length];

        for (int i = 0; i < ransomNote.length(); i++) {
            char need = ransomNote.charAt(i);
            boolean found = false;
            for (int j = 0; j < mag.length; j++) {
                if (!used[j] && mag[j] == need) {
                    used[j] = true;
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (frequency count with int[26]) <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     * Count how many of each letter the magazine provides in an int[26].
     * Then walk ransomNote, decrementing the count for each needed letter.
     * If any count drops below zero, the magazine lacks enough of that
     * letter -> return false.
     *
     * Time : O(len(note) + len(magazine))
     * Space : O(1) -- fixed 26-slot array, independent of input size
     */
    public boolean canConstruct(String ransomNote, String magazine) {
        if (ransomNote.length() > magazine.length())
            return false;

        int[] counts = new int[26];
        for (int i = 0; i < magazine.length(); i++) {
            counts[magazine.charAt(i) - 'a']++;
        }
        for (int i = 0; i < ransomNote.length(); i++) {
            if (--counts[ransomNote.charAt(i) - 'a'] < 0) {
                return false; // ran out of this letter
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — HashMap variant (any Unicode charset)
     * ------------------------------------------------------------------------
     * Idea:
     * Same counting logic, but a HashMap<Character,Integer> replaces int[26]
     * so it works for arbitrary characters (Unicode, mixed case, symbols).
     *
     * Time : O(len(note) + len(magazine))
     * Space : O(k) -- k = distinct chars in magazine
     */
    public boolean canConstructMap(String ransomNote, String magazine) {
        if (ransomNote.length() > magazine.length())
            return false;

        Map<Character, Integer> counts = new HashMap<>();
        for (int i = 0; i < magazine.length(); i++) {
            counts.merge(magazine.charAt(i), 1, Integer::sum);
        }
        for (int i = 0; i < ransomNote.length(); i++) {
            char c = ransomNote.charAt(i);
            int remaining = counts.getOrDefault(c, 0);
            if (remaining == 0)
                return false;
            counts.put(c, remaining - 1);
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why int[26] instead of a HashMap?"
     * -> The input is restricted to lowercase letters, so a 26-slot array
     * gives O(1) indexing with no hashing/boxing overhead — faster and
     * constant space. The HashMap is the general-charset fallback.
     *
     * F2. "Could you decrement magazine counts while scanning the note, in one
     * pass, and short-circuit early?"
     * -> Yes (Approach 2 already short-circuits on the first shortfall).
     * You still need the full magazine count first, so it's inherently
     * two passes over magazine + one over the note.
     *
     * F3. "What if letters could be reused UNLIMITED times?"
     * -> Then only the SET of magazine letters matters: the note is
     * constructible iff every distinct note letter appears at least once
     * in magazine. Counts become irrelevant.
     *
     * F4. "Case-insensitive version?"
     * -> Lowercase both strings first (or fold case when indexing), then
     * run the same count logic.
     *
     * F5. "Streaming magazine you can only read once?"
     * -> Counting still works: accumulate the magazine counts as it streams,
     * then validate the note. If BOTH stream, you'd need to buffer counts
     * for one side.
     *
     * F6. "Return WHICH letters are short and by how much, not just true/false?"
     * -> After decrementing, collect every letter whose count went negative
     * and report the deficit (magnitude of the negative). One extra pass
     * over the 26 slots.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String note, String magazine, RansomNote sol) {
        System.out.println(label + " note=\"" + note + "\", magazine=\"" + magazine + "\""
                + " -> count=" + sol.canConstruct(note, magazine)
                + ", map=" + sol.canConstructMap(note, magazine)
                + ", brute=" + sol.canConstructBruteForce(note, magazine));
    }

    public static void main(String[] args) {
        RansomNote sol = new RansomNote();

        // Examples
        report("Example 1 ->", "a", "b", sol); // false
        report("Example 2 ->", "aa", "ab", sol); // false
        report("Example 3 ->", "aa", "aab", sol); // true

        // Edge cases
        report("Exact match      ->", "abc", "cba", sol); // true (same multiset)
        report("Note longer      ->", "abcd", "abc", sol); // false (length check)
        report("Extra magazine   ->", "abc", "aabbccdd", sol); // true
        report("Repeated letters ->", "aaa", "aaaa", sol); // true
        report("Just short       ->", "aaaa", "aaa", sol); // false
        report("Single equal     ->", "z", "z", sol); // true
        report("Disjoint letters ->", "xyz", "abc", sol); // false
    }
}
