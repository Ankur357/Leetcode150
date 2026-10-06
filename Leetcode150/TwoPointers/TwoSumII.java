/*
 * ============================================================================
 * LeetCode 167. Two Sum II - Input Array Is Sorted           [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given a 1-indexed array of integers numbers that is already sorted in
 * NON-DECREASING order, find two numbers such that they add up to a specific
 * target number. Let these two numbers be numbers[index1] and numbers[index2]
 * where 1 <= index1 < index2 <= numbers.length.
 *
 * Return the indices of the two numbers, index1 and index2, ADDED BY ONE, as an
 * integer array [index1, index2] of length 2.
 *
 * The tests are generated such that there is EXACTLY ONE solution. You may not
 * use the same element twice.
 *
 * Your solution must use only CONSTANT extra space.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   2 <= numbers.length <= 3 * 10^4
 *   -1000 <= numbers[i] <= 1000
 *   numbers is sorted in non-decreasing order.
 *   -1000 <= target <= 1000
 *   The tests are generated such that there is exactly one solution.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  numbers = [2,7,11,15], target = 9
 *   Output: [1,2]
 *   Explanation: 2 + 7 = 9. Return 1-based indices [1, 2].
 *
 * Example 2:
 *   Input:  numbers = [2,3,4], target = 6
 *   Output: [1,3]
 *   Explanation: 2 + 4 = 6. Return [1, 3].
 *
 * Example 3:
 *   Input:  numbers = [-1,0], target = -1
 *   Output: [1,2]
 *   Explanation: -1 + 0 = -1. Return [1, 2].
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. The array is guaranteed SORTED (non-decreasing)?
 *      -> Yes. This is what enables the O(1)-space two-pointer approach and is
 *         the key difference from the original Two Sum (LeetCode 1).
 *
 *  Q2. Return indices are 1-BASED, and index1 < index2?
 *      -> Yes. Add 1 to the 0-based positions before returning.
 *
 *  Q3. Exactly ONE solution guaranteed?
 *      -> Yes, so I don't handle "no solution" or multiple solutions.
 *
 *  Q4. Must use CONSTANT extra space?
 *      -> Yes, the prompt mandates O(1). That rules out a HashMap (which would
 *         otherwise solve it in O(n) time / O(n) space).
 *
 *  Q5. Can the same element be used twice?
 *      -> No; index1 must be strictly less than index2.
 *
 *  Q6. Negative numbers / overflow?
 *      -> Values and target are small (|.| <= 1000), sums fit in int easily.
 *
 * ============================================================================
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TwoSumII {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check every pair)
     * ------------------------------------------------------------------------
     * Idea:
     *   Try every pair (i, j) with i < j; return the 1-based indices when the
     *   pair sums to target. Ignores the sortedness entirely.
     *
     * Time  : O(n^2)
     * Space : O(1)
     *
     * Correct but quadratic; the sorted input lets us do much better.
     */
    public int[] twoSumBruteForce(int[] numbers, int target) {
        int n = numbers.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (numbers[i] + numbers[j] == target) {
                    return new int[]{i + 1, j + 1};   // 1-based
                }
            }
        }
        return new int[]{-1, -1};                     // unreachable given the guarantee
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — HASH MAP (O(n) time but O(n) space — VIOLATES the constraint)
     * ------------------------------------------------------------------------
     * Idea:
     *   The generic Two Sum trick: for each value, check if target - value was
     *   seen before. O(n) time, but the map uses O(n) space, which the problem
     *   explicitly forbids. Shown to contrast with the required approach.
     *
     * Time  : O(n)
     * Space : O(n)   -- disallowed here
     */
    public int[] twoSumHashMap(int[] numbers, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < numbers.length; i++) {
            int need = target - numbers[i];
            if (seen.containsKey(need)) {
                return new int[]{seen.get(need) + 1, i + 1};
            }
            seen.put(numbers[i], i);
        }
        return new int[]{-1, -1};
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (two pointers from both ends) <-- required answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Because the array is SORTED, put pointers at both ends and look at the
     *   sum:
     *     - If sum == target -> found it, return 1-based indices.
     *     - If sum <  target -> the sum is too small; the only way to increase
     *       it is to move the LEFT pointer right (to a larger value).
     *     - If sum >  target -> too big; move the RIGHT pointer left (smaller).
     *   Each move discards exactly the values that cannot participate in any
     *   solution, so we never miss the unique answer.
     *
     * Time  : O(n)   -- pointers move toward each other, total < n steps
     * Space : O(1)   -- two indices, satisfies the constraint
     */
    public int[] twoSum(int[] numbers, int target) {
        int left = 0, right = numbers.length - 1;
        while (left < right) {
            int sum = numbers[left] + numbers[right];
            if (sum == target) {
                return new int[]{left + 1, right + 1};   // 1-based indices
            } else if (sum < target) {
                left++;                                  // need a bigger sum
            } else {
                right--;                                 // need a smaller sum
            }
        }
        return new int[]{-1, -1};                        // unreachable (guaranteed solution)
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Prove the two-pointer never misses the unique pair."
     *      -> When sum < target, numbers[left] is too small to pair with ANY
     *         remaining element <= numbers[right], so left can be safely
     *         discarded (all those pairs are < target). Symmetric for sum >
     *         target. Each step eliminates one row/column of the pair matrix
     *         that provably can't be the answer, so the answer survives.
     *
     *  F2. "What if the array were NOT sorted?" (this is LeetCode 1)
     *      -> The HashMap approach (Approach 2): O(n) time, O(n) space. Sorting
     *         first would cost O(n log n) AND lose original indices.
     *
     *  F3. "Could you use BINARY SEARCH instead of two pointers?"
     *      -> Yes: for each i, binary-search target - numbers[i] in the suffix.
     *         O(n log n) time, O(1) space — correct but slower than the linear
     *         two-pointer, so two pointers is preferred.
     *
     *  F4. "What if MULTIPLE valid pairs exist and you must return all?"
     *      -> Continue after each hit: move both pointers inward and skip
     *         duplicate values to avoid repeating pairs (like the 3Sum inner
     *         loop). Still O(n).
     *
     *  F5. "Extend to THREE numbers summing to target (3Sum)?" (LeetCode 15)
     *      -> Sort, then fix each i and run this two-pointer on the remainder.
     *         O(n^2) time, O(1) extra space (excluding output).
     *
     *  F6. "Return the VALUES instead of indices, or count pairs <= target?"
     *      -> Same scan; collect values on a match, or when sum <= target add
     *         (right - left) to a counter and advance left (counting variant).
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] numbers, int target, TwoSumII sol) {
        System.out.println(label + " " + Arrays.toString(numbers) + ", target=" + target
                + " -> twoPtr=" + Arrays.toString(sol.twoSum(numbers, target))
                + ", hashMap=" + Arrays.toString(sol.twoSumHashMap(numbers, target))
                + ", brute=" + Arrays.toString(sol.twoSumBruteForce(numbers, target)));
    }

    public static void main(String[] args) {
        TwoSumII sol = new TwoSumII();

        // Examples
        report("Example 1 ->", new int[]{2, 7, 11, 15}, 9, sol);  // [1,2]
        report("Example 2 ->", new int[]{2, 3, 4}, 6, sol);       // [1,3]
        report("Example 3 ->", new int[]{-1, 0}, -1, sol);        // [1,2]

        // Edge cases
        report("Two elements only ->", new int[]{1, 2}, 3, sol);            // [1,2]
        report("Answer at ends    ->", new int[]{1, 2, 3, 4, 6}, 7, sol);   // [1,5] (1+6)
        report("Adjacent middle   ->", new int[]{1, 2, 4, 5, 9}, 9, sol);   // [3,4] (4+5)
        report("All negatives     ->", new int[]{-8, -5, -3, -2}, -8, sol); // [2,3] (-5+-3)
        report("Duplicates        ->", new int[]{3, 3}, 6, sol);            // [1,2]
    }
}
