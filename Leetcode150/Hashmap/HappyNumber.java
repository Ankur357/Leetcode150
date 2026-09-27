package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 202. Happy Number                                   [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Write an algorithm to determine if a number n is HAPPY.
 *
 * A happy number is a number defined by the following process:
 *   - Starting with any positive integer, replace the number by the SUM OF THE
 *     SQUARES of its digits.
 *   - Repeat the process until the number equals 1 (where it will stay), or it
 *     LOOPS ENDLESSLY in a cycle that does not include 1.
 *   - Those numbers for which this process ends in 1 are HAPPY.
 *
 * Return true if n is a happy number, and false if not.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= n <= 2^31 - 1
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  n = 19
 *   Output: true
 *   Explanation:
 *     1^2 + 9^2            = 1 + 81  = 82
 *     8^2 + 2^2            = 64 + 4  = 68
 *     6^2 + 8^2            = 36 + 64 = 100
 *     1^2 + 0^2 + 0^2      = 1               -> happy!
 *
 * Example 2:
 *   Input:  n = 2
 *   Output: false
 *   Explanation: 2 -> 4 -> 16 -> 37 -> 58 -> 89 -> 145 -> 42 -> 20 -> 4 -> ...
 *                enters a cycle that never reaches 1.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. What terminates the process — 1 (happy) or a repeat (unhappy)?
 *      -> Reaching 1 means happy. Revisiting any number we've already seen means
 *         we're in a cycle that never hits 1 -> unhappy.
 *
 *  Q2. How do I KNOW the unhappy case always cycles (never grows forever)?
 *      -> The digit-square-sum of any number is bounded: for a number with d
 *         digits it's at most d * 81, which for large n shrinks the value fast.
 *         Any value quickly falls below ~243 (max for 3 digits) and thereafter
 *         stays in a finite range, so by pigeonhole it must eventually repeat.
 *         Hence the sequence either hits 1 or cycles — it can't diverge.
 *
 *  Q3. n fits in an int (up to 2^31 - 1)?
 *      -> Yes. The digit-square-sum of at most ~10 digits is <= 10*81 = 810, so
 *         no overflow after the first step. int is safe throughout.
 *
 *  Q4. Is n always positive?
 *      -> Yes, 1 <= n. n == 1 is trivially happy.
 *
 *  Q5. Return type?
 *      -> boolean: true iff n is happy.
 *
 *  Q6. Any known cycle I can exploit?
 *      -> Every unhappy number funnels into the cycle 4 -> 16 -> 37 -> 58 -> 89
 *         -> 145 -> 42 -> 20 -> 4. Detecting '4' is a shortcut (see Approach 3).
 *
 * ============================================================================
 */

import java.util.HashSet;
import java.util.Set;

public class HappyNumber {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — HASH SET cycle detection [clean baseline]
     * ------------------------------------------------------------------------
     * Idea:
     * Repeatedly replace n with the sum of the squares of its digits. Keep a
     * set of numbers we've already produced. If we reach 1 -> happy. If we
     * produce a number already in the set -> a cycle -> unhappy.
     *
     * Time : O(log n) per digit-sum step; the number of steps until 1-or-cycle
     * is bounded by a constant once values drop below ~243, so overall
     * effectively O(log n).
     * Space : O(log n) worth of distinct values in the set (bounded).
     */
    public boolean isHappySet(int n) {
        Set<Integer> seen = new HashSet<>();
        while (n != 1 && !seen.contains(n)) {
            seen.add(n);
            n = squareDigitSum(n);
        }
        return n == 1;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: FLOYD'S CYCLE DETECTION (O(1) space) <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     * Treat the transformation as a linked-list "next" pointer. Use a slow and
     * a fast runner (tortoise & hare): slow advances one step, fast two steps.
     * If there's a cycle they meet; if the sequence reaches 1, fast hits 1.
     * - If fast reaches 1 -> happy.
     * - If slow == fast (and != 1) -> a cycle not containing 1 -> unhappy.
     *
     * No extra storage needed -> O(1) space, beating the hash set.
     *
     * Time : O(log n) effective
     * Space : O(1)
     */
    public boolean isHappy(int n) {
        int slow = n;
        int fast = squareDigitSum(n);
        while (fast != 1 && slow != fast) {
            slow = squareDigitSum(slow); // one step
            fast = squareDigitSum(squareDigitSum(fast)); // two steps
        }
        return fast == 1;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — HARD-CODED CYCLE SHORTCUT (detect the '4' funnel)
     * ------------------------------------------------------------------------
     * Idea:
     * It's a known mathematical fact that EVERY unhappy number eventually
     * reaches 4 (which sits on the sole cycle 4->16->37->58->89->145->42->20->4).
     * So: iterate until n == 1 (happy) or n == 4 (unhappy). No set, no runners.
     *
     * Time : O(log n) effective
     * Space : O(1)
     *
     * Compact and fast, but relies on the memorized cycle fact — worth stating
     * to the interviewer rather than presenting as "magic".
     */
    public boolean isHappyShortcut(int n) {
        while (n != 1 && n != 4) {
            n = squareDigitSum(n);
        }
        return n == 1;
    }

    // Sum of the squares of the decimal digits of n.
    private int squareDigitSum(int n) {
        int sum = 0;
        while (n > 0) {
            int d = n % 10;
            sum += d * d;
            n /= 10;
        }
        return sum;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why must the process terminate (never diverge to infinity)?"
     * -> The digit-square-sum shrinks large numbers rapidly: any number
     * above 243 maps to something smaller, so values are trapped in a
     * finite range [1, 243]. By pigeonhole a repeat is inevitable, so it
     * either reaches 1 or cycles.
     *
     * F2. "Hash set vs. Floyd's — trade-offs?"
     * -> Same effective time. Hash set is O(log n) space but very readable;
     * Floyd's is O(1) space at the cost of computing the transform ~3x
     * per iteration. Floyd's is the 'can you do it in O(1) space?' answer.
     *
     * F3. "Why does detecting '4' work as a shortcut?"
     * -> There is exactly one cycle among unhappy numbers, and it contains
     * 4. Every unhappy sequence enters that cycle, hence passes through 4.
     * It's a proven fact for base-10 digit-square-sums.
     *
     * F4. "What about a different power (sum of CUBES of digits) or base?"
     * -> The cycle structure changes; the '4' shortcut no longer applies.
     * Fall back to hash-set or Floyd's, which work for ANY such map.
     *
     * F5. "Could you precompute digit-square sums for 0-9 to speed it up?"
     * -> Yes, a tiny lookup table squares[d] avoids repeated multiplication;
     * negligible for this range but a valid micro-optimization.
     *
     * F6. "n given as a very large number (beyond int/long)?"
     * -> Only the FIRST digit-square-sum depends on the full magnitude;
     * after one step the value is small. So take the digit-square-sum of
     * the big number (as a string/BigInteger) once, then proceed in int.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int n, HappyNumber sol) {
        System.out.println(label + " n=" + n
                + " -> floyd=" + sol.isHappy(n)
                + ", set=" + sol.isHappySet(n)
                + ", shortcut=" + sol.isHappyShortcut(n));
    }

    public static void main(String[] args) {
        HappyNumber sol = new HappyNumber();

        // Examples
        report("Example 1 ->", 19, sol); // true
        report("Example 2 ->", 2, sol); // false

        // Edge cases
        report("n == 1 (happy)   ->", 1, sol); // true
        report("n == 7 (happy)   ->", 7, sol); // true (7 -> 49 -> 97 -> 130 -> 10 -> 1)
        report("n == 4 (cycle)   ->", 4, sol); // false (on the cycle itself)
        report("n == 100 (happy) ->", 100, sol); // true
        report("Big happy 10^9-ish->", 1000000000, sol); // 1 -> true
        report("Unhappy 116      ->", 116, sol); // false
        report("Happy 23         ->", 23, sol); // true (23 -> 13 -> 10 -> 1)
    }
}
