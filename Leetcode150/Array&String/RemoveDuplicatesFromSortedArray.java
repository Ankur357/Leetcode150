/*
 * ============================================================================
 * LeetCode 26. Remove Duplicates from Sorted Array             [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an integer array nums sorted in NON-DECREASING order, remove the
 * duplicates IN-PLACE such that each unique element appears only ONCE. The
 * relative order of the elements should be kept the same. Then return the
 * number of unique elements in nums.
 *
 * Consider the number of unique elements of nums to be k. To be accepted, you
 * must do the following:
 *   - Change the array nums such that the first k elements of nums contain the
 *     unique elements in the order they were present originally. The remaining
 *     elements of nums are not important, as is the size of nums.
 *   - Return k.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= nums.length <= 3 * 10^4
 *   -100 <= nums[i] <= 100
 *   nums is sorted in non-decreasing order.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [1,1,2]
 *   Output: k = 2, nums = [1,2,_]
 *   Explanation: Unique elements are 1 and 2. Trailing slot is "don't care".
 *
 * Example 2:
 *   Input:  nums = [0,0,1,1,1,2,2,3,3,4]
 *   Output: k = 5, nums = [0,1,2,3,4,_,_,_,_,_]
 *   Explanation: Five unique values in original order.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Is the array guaranteed SORTED (non-decreasing)?
 *      -> Yes. This is the crucial property: all copies of a value are
 *         contiguous, so a single linear pass suffices — no hashing needed.
 *
 *  Q2. Must relative order be preserved?
 *      -> Yes, keep original order. (Trivially satisfied since the array is
 *         already sorted and we scan left to right.)
 *
 *  Q3. In-place, or may I return a new array?
 *      -> In-place. Return k; the first k slots must hold the unique values.
 *
 *  Q4. Each unique element appears exactly ONCE in the result, right?
 *      -> Yes. (Contrast with LeetCode 80 which allows up to two copies.)
 *
 *  Q5. Can the array be empty?
 *      -> Per constraints length >= 1, but a robust solution should still
 *         return 0 for an empty array.
 *
 *  Q6. What about the elements beyond index k?
 *      -> Irrelevant; no need to clear them or resize.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class RemoveDuplicatesFromSortedArray {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (LinkedHashSet to dedupe, copy back)
     * ------------------------------------------------------------------------
     * Idea:
     *   Insert every element into a LinkedHashSet (preserves insertion order,
     *   drops duplicates), then copy the set back into the front of nums.
     *
     * This ignores the "sorted" property and works even on UNSORTED input, at
     * the cost of extra memory and boxing. A reasonable first statement before
     * exploiting the sortedness.
     *
     * Time  : O(n)   -- set insert + copy back (amortized O(1) per op)
     * Space : O(n)   -- the set (plus Integer boxing overhead)
     */
    public int removeDuplicatesBruteForce(int[] nums) {
        java.util.LinkedHashSet<Integer> seen = new java.util.LinkedHashSet<>();
        for (int x : nums) {
            seen.add(x);
        }
        int k = 0;
        for (int x : seen) {
            nums[k++] = x;
        }
        return k;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (two pointers, slow/fast)
     * ------------------------------------------------------------------------
     * Idea:
     *   Because the array is sorted, duplicates are contiguous. Keep a WRITE
     *   pointer k marking the position of the LAST unique element written
     *   (so nums[k] is the most recent kept value). Scan i from 1 to end:
     *   whenever nums[i] differs from nums[k], it's a NEW unique value, so
     *   advance k and copy nums[i] into nums[k]. Otherwise skip (duplicate).
     *
     * After the loop, indices 0..k hold the uniques, so the count is k + 1.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)   -- in-place, no auxiliary structure
     */
    public int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;   // defensive; constraints say length >= 1

        int k = 0;                        // index of last unique element written
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] != nums[k]) {     // found a value not yet recorded
                k++;
                nums[k] = nums[i];
            }
        }
        return k + 1;                     // count = last index + 1
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "What if the array were NOT sorted?"
     *      -> Contiguity is lost, so the O(1) trick breaks. Use a HashSet to
     *         track seen values (O(n) time, O(n) space), or sort first
     *         (O(n log n) time, O(1) extra if sorting in place) then apply
     *         this two-pointer pass.
     *
     *  F2. "Allow each element to appear at MOST TWICE." (LeetCode 80)
     *      -> Same skeleton, but compare against nums[k-1] (the element two
     *         slots back): keep nums[i] if k < 2 || nums[i] != nums[k-2].
     *         Generalizes to "at most m copies" by comparing to nums[k-m].
     *
     *  F3. "Return the COUNT of each unique value too (like a run-length view)."
     *      -> Track a running count while scanning contiguous equal runs; emit
     *         (value, count) pairs. Still O(n) time.
     *
     *  F4. "Remove elements that appear MORE than once entirely (keep only
     *       values that are unique in the whole array)."
     *      -> Different problem: a value with duplicates is dropped completely.
     *         Because it's sorted, compare each run's length to 1 and only
     *         emit runs of length exactly 1.
     *
     *  F5. "The data is a sorted STREAM too large for memory."
     *      -> Streaming dedupe: remember only the last emitted value; emit the
     *         current element iff it differs from it. O(1) memory.
     *
     *  F6. "Why is k+1 the answer and not k?"
     *      -> k is an INDEX (0-based) of the last unique element. The number of
     *         elements from index 0 through k inclusive is k + 1.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against the optimal solution.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums,
                               java.util.function.ToIntFunction<int[]> fn) {
        int[] copy = Arrays.copyOf(nums, nums.length);
        int k = fn.applyAsInt(copy);
        System.out.println(label + " k=" + k
                + ", first k = " + Arrays.toString(Arrays.copyOf(copy, k)));
    }

    public static void main(String[] args) {
        RemoveDuplicatesFromSortedArray sol = new RemoveDuplicatesFromSortedArray();

        // Example 1
        report("Example 1 ->", new int[]{1, 1, 2}, sol::removeDuplicates);
        // Example 2
        report("Example 2 ->", new int[]{0, 0, 1, 1, 1, 2, 2, 3, 3, 4}, sol::removeDuplicates);

        // Edge cases
        report("Single element   ->", new int[]{7}, sol::removeDuplicates);        // k=1
        report("All identical    ->", new int[]{5, 5, 5, 5}, sol::removeDuplicates); // k=1
        report("No duplicates    ->", new int[]{1, 2, 3, 4}, sol::removeDuplicates); // k=4
        report("With negatives   ->", new int[]{-3, -3, -1, 0, 0, 2}, sol::removeDuplicates); // k=4

        // Brute-force sanity check on Example 2
        report("Example 2 (brute) ->", new int[]{0, 0, 1, 1, 1, 2, 2, 3, 3, 4},
                sol::removeDuplicatesBruteForce);
    }
}
