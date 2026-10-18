/*
 * ============================================================================
 * LeetCode 30. Substring with Concatenation of All Words        [Difficulty: Hard]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given a string s and an array of strings words. All the strings in
 * words are of the SAME LENGTH.
 *
 * A concatenated substring in s is a substring that contains ALL the strings of
 * words concatenated together in ANY ORDER, exactly once each, with no
 * characters in between.
 *
 * Return the starting indices of all concatenated substrings in s. You may
 * return the answer in any order.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length <= 10^4
 *   1 <= words.length <= 5000
 *   1 <= words[i].length <= 30
 *   s and words[i] consist of lowercase English letters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "barfoothefoobarman", words = ["foo","bar"]
 *   Output: [0,9]
 *   Explanation: Word length = 3, total = 6.
 *     Index 0 -> "barfoo" = "bar"+"foo"  (a permutation of words) ✓
 *     Index 9 -> "foobar" = "foo"+"bar"  ✓
 *     The order of "foo"/"bar" does not matter.
 *
 * Example 2:
 *   Input:  s = "wordgoodgoodgoodbestword", words = ["word","good","best","word"]
 *   Output: []
 *   Explanation: We need "word" twice but only one "word" exists before the
 *                "best" region lines up — no window has the exact multiset.
 *
 * Example 3:
 *   Input:  s = "barfoofoobarthefoobarman", words = ["bar","foo","the"]
 *   Output: [6,9,12]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Are ALL words guaranteed the same length?
 *      -> Yes. This is the key structural fact — s can be scanned in fixed
 *         word-length steps, so alignment only has `wordLen` distinct phases.
 *
 *  Q2. Can words contain DUPLICATES?
 *      -> Yes (Example 2 has "word" twice). So I must match a MULTISET (counts),
 *         not just a set.
 *
 *  Q3. Must the words be contiguous with NO gaps or extra chars?
 *      -> Yes — the concatenation is exactly totalLen = words.length * wordLen
 *         characters, back to back.
 *
 *  Q4. Does the ORDER of words in the window matter?
 *      -> No — any permutation counts. Only the multiset of words matters.
 *
 *  Q5. Output order of the start indices?
 *      -> Any order is accepted.
 *
 *  Q6. Can words be longer than s (so no answer possible)?
 *      -> Possibly; guard: if totalLen > s.length(), return empty immediately.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubstringWithConcatenationOfAllWords {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check every start index)
     * ------------------------------------------------------------------------
     * Idea:
     *   Let wordLen = length of each word, k = number of words,
     *   totalLen = wordLen * k. For every start index i in [0, n - totalLen],
     *   slice the window into k chunks of wordLen, count them, and compare the
     *   counts against the required word-count map.
     *
     * Time  : O(n * k * wordLen)
     *         -- n starts, each doing k chunk extractions of length wordLen
     * Space : O(k * wordLen)  -- the count maps
     *
     * Simple and correct; the sliding window reuses work across starts to drop
     * the k factor.
     */
    public List<Integer> findSubstringBruteForce(String s, String[] words) {
        List<Integer> result = new ArrayList<>();
        int k = words.length;
        int wordLen = words[0].length();
        int totalLen = k * wordLen;
        int n = s.length();
        if (totalLen > n) return result;

        Map<String, Integer> need = new HashMap<>();
        for (String w : words) need.merge(w, 1, Integer::sum);

        for (int i = 0; i + totalLen <= n; i++) {
            Map<String, Integer> seen = new HashMap<>();
            int j = 0;
            for (; j < k; j++) {
                int start = i + j * wordLen;
                String word = s.substring(start, start + wordLen);
                if (!need.containsKey(word)) break;          // word not needed at all
                seen.merge(word, 1, Integer::sum);
                if (seen.get(word) > need.get(word)) break;  // too many of this word
            }
            if (j == k) result.add(i);                       // all k chunks matched
        }
        return result;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (sliding window over word-aligned offsets)  <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Because all words share wordLen, any valid window starts at some index
     *   congruent to one of the offsets 0, 1, ..., wordLen-1 (mod wordLen).
     *   Run a sliding window SEPARATELY for each of those `wordLen` phases.
     *
     *   Within one phase, walk `right` forward one WORD at a time:
     *     - Extract the word at `right`.
     *     - If it's a needed word, add it to the window count. While its count
     *       exceeds what's needed, shrink from the LEFT one word at a time
     *       (removing left words) until the excess is gone.
     *     - If it's NOT a needed word, reset the whole window: clear counts and
     *       move `left` to just past this word.
     *     - When the window holds exactly k words, record `left` and then slide
     *       left forward by one word to keep searching.
     *
     *   Each word position is added/removed O(1) times per phase, so across all
     *   phases every position is touched O(1) amortized.
     *
     * Time  : O(n * wordLen)   -- wordLen phases, each an O(n/wordLen)-word pass,
     *                             times O(wordLen) per substring slice ≈ O(n*wordLen)
     * Space : O(k * wordLen)
     */
    public List<Integer> findSubstring(String s, String[] words) {
        List<Integer> result = new ArrayList<>();
        int k = words.length;
        int wordLen = words[0].length();
        int totalLen = k * wordLen;
        int n = s.length();
        if (totalLen > n) return result;

        Map<String, Integer> need = new HashMap<>();
        for (String w : words) need.merge(w, 1, Integer::sum);

        // One sliding window per starting offset (mod wordLen).
        for (int offset = 0; offset < wordLen; offset++) {
            int left = offset;             // window covers whole words in [left, right)
            int count = 0;                 // number of needed words currently in window
            Map<String, Integer> window = new HashMap<>();

            for (int right = offset; right + wordLen <= n; right += wordLen) {
                String word = s.substring(right, right + wordLen);

                if (!need.containsKey(word)) {
                    // Word not in the dictionary: nothing before it can extend
                    // to a valid window, so drop everything and restart after it.
                    window.clear();
                    count = 0;
                    left = right + wordLen;
                    continue;
                }

                // Admit this word.
                window.merge(word, 1, Integer::sum);
                count++;

                // If we now have too many copies of `word`, shrink from the left
                // until the surplus of THIS word is removed.
                while (window.get(word) > need.get(word)) {
                    String leftWord = s.substring(left, left + wordLen);
                    window.merge(leftWord, -1, Integer::sum);
                    if (window.get(leftWord) == 0) window.remove(leftWord);
                    count--;
                    left += wordLen;
                }

                // Exactly k needed words -> a valid concatenation starting at left.
                if (count == k) {
                    result.add(left);
                    // Slide left by one word to look for the next window.
                    String leftWord = s.substring(left, left + wordLen);
                    window.merge(leftWord, -1, Integer::sum);
                    if (window.get(leftWord) == 0) window.remove(leftWord);
                    count--;
                    left += wordLen;
                }
            }
        }
        return result;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why only `wordLen` sliding windows instead of one per index?"
     *      -> Every valid window is word-aligned to its own start, and all
     *         words share wordLen, so all windows of a given phase share the
     *         same word boundaries. Running one window per residue class (mod
     *         wordLen) covers every possible alignment with no overlap.
     *
     *  F2. "Complexity of brute force vs. sliding window?"
     *      -> Brute force O(n*k*wordLen); sliding window O(n*wordLen) because
     *         each word position is admitted/evicted O(1) times per phase. The
     *         win grows with k (word count).
     *
     *  F3. "How are duplicate words handled?"
     *      -> A count map (multiset), not a set. The 'while window[word] >
     *         need[word]' shrink is what enforces exact multiplicities.
     *
     *  F4. "What if words had DIFFERENT lengths?"
     *      -> The word-alignment trick collapses; it becomes a much harder
     *         search (backtracking / trie over variable splits). The equal-length
     *         guarantee is load-bearing here.
     *
     *  F5. "Very large s with tiny words — can we avoid substring() allocations?"
     *      -> Hash each word to an int id (rolling hash or a trie/HashMap of the
     *         dictionary) and compare ids, or use s.regionMatches to avoid
     *         allocating substrings. Reduces GC pressure.
     *
     *  F6. "Return count of windows, or the substrings themselves?"
     *      -> Trivial adaptations: count = result.size(); substrings =
     *         s.substring(idx, idx + totalLen) for each start index found.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, String[] words,
                               SubstringWithConcatenationOfAllWords sol) {
        System.out.println(label + " s=\"" + s + "\", words="
                + java.util.Arrays.toString(words)
                + " -> optimal=" + sol.findSubstring(s, words)
                + ", brute=" + sol.findSubstringBruteForce(s, words));
    }

    public static void main(String[] args) {
        SubstringWithConcatenationOfAllWords sol =
                new SubstringWithConcatenationOfAllWords();

        // Examples
        report("Example 1 ->", "barfoothefoobarman", new String[]{"foo", "bar"}, sol);          // [0,9]
        report("Example 2 ->", "wordgoodgoodgoodbestword",
                new String[]{"word", "good", "best", "word"}, sol);                              // []
        report("Example 3 ->", "barfoofoobarthefoobarman",
                new String[]{"bar", "foo", "the"}, sol);                                         // [6,9,12]

        // Edge cases
        report("Duplicates      ->", "wordgoodgoodgoodbestword",
                new String[]{"word", "good", "best", "good"}, sol);                              // [8]
        report("Single word     ->", "aaa", new String[]{"a"}, sol);                             // [0,1,2]
        report("Full string     ->", "foobar", new String[]{"foo", "bar"}, sol);                 // [0]
        report("Words > s       ->", "foo", new String[]{"foo", "bar"}, sol);                    // []
        report("Overlap repeats ->", "aaaaaa", new String[]{"aa", "aa"}, sol);                   // [0,1,2]
    }
}
