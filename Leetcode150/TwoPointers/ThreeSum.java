/*
 * ============================================================================
 * LeetCode 15. 3Sum                                          [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an integer array nums, return all the triplets
 * [nums[i], nums[j], nums[k]] such that i != j, i != k, and j != k, and
 * nums[i] + nums[j] + nums[k] == 0.
 *
 * Notice that the solution set must NOT contain duplicate triplets.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   3 <= nums.length <= 3000
 *   -10^5 <= nums[i] <= 10^5
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [-1,0,1,2,-1,-4]
 *   Output: [[-1,-1,2],[-1,0,1]]
 *   Explanation:
 *     -1 + 0 + 1  = 0
 *     -1 + -1 + 2 = 0
 *     -1 +  0 + 1 = 0
 *     The distinct triplets are [-1,-1,2] and [-1,0,1]. Note the ORDER of the
 *     output and the order of the triplets does not matter.
 *
 * Example 2:
 *   Input:  nums = [0,1,1]
 *   Output: []
 *   Explanation: The only possible triplet does not sum up to 0.
 *
 * Example 3:
 *   Input:  nums = [0,0,0]
 *   Output: [[0,0,0]]
 *   Explanation: The only possible triplet sums up to 0.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Triplets are by VALUE and must be UNIQUE (no duplicate value-triplets)?
 *      -> Yes. [-1,0,1] appearing twice in the array still counts once. Order
 *         within a triplet and among triplets doesn't matter.
 *
 *  Q2. Can I reuse the same INDEX twice?
 *      -> No — i, j, k must be distinct indices. But equal VALUES at different
 *         indices are allowed (e.g. two -1's forming [-1,-1,2]).
 *
 *  Q3. Target is exactly 0?
 *      -> Yes here. The approach generalizes to any target trivially.
 *
 *  Q4. Is sorting the array acceptable (it changes order but not values)?
 *      -> Yes; we return values, not indices, so sorting is the natural setup
 *         for the two-pointer + easy duplicate skipping.
 *
 *  Q5. Overflow when summing three values?
 *      -> 3 * 1e5 = 3e5, well within int. No overflow.
 *
 *  Q6. Expected complexity?
 *      -> O(n^2) is the standard optimal (sort + two-pointer). Brute force is
 *         O(n^3). I'll present both.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreeSum {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (all triplets, dedupe via a set)
     * ------------------------------------------------------------------------
     * Idea:
     *   Try every triple (i, j, k). When the sum is 0, sort the triplet and add
     *   it to a Set to eliminate duplicate value-combinations. Convert the set
     *   to a list at the end.
     *
     * Time  : O(n^3)          -- three nested loops
     * Space : O(#triplets)    -- the dedupe set
     *
     * Correct but far too slow for n up to 3000; motivates sort + two-pointer.
     */
    public List<List<Integer>> threeSumBruteForce(int[] nums) {
        int n = nums.length;
        Set<List<Integer>> set = new HashSet<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    if (nums[i] + nums[j] + nums[k] == 0) {
                        List<Integer> triplet = Arrays.asList(nums[i], nums[j], nums[k]);
                        triplet.sort(null);                 // canonical order for dedupe
                        set.add(triplet);
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (sort, then fix one + two-pointer)  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   1. SORT nums. This lets us (a) use a two-pointer sweep like Two Sum II,
     *      and (b) skip duplicates cheaply since equal values are adjacent.
     *   2. For each index i (the first element of the triplet), we need two more
     *      numbers summing to -nums[i]. Use left/right pointers on the suffix
     *      (i+1 .. n-1):
     *        sum = nums[i] + nums[left] + nums[right]
     *        sum == 0 -> record; then advance BOTH pointers past duplicates.
     *        sum <  0 -> move left right (need a bigger sum).
     *        sum >  0 -> move right left (need a smaller sum).
     *   3. DEDUPE: skip repeated nums[i] values, and after a hit skip repeated
     *      nums[left]/nums[right], so no duplicate triplet is emitted.
     *
     *   Early exit: once nums[i] > 0, no triplet can sum to 0 (all remaining
     *   values are >= nums[i] > 0), so we stop.
     *
     * Time  : O(n^2)   -- O(n log n) sort + O(n) two-pointer per i
     * Space : O(1) extra (ignoring the sort and the output list)
     */
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        int n = nums.length;

        for (int i = 0; i < n - 2; i++) {
            if (nums[i] > 0) break;                       // all subsequent sums > 0
            if (i > 0 && nums[i] == nums[i - 1]) continue; // skip duplicate first element

            int left = i + 1, right = n - 1;
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                if (sum == 0) {
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    // Skip duplicates for the second and third elements.
                    int lv = nums[left], rv = nums[right];
                    while (left < right && nums[left] == lv) left++;
                    while (left < right && nums[right] == rv) right--;
                } else if (sum < 0) {
                    left++;                               // need a larger sum
                } else {
                    right--;                              // need a smaller sum
                }
            }
        }
        return result;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "How exactly do you avoid duplicate triplets?"
     *      -> Three skips on the SORTED array: (a) skip a repeated first element
     *         nums[i], (b) after recording a hit, skip repeated nums[left], and
     *         (c) skip repeated nums[right]. Because equal values are adjacent
     *         after sorting, each skip is O(1) amortized.
     *
     *  F2. "Why sort first — what does it buy you?"
     *      -> Sorting enables the two-pointer (monotonic sum) turning the inner
     *         search from O(n^2)/hashing into O(n), and makes duplicate skipping
     *         trivial. Net O(n^2) instead of O(n^3).
     *
     *  F3. "Can you do it WITHOUT sorting?"
     *      -> Yes with hashing: fix i, then run a HashSet-based Two Sum on the
     *         rest for target -nums[i]. Still O(n^2) time but O(n) space and
     *         messier deduplication (need a set of triplets).
     *
     *  F4. "Generalize to 4Sum / kSum." (LeetCode 18)
     *      -> Recurse: kSum fixes one element and reduces to (k-1)Sum on the
     *         suffix, bottoming out at the 2-pointer 2Sum. O(n^(k-1)) time.
     *
     *  F5. "3Sum Closest — triplet whose sum is nearest a target." (LeetCode 16)
     *      -> Same sort + two-pointer, but track the minimum |sum - target|
     *         instead of requiring equality. O(n^2).
     *
     *  F6. "3Sum Smaller — count triplets with sum < target." (LeetCode 259)
     *      -> Sort; for each i, two-pointer: when sum < target, ALL pairs
     *         between left and right qualify, so add (right - left) and left++.
     *         O(n^2).
     *
     *  F7. "Lower bound — can 3Sum beat O(n^2)?"
     *      -> It's a long-standing open/hard result; sub-quadratic algorithms
     *         are only marginally better (via 3SUM-hardness theory). O(n^2) is
     *         the practical optimum.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, ThreeSum sol) {
        // Note: threeSum sorts in place, so pass copies to keep the two calls independent.
        int[] a = Arrays.copyOf(nums, nums.length);
        int[] b = Arrays.copyOf(nums, nums.length);
        System.out.println(label + " " + Arrays.toString(nums)
                + "\n   optimal = " + sol.threeSum(a)
                + "\n   brute   = " + sol.threeSumBruteForce(b));
    }

    public static void main(String[] args) {
        ThreeSum sol = new ThreeSum();

        // Examples
        report("Example 1 ->", new int[]{-1, 0, 1, 2, -1, -4}, sol);  // [[-1,-1,2],[-1,0,1]]
        report("Example 2 ->", new int[]{0, 1, 1}, sol);              // []
        report("Example 3 ->", new int[]{0, 0, 0}, sol);              // [[0,0,0]]

        // Edge cases
        report("All positive     ->", new int[]{1, 2, 3, 4}, sol);              // []
        report("All negative     ->", new int[]{-1, -2, -3}, sol);              // []
        report("Many duplicates  ->", new int[]{-2, 0, 0, 2, 2}, sol);          // [[-2,0,2]]
        report("Multiple triplets->", new int[]{-4, -2, -2, 0, 2, 2, 4}, sol);  // [[-4,0,4],[-4,2,2],[-2,-2,4],[-2,0,2]]
        report("Zeros + pairs    ->", new int[]{-1, -1, 2, 0, 0, 0}, sol);      // [[-1,-1,2],[0,0,0]]
    }
}
