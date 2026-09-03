/*
 * ============================================================================
 * LeetCode 169. Majority Element                               [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an array nums of size n, return the majority element.
 *
 * The majority element is the element that appears MORE THAN floor(n / 2)
 * times. You may assume that the majority element ALWAYS exists in the array.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   n == nums.length
 *   1 <= n <= 5 * 10^4
 *   -10^9 <= nums[i] <= 10^9
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [3,2,3]
 *   Output: 3
 *   Explanation: 3 appears 2 times, and 2 > floor(3/2) = 1.
 *
 * Example 2:
 *   Input:  nums = [2,2,1,1,1,2,2]
 *   Output: 2
 *   Explanation: 2 appears 4 times; floor(7/2) = 3, and 4 > 3.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Is the majority element GUARANTEED to exist?
 *      -> Yes, the prompt says so. This lets Boyer-Moore skip a verification
 *         pass. (If it were NOT guaranteed, I'd add a second pass to confirm.)
 *
 *  Q2. "Majority" means STRICTLY more than n/2, correct? (not >=)
 *      -> Yes, strictly greater than floor(n/2). So it occupies more than half
 *         the array and is therefore unique.
 *
 *  Q3. Can there be more than one majority element?
 *      -> No. By definition at most one value can exceed n/2 occurrences.
 *
 *  Q4. What's the value range / any overflow worry?
 *      -> Values fit in int. Counts fit in int (n <= 5e4). No overflow.
 *
 *  Q5. Can the array have a single element?
 *      -> Yes, n >= 1. A one-element array's sole element is the majority.
 *
 *  Q6. Are there constraints on time/space they want me to hit?
 *      -> Classic ask is O(n) time and O(1) space (Boyer-Moore voting). I'll
 *         mention the simpler O(n)-space approaches first, then optimize.
 *
 * ============================================================================
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MajorityElement {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (hash map frequency count)
     * ------------------------------------------------------------------------
     * Idea:
     *   Count how many times each value occurs in a HashMap. The first value
     *   whose count exceeds n/2 is the answer. (Simplest correct approach; also
     *   the natural solution if majority were NOT guaranteed.)
     *
     * Time  : O(n)   -- one pass to count
     * Space : O(n)   -- the frequency map (up to n distinct keys)
     *
     * Weakness: O(n) extra space. Interviewer will push for O(1).
     */
    public int majorityElementBruteForce(int[] nums) {
        Map<Integer, Integer> counts = new HashMap<>();
        int threshold = nums.length / 2;
        for (int x : nums) {
            int c = counts.merge(x, 1, Integer::sum);
            if (c > threshold) {
                return x;
            }
        }
        return -1; // unreachable given the guarantee
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — SORTING (the middle element trick)
     * ------------------------------------------------------------------------
     * Idea:
     *   If a value occupies more than half the array, then after sorting it
     *   MUST cover the middle index n/2 (no matter where its block lands).
     *   So the element at index n/2 of the sorted array is the majority.
     *
     * Time  : O(n log n)   -- dominated by the sort
     * Space : O(1) extra   -- if sorting in place (ignoring sort's stack)
     *
     * A neat one-liner to mention, but slower than linear and mutates input.
     */
    public int majorityElementSorting(int[] nums) {
        int[] copy = Arrays.copyOf(nums, nums.length); // avoid mutating caller's array
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (Boyer-Moore Voting Algorithm)
     * ------------------------------------------------------------------------
     * Idea (intuition: cancellation):
     *   Keep a `candidate` and a `count`. Walk the array:
     *     - If count == 0, adopt the current element as the new candidate.
     *     - If the current element equals the candidate, count++ (a "vote for").
     *     - Otherwise count-- (a "vote against" — one candidate vote cancels
     *       one opposing element).
     *
     *   Because the majority appears more than n/2 times, every non-majority
     *   element can cancel at most one majority vote — but there aren't enough
     *   of them to drive the majority's net count to zero. So whatever remains
     *   as the candidate at the end IS the majority element.
     *
     *   No verification pass is needed here because existence is guaranteed.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)   -- two scalar variables
     */
    public int majorityElement(int[] nums) {
        int candidate = 0;
        int count = 0;
        for (int x : nums) {
            if (count == 0) {
                candidate = x;      // no standing candidate: pick this one
            }
            count += (x == candidate) ? 1 : -1;
        }
        return candidate;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "What if the majority element is NOT guaranteed to exist?"
     *      -> Boyer-Moore only finds a CANDIDATE. Add a second O(n) pass to
     *         count the candidate's true frequency; return it only if it
     *         exceeds n/2, else report "no majority". Space stays O(1).
     *
     *  F2. "Find all elements that appear more than n/3 times." (LeetCode 229)
     *      -> Generalized Boyer-Moore with TWO candidates and two counters (at
     *         most two values can exceed n/3). Then verify both in a second
     *         pass. For "> n/k" you track k-1 candidates.
     *
     *  F3. "Explain WHY Boyer-Moore works — prove it."
     *      -> Pair up each majority occurrence with a distinct non-majority
     *         one; they cancel. Since majority count > n/2 > (non-majority
     *         count), at least one majority occurrence is left unpaired, so the
     *         final surviving candidate is the majority.
     *
     *  F4. "Randomized approach?"
     *      -> Pick a random index, count its frequency; if > n/2 return it,
     *         else retry. Expected O(n) since a random pick hits the majority
     *         with probability > 1/2, so ~2 iterations on average.
     *
     *  F5. "Divide and conquer?"
     *      -> Recurse on left/right halves; combine: if both halves agree,
     *         that's the answer; otherwise count each candidate over the whole
     *         range and take the larger. O(n log n) time, O(log n) stack.
     *
     *  F6. "Bit-manipulation trick?"
     *      -> For each of the 32 bit positions, count how many numbers have it
     *         set; if more than n/2 do, that bit is set in the majority.
     *         Reconstruct the answer bit by bit. O(32n) time, O(1) space.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, MajorityElement sol) {
        System.out.println(label
                + " boyerMoore=" + sol.majorityElement(nums)
                + ", hashMap="   + sol.majorityElementBruteForce(nums)
                + ", sorting="   + sol.majorityElementSorting(nums));
    }

    public static void main(String[] args) {
        MajorityElement sol = new MajorityElement();

        // Example 1
        report("Example 1 ->", new int[]{3, 2, 3}, sol);
        // Example 2
        report("Example 2 ->", new int[]{2, 2, 1, 1, 1, 2, 2}, sol);

        // Edge cases
        report("Single element   ->", new int[]{7}, sol);                      // 7
        report("All identical    ->", new int[]{9, 9, 9, 9}, sol);             // 9
        report("Majority at ends ->", new int[]{4, 1, 4, 2, 4, 3, 4}, sol);    // 4
        report("With negatives   ->", new int[]{-1, -1, -1, 2, 3}, sol);       // -1
    }
}
