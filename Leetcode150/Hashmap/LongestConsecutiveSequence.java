package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 128. Longest Consecutive Sequence                  [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an unsorted array of integers nums, return the length of the LONGEST
 * consecutive elements sequence.
 *
 * You must write an algorithm that runs in O(n) time.
 *
 * (A consecutive sequence is a run of integers x, x+1, x+2, ... that all appear
 * in nums, regardless of their order or position in the array.)
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   0 <= nums.length <= 10^5
 *   -10^9 <= nums[i] <= 10^9
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [100,4,200,1,3,2]
 *   Output: 4
 *   Explanation: The longest consecutive run is [1,2,3,4], length 4.
 *
 * Example 2:
 *   Input:  nums = [0,3,7,2,5,8,4,6,0,1]
 *   Output: 9
 *   Explanation: [0,1,2,3,4,5,6,7,8], length 9 (duplicate 0 doesn't extend it).
 *
 * Example 3:
 *   Input:  nums = [1,0,1,2]
 *   Output: 3
 *   Explanation: [0,1,2], length 3.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Does the sequence need to be CONTIGUOUS in the array, or just present?
 *      -> Just present anywhere; order/positions don't matter. It's about the
 *         VALUES forming a run x, x+1, x+2, ...
 *
 *  Q2. Are DUPLICATES possible, and do they affect length?
 *      -> Yes duplicates can appear; they don't lengthen a run (a Set collapses
 *         them). [1,1,2] -> longest is [1,2], length 2.
 *
 *  Q3. Is O(n) REQUIRED (so sorting O(n log n) is disallowed)?
 *      -> The problem demands O(n). Sorting is a valid fallback to MENTION but
 *         the target solution is the hash-set O(n) approach.
 *
 *  Q4. Can the array be EMPTY?
 *      -> Yes (length 0). Then the answer is 0.
 *
 *  Q5. Value range — negatives / large ints?
 *      -> Yes (+/-1e9). Consecutive means differ by exactly 1; no overflow risk
 *         with careful +1 (values stay within int).
 *
 *  Q6. Return the LENGTH, not the sequence itself?
 *      -> The length. (Recovering the actual run is a trivial extension.)
 *
 * ============================================================================
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LongestConsecutiveSequence {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — SORT THEN SCAN [O(n log n) fallback, not O(n)]
     * ------------------------------------------------------------------------
     * Idea:
     * Sort the array. Walk it, extending the current run when the next value
     * is exactly previous + 1, skipping equal duplicates, and resetting on a
     * gap. Track the longest run.
     *
     * Time : O(n log n) -- the sort dominates
     * Space : O(1) or O(n) depending on the sort
     *
     * Correct and simple, but does NOT meet the O(n) requirement. Good to state
     * as a baseline before giving the hash-set solution.
     */
    public int longestConsecutiveSort(int[] nums) {
        if (nums.length == 0)
            return 0;
        int[] a = nums.clone();
        Arrays.sort(a);

        int longest = 1, current = 1;
        for (int i = 1; i < a.length; i++) {
            if (a[i] == a[i - 1])
                continue; // skip duplicates
            if (a[i] == a[i - 1] + 1) {
                current++; // extend run
            } else {
                current = 1; // gap -> reset
            }
            longest = Math.max(longest, current);
        }
        return longest;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: HASH SET, count from sequence STARTS only <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     * Put all values in a HashSet (dedup + O(1) membership). A number x is the
     * START of a consecutive run iff x-1 is NOT in the set. For each such start,
     * walk x, x+1, x+2, ... while present, counting the length. Track the max.
     *
     * The key insight for O(n): we ONLY begin counting at sequence starts, so
     * each value is visited by the inner while-loop at most once across the
     * whole run it belongs to. Total inner work is O(n), not O(n^2).
     *
     * Time : O(n) -- amortized; each number touched a constant number of times
     * Space : O(n) -- the set
     */
    public int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int x : nums)
            set.add(x);

        int longest = 0;
        for (int x : set) {
            // Only start counting from the beginning of a run.
            if (!set.contains(x - 1)) {
                int current = x;
                int length = 1;
                while (set.contains(current + 1)) {
                    current++;
                    length++;
                }
                longest = Math.max(longest, length);
            }
        }
        return longest;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why is the hash-set approach O(n) despite the inner while-loop?"
     * -> We enter the inner loop ONLY at run starts (x with no x-1). Each
     * value is then walked exactly once as part of its run. The number
     * of start-checks is n and the total inner steps sum to n, so it's
     * O(n) overall — NOT O(n^2).
     *
     * F2. "What breaks if you count from EVERY element, not just starts?"
     * -> You'd re-walk the same run from each of its members, giving
     * O(n * L) worst case (e.g. one long run -> O(n^2)). The 'x-1 absent'
     * guard is what keeps it linear.
     *
     * F3. "Sort vs. hash set — trade-offs?"
     * -> Sort is O(n log n) time / O(1)-ish space and dead simple; hash set
     * is O(n) time / O(n) space and meets the requirement. Choose based
     * on whether O(n) is mandated and memory is available.
     *
     * F4. "How are duplicates handled?"
     * -> The HashSet collapses duplicates, so a repeated value neither
     * lengthens a run nor causes double counting.
     *
     * F5. "Return the actual sequence, not just the length?"
     * -> Track the start value and best length together; the run is
     * [bestStart, bestStart + bestLen - 1].
     *
     * F6. "Union-Find alternative?"
     * -> You can union x with x+1 whenever both exist and report the largest
     * component size. It's also near-linear but heavier to implement than
     * the set scan; the set approach is preferred here.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, LongestConsecutiveSequence sol) {
        System.out.println(label + " nums=" + Arrays.toString(nums)
                + " -> hashSet=" + sol.longestConsecutive(nums)
                + ", sort=" + sol.longestConsecutiveSort(nums));
    }

    public static void main(String[] args) {
        LongestConsecutiveSequence sol = new LongestConsecutiveSequence();

        // Examples
        report("Example 1 ->", new int[] { 100, 4, 200, 1, 3, 2 }, sol); // 4
        report("Example 2 ->", new int[] { 0, 3, 7, 2, 5, 8, 4, 6, 0, 1 }, sol); // 9
        report("Example 3 ->", new int[] { 1, 0, 1, 2 }, sol); // 3

        // Edge cases
        report("Empty            ->", new int[] {}, sol); // 0
        report("Single element   ->", new int[] { 42 }, sol); // 1
        report("All duplicates   ->", new int[] { 5, 5, 5 }, sol); // 1
        report("No consecutives  ->", new int[] { 10, 20, 30 }, sol); // 1
        report("Negatives span 0 ->", new int[] { -2, -1, 0, 1, 3 }, sol); // 4 ([-2..1])
        report("Two equal runs   ->", new int[] { 1, 2, 3, 10, 11, 12 }, sol); // 3
        report("Reverse order    ->", new int[] { 5, 4, 3, 2, 1 }, sol); // 5
    }
}
