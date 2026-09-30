package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 1. Two Sum                                          [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an array of integers nums and an integer target, return the INDICES of
 * the two numbers such that they add up to target.
 *
 * You may assume that each input would have EXACTLY ONE solution, and you may
 * not use the SAME element twice.
 *
 * You can return the answer in any order.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   2 <= nums.length <= 10^4
 *   -10^9 <= nums[i] <= 10^9
 *   -10^9 <= target <= 10^9
 *   Only one valid answer exists.
 *
 * Follow-up:
 *   Can you come up with an algorithm that is less than O(n^2) time complexity?
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [2,7,11,15], target = 9
 *   Output: [0,1]
 *   Explanation: nums[0] + nums[1] == 2 + 7 == 9.
 *
 * Example 2:
 *   Input:  nums = [3,2,4], target = 6
 *   Output: [1,2]
 *
 * Example 3:
 *   Input:  nums = [3,3], target = 6
 *   Output: [0,1]
 *   Explanation: duplicate values, two distinct indices.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Is EXACTLY ONE solution guaranteed?
 *      -> Yes. So I don't need to handle "no answer" or multiple answers.
 *
 *  Q2. Can I use the same element twice?
 *      -> No — the two indices must be distinct. (Values may repeat, indices
 *         may not.)
 *
 *  Q3. Are the numbers sorted?
 *      -> No, arbitrary order. (If they WERE sorted, a two-pointer O(n) with
 *         O(1) space would apply — that's LeetCode 167.)
 *
 *  Q4. Return indices or values?
 *      -> INDICES of the two numbers, in any order.
 *
 *  Q5. Can values/target be negative or large?
 *      -> Yes (up to +/-1e9). The complement 'target - x' can overflow int?
 *         target - nums[i] ranges within ~[-2e9, 2e9], which EXCEEDS int range;
 *         I'll compute the complement as int carefully (it fits: |diff| <= 2e9
 *         > Integer.MAX 2.147e9? Actually up to 2e9 < 2.147e9, so int is safe),
 *         but using the value directly as a map key sidesteps any concern.
 *
 *  Q6. Duplicate values allowed (e.g. [3,3])?
 *      -> Yes; the one-pass map handles it because we check the complement
 *         BEFORE inserting the current element.
 *
 * ============================================================================
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TwoSum {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check every pair)
     * ------------------------------------------------------------------------
     * Idea:
     * Try all pairs (i, j) with i < j; return the first pair summing to target.
     *
     * Time : O(n^2)
     * Space : O(1)
     *
     * Correct but quadratic; the hash map trades space for linear time.
     */
    public int[] twoSumBruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[] { i, j };
                }
            }
        }
        return new int[] { -1, -1 }; // unreachable given the one-solution guarantee
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — TWO-PASS HASH MAP
     * ------------------------------------------------------------------------
     * Idea:
     * Pass 1: map value -> index for every element.
     * Pass 2: for each i, look up complement (target - nums[i]); if present at
     * a DIFFERENT index, return the pair.
     * The "different index" check avoids reusing the same element.
     *
     * Time : O(n)
     * Space : O(n)
     */
    public int[] twoSumTwoPass(int[] nums, int target) {
        Map<Integer, Integer> indexOf = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            indexOf.put(nums[i], i); // last index wins for duplicates; fine here
        }
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            Integer j = indexOf.get(complement);
            if (j != null && j != i) {
                return new int[] { i, j };
            }
        }
        return new int[] { -1, -1 };
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL ONE-PASS HASH MAP <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     * Walk once, maintaining a map of value -> index for elements seen SO FAR.
     * For each nums[i], check if its complement is already in the map. If so,
     * we've found the pair; otherwise record nums[i] and continue.
     *
     * Checking BEFORE inserting guarantees we never pair an element with
     * itself and naturally handles duplicates like [3,3].
     *
     * Time : O(n)
     * Space : O(n)
     */
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>(); // value -> index
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            Integer j = seen.get(complement);
            if (j != null) { // complement seen earlier
                return new int[] { j, i };
            }
            seen.put(nums[i], i); // record current for future lookups
        }
        return new int[] { -1, -1 }; // unreachable given the guarantee
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why check the complement BEFORE inserting the current element?"
     * -> It ensures the matched index j is strictly earlier than i, so we
     * never use the same element twice — and duplicates like [3,3] work:
     * the second 3 finds the first 3's index.
     *
     * F2. "What if the array were SORTED?"
     * -> Use two pointers from both ends (LeetCode 167): move left/right
     * based on whether the sum is below/above target. O(n) time, O(1)
     * space — better than the hash map on space.
     *
     * F3. "What if there could be NO solution or MULTIPLE solutions?"
     * -> Drop the single-answer assumption: return empty/none if the loop
     * ends; for all pairs, collect every match instead of returning early
     * (careful with duplicate index pairs).
     *
     * F4. "Return the VALUES instead of indices?"
     * -> Trivial: return {nums[i], complement}. But then sorting the input
     * + two pointers becomes viable since indices no longer matter.
     *
     * F5. "Three numbers summing to target (3Sum)?"
     * -> Sort, fix one element, and two-pointer the rest: O(n^2). Two Sum
     * is the inner primitive.
     *
     * F6. "Overflow concerns with target - nums[i]?"
     * -> With values in +/-1e9, the difference can reach ~2e9, still within
     * int range (2.147e9). For wider ranges, compute the complement as
     * long to be safe.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, int target, TwoSum sol) {
        System.out.println(label + " nums=" + Arrays.toString(nums) + ", target=" + target
                + " -> onePass=" + Arrays.toString(sol.twoSum(nums, target))
                + ", twoPass=" + Arrays.toString(sol.twoSumTwoPass(nums, target))
                + ", brute=" + Arrays.toString(sol.twoSumBruteForce(nums, target)));
    }

    public static void main(String[] args) {
        TwoSum sol = new TwoSum();

        // Examples
        report("Example 1 ->", new int[] { 2, 7, 11, 15 }, 9, sol); // [0,1]
        report("Example 2 ->", new int[] { 3, 2, 4 }, 6, sol); // [1,2]
        report("Example 3 ->", new int[] { 3, 3 }, 6, sol); // [0,1]

        // Edge cases
        report("Negatives        ->", new int[] { -3, 4, 3, 90 }, 0, sol); // [0,2] (-3+3)
        report("Answer at ends   ->", new int[] { 5, 1, 2, 8 }, 13, sol); // [0,3]
        report("Two elements     ->", new int[] { 1, 2 }, 3, sol); // [0,1]
        report("Large values     ->", new int[] { 1000000000, 7, -1000000000 }, 7, sol); // [0,2]
        report("Zero target      ->", new int[] { 0, 4, 0 }, 0, sol); // [0,2]
    }
}
