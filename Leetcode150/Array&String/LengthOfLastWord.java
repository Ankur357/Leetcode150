/*
 * ============================================================================
 * LeetCode 58. Length of Last Word                            [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given a string s consisting of words and spaces, return the LENGTH of the
 * LAST word in the string.
 *
 * A word is a maximal substring consisting of non-space characters only.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length <= 10^4
 *   s consists of only English letters and spaces ' '.
 *   There is at least one word in s.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "Hello World"
 *   Output: 5
 *   Explanation: The last word is "World", whose length is 5.
 *
 * Example 2:
 *   Input:  s = "   fly me   to   the moon  "
 *   Output: 4
 *   Explanation: The last word is "moon", whose length is 4. Note the trailing
 *                spaces, which must be skipped before measuring.
 *
 * Example 3:
 *   Input:  s = "luffy is still joyboy"
 *   Output: 6
 *   Explanation: The last word is "joyboy", length 6.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Can there be TRAILING spaces after the last word?
 *      -> Yes (Example 2). I must skip trailing spaces before counting.
 *
 *  Q2. Only spaces separate words, and characters are letters + ' ' only?
 *      -> Yes. No tabs, punctuation, or digits — simplifies the "is separator"
 *         check to a plain space comparison.
 *
 *  Q3. Is there guaranteed to be at least one word?
 *      -> Yes, so the answer is always >= 1; no empty-result edge case.
 *
 *  Q4. Can there be MULTIPLE spaces between words?
 *      -> Yes (Example 2). Any run of spaces is a single separator effectively.
 *
 *  Q5. Should I avoid allocating (no split into an array)?
 *      -> Not required, but scanning from the END is O(n) time and O(1) space
 *         and touches far fewer characters — the preferred answer.
 *
 *  Q6. Case sensitivity / trimming semantics matter?
 *      -> No; we only measure length, so case is irrelevant. Trailing/leading
 *         spaces are the only subtlety.
 *
 * ============================================================================
 */

public class LengthOfLastWord {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (trim + split on whitespace)
     * ------------------------------------------------------------------------
     * Idea:
     *   Trim surrounding spaces, split on one-or-more spaces, take the length
     *   of the final token. Very readable; leans on library string ops.
     *
     * Time  : O(n)   -- trim + split both scan the whole string
     * Space : O(n)   -- split allocates an array of all words
     *
     * Correct but wasteful: it processes and stores EVERY word just to read the
     * last one. Fine to state, then optimize.
     */
    public int lengthOfLastWordBruteForce(String s) {
        String[] words = s.trim().split("\\s+");   // "\\s+" collapses multiple spaces
        return words[words.length - 1].length();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (scan from the END, no allocation)
     * ------------------------------------------------------------------------
     * Idea:
     *   Walk backwards from the last character:
     *     1. Skip any TRAILING spaces.
     *     2. Then count consecutive non-space characters until we hit a space
     *        or the start of the string. That count is the last word's length.
     *   We never need to look at anything before the last word.
     *
     * Time  : O(n) worst case, but typically only touches the trailing spaces
     *         plus the last word (often far less than n).
     * Space : O(1)   -- just an index and a counter.
     */
    public int lengthOfLastWord(String s) {
        int i = s.length() - 1;

        // Step 1: skip trailing spaces.
        while (i >= 0 && s.charAt(i) == ' ') {
            i--;
        }
        // Step 2: count the last word's characters.
        int length = 0;
        while (i >= 0 && s.charAt(i) != ' ') {
            length++;
            i--;
        }
        return length;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — ONE-PASS FORWARD (running length, reset on space)
     * ------------------------------------------------------------------------
     * Idea:
     *   Scan left to right. Grow a running `length` on non-space characters;
     *   reset it to 0 when a space starts a gap. When the scan ends, `length`
     *   holds the length of whatever word finished last. Handles trailing
     *   spaces naturally because the last reset happens at the space AFTER the
     *   final word, but we only reset on the FIRST space of a run... so we
     *   track "did the previous char end a word" via the reset-on-space rule.
     *
     * Time  : O(n)   -- single forward pass
     * Space : O(1)
     *
     * Slightly less efficient in practice than scanning from the end (it must
     * read the whole string), but a clean alternative if end-scanning feels
     * awkward.
     */
    public int lengthOfLastWordForward(String s) {
        int length = 0;
        int last = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
                length = 0;                 // gap: start a fresh count
            } else {
                length++;
                last = length;              // remember the most recent word length
            }
        }
        return last;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why scan from the END rather than the front?"
     *      -> We only need the LAST word. End-scanning skips trailing spaces and
     *         reads just that word, avoiding touching the rest of the string in
     *         the common case. Forward scanning must read everything.
     *
     *  F2. "What if separators could be tabs/newlines, not just spaces?"
     *      -> Replace the `== ' '` test with Character.isWhitespace(c), or use
     *         "\\s+" in the split approach. Logic is otherwise identical.
     *
     *  F3. "Return the FIRST word's length, or the k-th from the end?"
     *      -> First word: scan from the front skipping leading spaces. k-th from
     *         end: count word boundaries from the back until the k-th completes.
     *
     *  F4. "Return the last word itself, not just its length."
     *      -> Track start and end indices during the end-scan and substring
     *         once (single allocation) instead of counting.
     *
     *  F5. "The string is a huge stream read left-to-right only."
     *      -> Use the forward one-pass (Approach 3): maintain the last completed
     *         word length as you consume characters; O(1) memory, no seek-back.
     *
     *  F6. "Unicode / combining characters — does length mean code units or
     *       grapheme clusters?"
     *      -> Clarify the definition. charAt counts UTF-16 code units; for true
     *         user-perceived characters use a grapheme iterator (e.g.
     *         BreakIterator). For this problem (ASCII letters) it's moot.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, LengthOfLastWord sol) {
        System.out.println(label + " \"" + s + "\""
                + " -> endScan=" + sol.lengthOfLastWord(s)
                + ", forward=" + sol.lengthOfLastWordForward(s)
                + ", brute=" + sol.lengthOfLastWordBruteForce(s));
    }

    public static void main(String[] args) {
        LengthOfLastWord sol = new LengthOfLastWord();

        // Examples
        report("Example 1 ->", "Hello World", sol);                 // 5
        report("Example 2 ->", "   fly me   to   the moon  ", sol); // 4
        report("Example 3 ->", "luffy is still joyboy", sol);       // 6

        // Edge cases
        report("Single word        ->", "word", sol);          // 4
        report("Single char        ->", "a", sol);             // 1
        report("Trailing spaces    ->", "hello   ", sol);      // 5
        report("Leading spaces     ->", "   hi", sol);         // 2
        report("Word then space x1 ->", "abc ", sol);          // 3
    }
}
