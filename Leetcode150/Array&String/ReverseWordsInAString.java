/*
 * ============================================================================
 * LeetCode 151. Reverse Words in a String                   [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an input string s, reverse the ORDER of the words.
 *
 * A word is defined as a sequence of non-space characters. The words in s will
 * be separated by at least one space.
 *
 * Return a string of the words in REVERSE ORDER concatenated by a SINGLE space.
 *
 * Note that s may contain leading or trailing spaces or multiple spaces between
 * two words. The returned string should only have a single space separating the
 * words. Do NOT include any extra spaces.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length <= 10^4
 *   s contains English letters (upper/lower-case), digits, and spaces ' '.
 *   There is at least one word in s.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "the sky is blue"
 *   Output: "blue is sky the"
 *
 * Example 2:
 *   Input:  s = "  hello world  "
 *   Output: "world hello"
 *   Explanation: The reversed string should not contain leading or trailing
 *                spaces.
 *
 * Example 3:
 *   Input:  s = "a good   example"
 *   Output: "example good a"
 *   Explanation: You need to reduce multiple spaces between two words to a
 *                single space in the reversed string.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. I reverse the ORDER of words, not the characters within each word?
 *      -> Correct. "the sky" -> "sky the", each word left intact.
 *
 *  Q2. Output must collapse leading/trailing/multiple spaces to single spaces?
 *      -> Yes. Exactly one space between words, none at the ends.
 *
 *  Q3. Words can contain digits too (not only letters)?
 *      -> Yes, any non-space run is a word.
 *
 *  Q4. At least one word guaranteed, so output is never empty?
 *      -> Yes; the result always has >= 1 word.
 *
 *  Q5. Is the follow-up an in-place O(1) space solution (like a C char[])?
 *      -> In Java, String is immutable so true in-place is moot; I'll mention
 *         the char[] reverse-all-then-reverse-each-word trick that C/C++ uses.
 *
 *  Q6. Should tabs/newlines count as separators?
 *      -> Per constraints only ' ' appears, so I treat space as the only
 *         separator (would generalize to isWhitespace if asked).
 *
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.Deque;

public class ReverseWordsInAString {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE / LIBRARY (trim + split + reverse + join)
     * ------------------------------------------------------------------------
     * Idea:
     *   Trim ends, split on runs of whitespace, reverse the token array, join
     *   with single spaces. Shortest to write; leans on the standard library.
     *
     * Time  : O(n)   -- trim/split/join each scan the string
     * Space : O(n)   -- the token array + the output builder
     */
    public String reverseWordsBruteForce(String s) {
        String[] words = s.trim().split("\\s+");   // "\\s+" collapses multiple spaces
        StringBuilder sb = new StringBuilder();
        for (int i = words.length - 1; i >= 0; i--) {
            sb.append(words[i]);
            if (i > 0) sb.append(' ');
        }
        return sb.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL-ish (manual scan from the END, no split/regex)
     * ------------------------------------------------------------------------
     * Idea:
     *   Walk the string from the END. Skip spaces, then mark the end of a word,
     *   walk left to its start, and append that slice to the output. Repeat.
     *   This handles all the spacing rules without regex or an intermediate
     *   token array, and emits words already in reversed order.
     *
     * Time  : O(n)   -- single backward pass
     * Space : O(n)   -- only the output builder (unavoidable for the result)
     *
     * This is the version I'd write in an interview: explicit, no hidden costs.
     */
    public String reverseWords(String s) {
        StringBuilder sb = new StringBuilder();
        int i = s.length() - 1;
        while (i >= 0) {
            // Skip trailing/intermediate spaces.
            while (i >= 0 && s.charAt(i) == ' ') i--;
            if (i < 0) break;                       // consumed the rest; done
            // [wordStart+1 .. i] is the current word (scanning right to left).
            int wordEnd = i;
            while (i >= 0 && s.charAt(i) != ' ') i--;
            int wordStart = i + 1;
            if (sb.length() > 0) sb.append(' ');    // separator before all but the first
            sb.append(s, wordStart, wordEnd + 1);   // append the word slice
        }
        return sb.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — DEQUE (front-push each word to reverse order)
     * ------------------------------------------------------------------------
     * Idea:
     *   Scan forward, building each word; when a word completes, push it to the
     *   FRONT of a deque. Because later words are pushed in front, joining the
     *   deque yields reversed order. A clean, readable alternative.
     *
     * Time  : O(n)
     * Space : O(n)   -- the deque of words + output
     */
    public String reverseWordsDeque(String s) {
        Deque<String> deque = new ArrayDeque<>();
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == ' ') {
                if (word.length() > 0) {            // a word just finished
                    deque.addFirst(word.toString());
                    word.setLength(0);
                }
            } else {
                word.append(ch);
            }
        }
        if (word.length() > 0) deque.addFirst(word.toString());  // flush last word
        return String.join(" ", deque);
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Do it IN-PLACE with O(1) extra space (C/C++ char array)."
     *      -> Two-step trick: (1) reverse the ENTIRE array, (2) reverse each
     *         individual word back to normal. Then compact spaces in place with
     *         a read/write pointer. Java's immutable String blocks true in-place,
     *         but on a char[] this is the canonical O(1)-space method.
     *
     *  F2. "Why scan from the end instead of split()?"
     *      -> Avoids regex overhead and an intermediate array; gives explicit
     *         control over the spacing rules and emits words already reversed.
     *
     *  F3. "Reverse the CHARACTERS in each word but keep word order." (LC557)
     *      -> Opposite transform: keep order, reverse each word's characters.
     *         One pass per word.
     *
     *  F4. "Separators other than spaces (tabs, punctuation)?"
     *      -> Swap the ' ' test for Character.isWhitespace or a tokenizer with a
     *         custom delimiter set; the reversal logic is unchanged.
     *
     *  F5. "Preserve the ORIGINAL spacing when reversing word order?"
     *      -> Then you can't just collapse spaces; you'd track gap widths and
     *         reassemble, or reverse tokens including whitespace tokens.
     *
     *  F6. "Streaming input, can't hold the whole string?"
     *      -> Reversing word ORDER inherently needs the last words first, so you
     *         must buffer (a stack of words). True O(1) memory isn't possible
     *         without random access or a second reversed pass.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, ReverseWordsInAString sol) {
        System.out.println(label + " \"" + s + "\""
                + " -> endScan=\"" + sol.reverseWords(s) + "\""
                + ", deque=\"" + sol.reverseWordsDeque(s) + "\""
                + ", brute=\"" + sol.reverseWordsBruteForce(s) + "\"");
    }

    public static void main(String[] args) {
        ReverseWordsInAString sol = new ReverseWordsInAString();

        // Examples
        report("Example 1 ->", "the sky is blue", sol);      // "blue is sky the"
        report("Example 2 ->", "  hello world  ", sol);      // "world hello"
        report("Example 3 ->", "a good   example", sol);     // "example good a"

        // Edge cases
        report("Single word         ->", "word", sol);              // "word"
        report("Single word padded  ->", "   solo   ", sol);        // "solo"
        report("Two words           ->", "hi there", sol);          // "there hi"
        report("Words with digits   ->", "abc 123 xy9", sol);       // "xy9 123 abc"
        report("Many inner spaces   ->", "one     two", sol);       // "two one"
    }
}
