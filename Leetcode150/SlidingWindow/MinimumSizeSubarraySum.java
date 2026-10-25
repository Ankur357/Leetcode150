/*
 * ============================================================================
 * LeetCode 209. Minimum Size Subarray Sum                    [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an array of positive integers nums and a positive integer target,
 * return the MINIMAL LENGTH of a subarray whose sum is GREATER THAN OR EQUAL to
 * target. If there is no such subarray, return 0 instead.
 *
 * (A subarray is a contiguous, non-empty sequence of elements.)
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= target <= 10^9
 *   1 <= nums.length <= 10^5
 *   1 <= nums[i] <= 10^4
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  target = 7, nums = [2,3,1,2,4,3]
 *   Output: 2
 *   Explanation: The subarray [4,3] has the minimal length under the problem
 *                constraint (sum 7 >= 7).
 *
 * Example 2:
 *   Input:  target = 4, nums = [1,4,4]
 *   Output: 1
 *   Explanation: The single element [4] already meets the target.
 *
 * Example 3:
 *   Input:  target = 11, nums = [1,1,1,1,1,1,1,1]
 *   Output: 0
 *   Explanation: The total sum is 8 < 11, so no subarray qualifies -> 0.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. All elements are POSITIVE?
 *      -> Yes (1 <= nums[i]). This is crucial: with only positive numbers a
 *         window's sum grows when we extend right and shrinks when we contract
 *         left, which is exactly what makes the sliding window valid.
 *
 *  Q2. Condition is sum >= target (not strictly greater)?
 *      -> Yes, ">=". A subarray meeting exactly target counts.
 *
 *  Q3. Return the LENGTH (not the subarray itself), and 0 if none exists?
 *      -> Correct. 0 is the sentinel for "no qualifying subarray".
 *
 *  Q4. Subarray must be CONTIGUOUS?
 *      -> Yes, contiguous. (A subsequence would be a different, easier problem
 *         since you'd just greedily pick the largest elements.)
 *
 *  Q5. Overflow on the running sum?
 *      -> Max sum ~ 1e5 * 1e4 = 1e9, fits in int, but target can be 1e9 too;
 *         I'll use int carefully (sums stay <= ~1e9) — a long is a safe habit.
 *
 *  Q6. Could there be a faster-than-O(n) requirement / a follow-up?
 *      -> The follow-up asks for an O(n log n) solution too; I'll cover the
 *         prefix-sum + binary search version alongside the O(n) window.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class MinimumSizeSubarraySum {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (try every subarray)
     * ------------------------------------------------------------------------
     * Idea:
     *   For each start i, extend j and accumulate the sum; the moment it reaches
     *   target, record (j - i + 1) and break (extending further only lengthens).
     *   Track the global minimum.
     *
     * Time  : O(n^2)   -- each start scans forward
     * Space : O(1)
     *
     * Correct but quadratic; the sliding window collapses it to linear.
     */
    public int minSubArrayLenBruteForce(int target, int[] nums) {
        int n = nums.length;
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int j = i; j < n; j++) {
                sum += nums[j];
                if (sum >= target) {
                    best = Math.min(best, j - i + 1);
                    break;                       // shortest for this start found
                }
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (sliding window / two pointers)  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Maintain a window [left, right] with running windowSum. Expand `right`
     *   one step at a time, adding nums[right]. Whenever windowSum >= target,
     *   the window qualifies, so try to SHRINK from the left: record the length,
     *   subtract nums[left], and advance left — repeating while it still
     *   qualifies. This finds the shortest window ending at each `right`.
     *
     *   Validity relies on POSITIVE numbers: extending right only increases the
     *   sum, shrinking left only decreases it, so the window sum is monotonic
     *   with respect to each pointer — no need to ever move a pointer backward.
     *
     * Time  : O(n)   -- each element enters and leaves the window at most once
     * Space : O(1)
     */
    public int minSubArrayLen(int target, int[] nums) {
        int n = nums.length;
        int left = 0;
        int windowSum = 0;
        int best = Integer.MAX_VALUE;
        for (int right = 0; right < n; right++) {
            windowSum += nums[right];                 // grow the window to the right
            while (windowSum >= target) {             // shrink while still valid
                best = Math.min(best, right - left + 1);
                windowSum -= nums[left];
                left++;
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — FOLLOW-UP O(n log n) (prefix sums + binary search)
     * ------------------------------------------------------------------------
     * Idea:
     *   Build prefix[k] = sum of the first k elements (prefix[0] = 0). Because
     *   nums are positive, prefix[] is STRICTLY INCREASING. For each start index
     *   i, we want the smallest end index j such that
     *       prefix[j] - prefix[i] >= target  <=>  prefix[j] >= prefix[i] + target.
     *   Binary-search that threshold in the increasing prefix array; the window
     *   length is j - i. Take the minimum over all i.
     *
     * Time  : O(n log n)   -- n binary searches over the prefix array
     * Space : O(n)         -- the prefix array
     *
     * Slower than the O(n) window, but it's the intended "can you also do
     * O(n log n)?" follow-up and generalizes to variants where the window
     * monotonicity is less obvious.
     */
    public int minSubArrayLenBinarySearch(int target, int[] nums) {
        int n = nums.length;
        int[] prefix = new int[n + 1];               // prefix[0] = 0
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int needed = prefix[i] + target;         // want prefix[j] >= needed
            int j = lowerBound(prefix, i + 1, n + 1, needed);
            if (j <= n) {                            // found a valid end
                best = Math.min(best, j - i);
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    // Smallest index in prefix[lo..hi) whose value >= key (array is increasing).
    private int lowerBound(int[] prefix, int lo, int hi, int key) {
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (prefix[mid] < key) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why does the sliding window REQUIRE positive numbers?"
     *      -> Monotonicity: adding an element can only increase the sum,
     *         removing one can only decrease it. With negatives, shrinking the
     *         left could INCREASE the sum, so a shrunk window might still be
     *         valid — the greedy 'shrink while valid' logic breaks.
     *
     *  F2. "What if negatives WERE allowed?"
     *      -> Use prefix sums + a monotonic deque (like 'Shortest Subarray with
     *         Sum at Least K', LeetCode 862): O(n) with a deque of increasing
     *         prefix sums. The plain window no longer works.
     *
     *  F3. "Give the O(n log n) approach." (the stated follow-up)
     *      -> Prefix sums are increasing (positives), so binary-search for the
     *         first prefix[j] >= prefix[i] + target per start i (Approach 3).
     *
     *  F4. "Return the actual subarray, not just its length."
     *      -> Track bestLeft/bestRight whenever `best` improves in the window
     *         loop; return nums[bestLeft..bestRight].
     *
     *  F5. "Find the LONGEST subarray with sum <= target instead."
     *      -> Symmetric sliding window: expand right, and shrink left only when
     *         the sum EXCEEDS target; track the max length. Still O(n).
     *
     *  F6. "Exact sum == target (count or existence)?"
     *      -> With positives, a window works for existence; in general use a
     *         HashMap of prefix sums (subarray-sum-equals-k, LeetCode 560).
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int target, int[] nums, MinimumSizeSubarraySum sol) {
        System.out.println(label + " target=" + target + ", " + Arrays.toString(nums)
                + " -> window=" + sol.minSubArrayLen(target, nums)
                + ", binSearch=" + sol.minSubArrayLenBinarySearch(target, nums)
                + ", brute=" + sol.minSubArrayLenBruteForce(target, nums));
    }

    public static void main(String[] args) {
        MinimumSizeSubarraySum sol = new MinimumSizeSubarraySum();

        // Examples
        report("Example 1 ->", 7, new int[]{2, 3, 1, 2, 4, 3}, sol);            // 2
        report("Example 2 ->", 4, new int[]{1, 4, 4}, sol);                     // 1
        report("Example 3 ->", 11, new int[]{1, 1, 1, 1, 1, 1, 1, 1}, sol);     // 0

        // Edge cases
        report("Single meets      ->", 5, new int[]{5}, sol);                   // 1
        report("Single short      ->", 5, new int[]{3}, sol);                   // 0
        report("Whole array needed->", 15, new int[]{1, 2, 3, 4, 5}, sol);      // 5
        report("First element big ->", 4, new int[]{4, 1, 1, 1}, sol);          // 1
        report("Exact at end      ->", 6, new int[]{1, 1, 1, 3, 3}, sol);       // 2 ([3,3])
    }
}
