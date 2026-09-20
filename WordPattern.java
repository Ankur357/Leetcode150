/*
 * ============================================================================
 * LeetCode 290. Word Pattern                                   [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given a pattern and a string s, find if s FOLLOWS the same pattern.
 *
 * Here "follow" means a full match, such that there is a BIJECTION between a
 * letter in pattern and a non-empty WORD in s. Specifically:
 *   - Each letter in pattern maps to exactly one unique word in s, and
 *   - Each unique word in s maps to exactly one letter in pattern.
 *   - No two letters map to the same word, and no two words map to the same
 *     letter.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= pattern.length <= 300
 *   pattern contains only lowercase English letters.
 *   1 <= s.length <= 3000
 *   s contains only lowercase English letters and spaces ' '.
 *   s does NOT contain any leading or trailing spaces.
 *   All the words in s are separated by a SINGLE space.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  pattern = "abba", s = "dog cat cat dog"
 *   Output: true
 *   Explanation: a->dog, b->cat. Consistent and bijective.
 *
 * Example 2:
 *   Input:  pattern = "abba", s = "dog cat cat fish"
 *   Output: false
 *   Explanation: a would map to both "dog" and "fish".
 *
 * Example 3:
 *   Input:  pattern = "aaaa", s = "dog cat cat dog"
 *   Output: false
 *   Explanation: 'a' can't map to more than one word.
 *
 * A tricky one (reverse collision):
 *   pattern = "abba", s = "dog dog dog dog" -> false
 *   'a'->dog and 'b'->dog would map two letters to the SAME word.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Is the mapping a BIJECTION (both directions unique)?
 *      -> Yes. Just like Isomorphic Strings, I must check letter->word AND
 *         word->letter. One direction alone is insufficient.
 *
 *  Q2. What if the counts differ — pattern length vs. number of words?
 *      -> If pattern has a different number of characters than s has words,
 *         it's an immediate false. This is the most common oversight.
 *
 *  Q3. Word delimiter and whitespace rules?
 *      -> Single spaces, no leading/trailing space. So split on " " is safe.
 *
 *  Q4. Can a letter map to a word equal to that letter (e.g. 'a'->"a")?
 *      -> Sure, that's allowed; only cross-collisions are forbidden.
 *
 *  Q5. Are words case-sensitive?
 *      -> Lowercase only per constraints, so no case-folding needed. Word
 *         comparison is exact string equality.
 *
 *  Q6. Return type?
 *      -> boolean: true iff s follows the pattern bijectively.
 *
 * ============================================================================
 */

import java.util.HashMap;
import java.util.Map;

public class WordPattern {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — TWO HASH MAPS (letter->word AND word->letter)  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Split s into words. If the word count != pattern length, return false.
     *   Walk both in lockstep. Maintain:
     *     - charToWord: pattern char -> word
     *     - wordToChar: word -> pattern char
     *   At each index i with char c and word w:
     *     - If c already mapped, it must map to w; else fail.
     *     - If w already mapped, it must map to c; else fail.
     *     - Otherwise record both.
     *   The two directions together enforce the BIJECTION.
     *
     * Time  : O(n)  -- n = pattern length (= word count); word compares O(wordLen)
     * Space : O(n)
     */
    public boolean wordPattern(String pattern, String s) {
        String[] words = s.split(" ");
        if (pattern.length() != words.length) return false;   // count mismatch -> false

        Map<Character, String> charToWord = new HashMap<>();
        Map<String, Character> wordToChar = new HashMap<>();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            String w = words[i];

            if (charToWord.containsKey(c)) {
                if (!charToWord.get(c).equals(w)) return false;    // c bound to a different word
            } else {
                charToWord.put(c, w);
            }

            if (wordToChar.containsKey(w)) {
                if (wordToChar.get(w) != c) return false;          // w already claimed by another char
            } else {
                wordToChar.put(w, c);
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — SINGLE MAP via "last seen index" (one map, both directions)
     * ------------------------------------------------------------------------
     * Idea:
     *   A neat trick that collapses both maps into one keyed by BOTH the char
     *   and the word. Store, for each key, the last index at which it was seen.
     *   At index i, the char's last-seen index and the word's last-seen index
     *   must be equal (both unseen, or both last seen at the same position). If
     *   they differ, the pairing is inconsistent.
     *
     *   We use Integer objects and require reference-safe comparison via equals
     *   (use a single map<Object,Integer> with String keys for chars too).
     *
     * Time  : O(n)
     * Space : O(n)
     */
    public boolean wordPatternSingleMap(String pattern, String s) {
        String[] words = s.split(" ");
        if (pattern.length() != words.length) return false;

        Map<Object, Integer> lastSeen = new HashMap<>();
        for (int i = 0; i < pattern.length(); i++) {
            // Key the char as a Character and the word as a String; they never
            // collide because they are different types in the map.
            Character c = pattern.charAt(i);
            String w = words[i];
            Integer prevC = lastSeen.put(c, i);
            Integer prevW = lastSeen.put(w, i);
            // Both must have identical history (both null, or equal indices).
            if (prevC == null ? prevW != null
                              : !prevC.equals(prevW)) {
                return false;
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why must you check the word-count vs. pattern-length first?"
     *      -> Without it, "abc" vs "dog dog" (2 words) could pass a partial loop
     *         or index out of bounds. Equal counts is a precondition for any
     *         bijection between positions.
     *
     *  F2. "Why isn't one map (char->word) enough?"
     *      -> Two different letters could map to the same word (pattern="ab",
     *         s="dog dog"). The reverse map (word->char) rejects that. Same
     *         bijection subtlety as Isomorphic Strings (LC 205).
     *
     *  F3. "Relationship to LeetCode 205 (Isomorphic Strings)?"
     *      -> Identical algorithm; here the 'right side' tokens are WORDS instead
     *         of single characters. Swap char comparison for String.equals.
     *
     *  F4. "What if words could be separated by multiple/irregular spaces?"
     *      -> Use split("\\s+") and trim, or a manual tokenizer; the mapping
     *         logic is unchanged once you have the word list.
     *
     *  F5. "Reverse problem: given the mapping, generate a matching s?"
     *      -> Assign each distinct pattern letter a distinct word, then emit the
     *         word per letter in order — trivially constructs a valid s.
     *
     *  F6. "Huge s — avoid building the full String[] from split?"
     *      -> Tokenize lazily (e.g. indexOf(' ') scanning) and compare words by
     *         substring ranges; avoids allocating the whole array at once.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String pattern, String s, WordPattern sol) {
        System.out.println(label + " pattern=\"" + pattern + "\", s=\"" + s + "\""
                + " -> twoMaps=" + sol.wordPattern(pattern, s)
                + ", singleMap=" + sol.wordPatternSingleMap(pattern, s));
    }

    public static void main(String[] args) {
        WordPattern sol = new WordPattern();

        // Examples
        report("Example 1 ->", "abba", "dog cat cat dog", sol);   // true
        report("Example 2 ->", "abba", "dog cat cat fish", sol);  // false
        report("Example 3 ->", "aaaa", "dog cat cat dog", sol);   // false

        // Edge cases
        report("Reverse collision ->", "abba", "dog dog dog dog", sol); // false (a & b -> dog)
        report("Count mismatch    ->", "abc", "dog cat", sol);          // false (3 vs 2)
        report("Single pair       ->", "a", "dog", sol);                // true
        report("Map to self-like  ->", "ab", "a b", sol);               // true
        report("All distinct      ->", "abc", "dog cat fish", sol);     // true
        report("Extra word        ->", "ab", "dog cat fish", sol);      // false (2 vs 3)
        report("Long consistent   ->", "abab", "dog cat dog cat", sol); // true
    }
}
