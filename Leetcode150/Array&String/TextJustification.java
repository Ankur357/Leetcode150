/*
 * ============================================================================
 * LeetCode 68. Text Justification                             [Difficulty: Hard]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an array of strings words and a width maxWidth, format the text such
 * that each line has EXACTLY maxWidth characters and is fully (left AND right)
 * justified.
 *
 * You should pack your words in a greedy approach; that is, pack as many words
 * as you can in each line. Pad extra spaces ' ' when necessary so that each
 * line has exactly maxWidth characters.
 *
 * Extra spaces between words should be distributed as EVENLY as possible. If
 * the number of spaces on a line does not divide evenly between words, the
 * empty slots on the LEFT will be assigned more spaces than the slots on the
 * right.
 *
 * For the LAST line of text, it should be LEFT-justified, and no extra space is
 * inserted between words.
 *
 * Note:
 *   - A word is defined as a character sequence consisting of non-space
 *     characters only.
 *   - Each word's length is guaranteed to be greater than 0 and not exceed
 *     maxWidth.
 *   - The input array words contains at least one word.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= words.length <= 300
 *   1 <= words[i].length <= 20
 *   words[i] consists of only English letters and symbols.
 *   1 <= maxWidth <= 100
 *   words[i].length <= maxWidth
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  words = ["This","is","an","example","of","text","justification."]
 *           maxWidth = 16
 *   Output:
 *     [
 *       "This    is    an",
 *       "example  of text",
 *       "justification.  "
 *     ]
 *   Explanation: The last line is left-justified.
 *
 * Example 2:
 *   Input:  words = ["What","must","be","acknowledgment","shall","be"]
 *           maxWidth = 16
 *   Output:
 *     [
 *       "What   must   be",
 *       "acknowledgment  ",
 *       "shall be        "
 *     ]
 *   Explanation: The second line has only one word "acknowledgment" -> it is
 *                left-justified (a single word can't be spread apart). The last
 *                line "shall be" is left-justified.
 *
 * Example 3:
 *   Input:  words = ["Science","is","what","we","understand","well","enough",
 *                    "to","explain","to","a","computer.","Art","is","everything",
 *                    "else","we","do"]
 *           maxWidth = 20
 *   Output:
 *     [
 *       "Science  is  what we",
 *       "understand      well",
 *       "enough to explain to",
 *       "a  computer.  Art is",
 *       "everything  else  we",
 *       "do                  "
 *     ]
 *   (Each line is exactly 20 chars; the driver prints them quoted to verify.)
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Packing is GREEDY — fit as many words per line as possible?
 *      -> Yes. A word joins the current line if it (plus one space per gap)
 *         still fits within maxWidth.
 *
 *  Q2. How exactly are extra spaces distributed on a fully-justified line?
 *      -> Evenly across the gaps; leftover spaces go to the LEFTMOST gaps first
 *         (left gaps get one extra until the remainder is exhausted).
 *
 *  Q3. Two SPECIAL cases that are LEFT-justified instead of fully justified?
 *      -> (a) the LAST line, and (b) any line with a SINGLE word (no gaps to
 *         distribute). Both: single spaces between words, pad the RIGHT.
 *
 *  Q4. Each line must be EXACTLY maxWidth characters, always?
 *      -> Yes, including trailing padding on left-justified lines.
 *
 *  Q5. Is a single word guaranteed to fit (word length <= maxWidth)?
 *      -> Yes, per constraints, so a word never overflows a line by itself.
 *
 *  Q6. words is non-empty and every word length > 0?
 *      -> Yes; no empty-input or empty-word edge cases.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

public class TextJustification {

    /*
     * ------------------------------------------------------------------------
     * APPROACH — GREEDY LINE PACKING + PER-LINE JUSTIFY
     * ------------------------------------------------------------------------
     * (This problem is about careful case handling, not algorithmic cleverness,
     *  so there isn't a meaningfully different "brute force vs optimal" split —
     *  the greedy pack IS the intended solution. I include the natural naive
     *  variation as a comment in the follow-ups.)
     *
     * Algorithm:
     *   1. GREEDY PACK: scan words, grouping as many as fit on the current line.
     *      A line of k words needs at least (sum of word lengths) + (k - 1)
     *      minimum spaces. Add the next word while that stays <= maxWidth.
     *   2. JUSTIFY each completed line:
     *      - If it's the LAST line OR has a SINGLE word -> LEFT justify:
     *        join with single spaces, pad the remainder on the right.
     *      - Otherwise FULL justify: distribute (maxWidth - totalChars) spaces
     *        across the (k - 1) gaps as evenly as possible; the first
     *        (extra % gaps) gaps receive one additional space.
     *
     * Time  : O(total characters)  -- each character is placed once
     * Space : O(maxWidth) per line for the builder (plus the output list)
     */
    public List<String> fullJustify(String[] words, int maxWidth) {
        List<String> result = new ArrayList<>();
        int n = words.length;
        int i = 0;

        while (i < n) {
            // --- Step 1: greedily determine the words [i, j) on this line. ---
            int j = i;
            int lineLen = 0;   // sum of word lengths only (no spaces yet)
            // Adding words[j] requires lineLen + words[j].length + (#words so far) spaces.
            while (j < n && lineLen + words[j].length() + (j - i) <= maxWidth) {
                lineLen += words[j].length();
                j++;
            }
            int numWords = j - i;
            int lastWordIndex = j - 1;

            // --- Step 2: build the justified line. ---
            StringBuilder sb = new StringBuilder();
            boolean isLastLine = (j == n);

            if (isLastLine || numWords == 1) {
                // LEFT justify: single spaces, pad right.
                for (int k = i; k < j; k++) {
                    if (k > i) sb.append(' ');
                    sb.append(words[k]);
                }
                padRight(sb, maxWidth);
            } else {
                // FULL justify: distribute spaces across the gaps.
                int totalSpaces = maxWidth - lineLen;   // all spaces to place
                int gaps = numWords - 1;
                int base = totalSpaces / gaps;          // each gap gets at least this
                int extra = totalSpaces % gaps;         // leftmost `extra` gaps get +1
                for (int k = i; k < j; k++) {
                    sb.append(words[k]);
                    if (k < lastWordIndex) {            // no spaces after the last word
                        int spaces = base + (k - i < extra ? 1 : 0);
                        appendSpaces(sb, spaces);
                    }
                }
            }
            result.add(sb.toString());
            i = j;   // advance to the next line's first word
        }
        return result;
    }

    // Pads sb with spaces on the right until it reaches `width` characters.
    private void padRight(StringBuilder sb, int width) {
        appendSpaces(sb, width - sb.length());
    }

    // Appends `count` space characters to sb.
    private void appendSpaces(StringBuilder sb, int count) {
        for (int s = 0; s < count; s++) sb.append(' ');
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the solution)
     * ------------------------------------------------------------------------
     *  F1. "Walk me through the space distribution math."
     *      -> With `gaps = numWords - 1` and `totalSpaces` to spread:
     *         base  = totalSpaces / gaps   (floor, every gap gets this)
     *         extra = totalSpaces % gaps   (remainder)
     *         The first `extra` gaps get base+1, the rest get base. This makes
     *         the left gaps wider, matching the problem's tie-break rule.
     *
     *  F2. "Why are the last line and single-word lines special?"
     *      -> They have no interior gaps to expand (single word) or by
     *         definition are left-justified (last line), so we fall back to
     *         single spaces + right padding to still hit exactly maxWidth.
     *
     *  F3. "How do you decide how many words fit on a line?"
     *      -> A line of k words needs sum(word lengths) + (k-1) minimum spaces.
     *         Greedily add words while that total stays <= maxWidth. This greedy
     *         is optimal for the 'pack as many as possible' requirement.
     *
     *  F4. "Is greedy always optimal, or could a look-ahead pack better?"
     *      -> The problem MANDATES greedy packing ('pack as many as you can'),
     *         so we don't minimize raggedness globally. (Knuth's line-breaking
     *         algorithm does that via DP for typesetting — a different objective.)
     *
     *  F5. "What if a single word exceeded maxWidth?"
     *      -> The constraints forbid it. If allowed, you'd hyphenate/break the
     *         word or clarify the desired behavior — an important edge to raise.
     *
     *  F6. "Reduce allocations for very large inputs?"
     *      -> Reuse a single char[] buffer of size maxWidth per line, filling
     *         spaces first then overwriting word positions; avoids repeated
     *         StringBuilder growth. Same O(total chars) time.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples and prints each line quoted to reveal padding.
    // ------------------------------------------------------------------------
    private static void report(String label, String[] words, int maxWidth,
                               TextJustification sol) {
        System.out.println(label + " maxWidth=" + maxWidth);
        for (String line : sol.fullJustify(words, maxWidth)) {
            System.out.println("  |" + line + "|  (len=" + line.length() + ")");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        TextJustification sol = new TextJustification();

        // Example 1
        report("Example 1 ->",
                new String[]{"This", "is", "an", "example", "of", "text", "justification."},
                16, sol);

        // Example 2
        report("Example 2 ->",
                new String[]{"What", "must", "be", "acknowledgment", "shall", "be"},
                16, sol);

        // Example 3
        report("Example 3 ->",
                new String[]{"Science", "is", "what", "we", "understand", "well", "enough",
                        "to", "explain", "to", "a", "computer.", "Art", "is",
                        "everything", "else", "we", "do"},
                20, sol);

        // Edge cases
        report("Single word fills   ->", new String[]{"hello"}, 5, sol);         // "hello"
        report("Single short word   ->", new String[]{"a"}, 4, sol);             // "a   "
        report("All on one full line->", new String[]{"a", "b", "c"}, 5, sol);   // "a b c" (last line)
    }
}
