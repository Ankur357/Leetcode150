/*
 * ============================================================================
 * LeetCode 125. Valid Palindrome                              [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * A phrase is a PALINDROME if, after converting all uppercase letters into
 * lowercase letters and removing all non-alphanumeric characters, it reads the
 * same forward and backward. Alphanumeric characters include letters and
 * numbers.
 *
 * Given a string s, return true if it is a palindrome, or false otherwise.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length <= 2 * 10^5
 *   s consists only of printable ASCII characters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "A man, a plan, a canal: Panama"
 *   Output: true
 *   Explanation: "amanaplanacanalpanama" is a palindrome.
 *
 * Example 2:
 *   Input:  s = "race a car"
 *   Output: false
 *   Explanation: "raceacar" is not a palindrome.
 *
 * Example 3:
 *   Input:  s = " "
 *   Output: true
 *   Explanation: After removing non-alphanumeric characters, s becomes an empty
 *                string "". Since an empty string reads the same forward and
 *                backward, it is a palindrome.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. "Alphanumeric" means letters AND digits are both kept?
 *      -> Yes. Everything else (spaces, punctuation, symbols) is ignored.
 *
 *  Q2. Comparison is CASE-INSENSITIVE?
 *      -> Yes, fold uppercase to lowercase before comparing.
 *
 *  Q3. Is an EMPTY (or all-symbols) string a palindrome?
 *      -> Yes (Example 3). An empty filtered string reads the same both ways.
 *
 *  Q4. ASCII only, so I can use Character.isLetterOrDigit / toLowerCase safely?
 *      -> Yes, printable ASCII. No Unicode surrogate complications here.
 *
 *  Q5. Should I build a cleaned string, or compare in place?
 *      -> Both work; the two-pointer in-place scan is O(1) extra space and is
 *         the preferred answer. I'll show the filter-then-check version too.
 *
 *  Q6. Do digits participate (e.g. "0P" -> "0p", not a palindrome)?
 *      -> Yes, digits count as characters to match. "0P" filters to "0p" which
 *         is NOT a palindrome ('0' != 'p').
 *
 * ============================================================================
 */

public class ValidPalindrome {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (filter + build cleaned string, then reverse)
     * ------------------------------------------------------------------------
     * Idea:
     *   Build a new string of only lowercase alphanumeric characters, then
     *   compare it to its reverse (or two-pointer over the clean string).
     *
     * Time  : O(n)   -- one pass to filter, one to compare/reverse
     * Space : O(n)   -- the cleaned string / StringBuilder
     *
     * Simplest to read; the extra O(n) buffer is what the optimal removes.
     */
    public boolean isPalindromeBruteForce(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        String cleaned = sb.toString();
        String reversed = sb.reverse().toString();   // sb is now reversed
        return cleaned.equals(reversed);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (two pointers, skip non-alphanumeric in place)
     * ------------------------------------------------------------------------
     * Idea:
     *   Pointers left/right converge from both ends. Skip any non-alphanumeric
     *   character on either side, then compare the two "real" characters
     *   case-insensitively. Any mismatch -> not a palindrome. If the pointers
     *   cross without a mismatch, it IS a palindrome.
     *
     *   No cleaned copy is built, so extra space is O(1).
     *
     * Time  : O(n)   -- each character is visited at most once
     * Space : O(1)   -- two indices only
     */
    public boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left < right) {
            // Advance past non-alphanumeric characters from the left.
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }
            // Advance past non-alphanumeric characters from the right.
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }
            // Compare the two meaningful characters, case-folded.
            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;   // pointers met/crossed with no mismatch
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Allow deleting AT MOST ONE character and still be a palindrome."
     *      -> LeetCode 680: on the first mismatch, try skipping EITHER the left
     *         or the right char and check if the remaining substring is a
     *         palindrome. O(n) time, O(1) space.
     *
     *  F2. "Allow up to K deletions."
     *      -> Reduces to: is (n - longestPalindromicSubsequence) <= K? Compute
     *         LPS via DP (LCS of s and reverse(s)). O(n^2) time/space.
     *
     *  F3. "Why is the two-pointer approach better than building a clean string?"
     *      -> Same O(n) time but O(1) extra space, and it can EARLY-EXIT on the
     *         first mismatch without processing the whole string.
     *
     *  F4. "Unicode input (not just ASCII)?"
     *      -> Iterate by code points (codePointAt / offsetByCodePoints), use
     *         Character.isLetterOrDigit on the code point, and be careful with
     *         locale-sensitive case folding (toLowerCase(Locale)).
     *
     *  F5. "Count how many characters would need changing to make it a palindrome."
     *      -> Two-pointer, increment a counter on each mismatched pair. Each
     *         mismatch needs one change. O(n) time, O(1) space.
     *
     *  F6. "Validate a palindrome for a LINKED LIST or a stream."
     *      -> Linked list (LC234): find middle, reverse second half, compare.
     *         Stream: buffer or use a reversible structure; you need both ends,
     *         so pure one-pass O(1) isn't possible without random access.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, ValidPalindrome sol) {
        System.out.println(label + " \"" + s + "\""
                + " -> twoPtr=" + sol.isPalindrome(s)
                + ", brute=" + sol.isPalindromeBruteForce(s));
    }

    public static void main(String[] args) {
        ValidPalindrome sol = new ValidPalindrome();

        // Examples
        report("Example 1 ->", "A man, a plan, a canal: Panama", sol); // true
        report("Example 2 ->", "race a car", sol);                     // false
        report("Example 3 ->", " ", sol);                              // true

        // Edge cases
        report("Empty-ish symbols ->", ".,", sol);          // true (filters to "")
        report("Single char       ->", "z", sol);           // true
        report("Digit palindrome  ->", "1a2", sol);         // false ('1' vs '2')
        report("Alnum mix pal     ->", "ab_a", sol);        // true ("aba")
        report("Case + digits     ->", "0P", sol);          // false ("0p")
        report("Long true         ->", "Was it a car or a cat I saw?", sol); // true
    }
}
