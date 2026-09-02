/*
 * ============================================================================
 * LeetCode 80. Remove Duplicates from Sorted Array II        [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an integer array nums sorted in NON-DECREASING order, remove some
 * duplicates IN-PLACE such that each unique element appears AT MOST TWICE. The
 * relative order of the elements should be kept the same. Then return the
 * number of elements remaining.
 *
 * Do NOT allocate extra space for another array — you must do this by
 * modifying the input array in-place with O(1) extra memory.
 *
 * Consider the number of remaining elements to be k. To be accepted:
 *   - Change nums so that its first k elements contain the final result in the
 *     same relative order. Elements beyond k are "don't care".
 *   - Return k.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= nums.length <= 3 * 10^4
 *   -10^4 <= nums[i] <= 10^4
 *   nums is sorted in non-decreasing order.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [1,1,1,2,2,3]
 *   Output: k = 5, nums = [1,1,2,2,3,_]
 *   Explanation: The three 1s are trimmed to two; everything else already
 *                appears at most twice.
 *
 * Example 2:
 *   Input:  nums = [0,0,1,1,1,1,2,3,3]
 *   Output: k = 7, nums = [0,0,1,1,2,3,3,_,_]
 *   Explanation: The four 1s are trimmed to two.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. "At most twice" — so a value that appears once stays once, and any value
 *       appearing 2+ times is capped at exactly two copies?
 *      -> Correct. Keep min(count, 2) copies of each distinct value.
 *
 *  Q2. Is the array guaranteed SORTED?
 *      -> Yes, non-decreasing. Duplicates are contiguous, which is what makes
 *         a single O(1)-space pass possible.
 *
 *  Q3. Must relative order be preserved?
 *      -> Yes. (Automatic here since we scan left to right on sorted data.)
 *
 *  Q4. Strict O(1) extra space required?
 *      -> Yes, the prompt explicitly forbids a second array.
 *
 *  Q5. How does this differ from LeetCode 26?
 *      -> LC26 allows each value ONCE; here the cap is TWO. Same two-pointer
 *         skeleton, but the "keep" test compares against the element two slots
 *         back instead of one.
 *
 *  Q6. Can I mutate the input, and is the tail beyond k irrelevant?
 *      -> Yes to both; only the first k slots are checked.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class RemoveDuplicatesFromSortedArrayII {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (count runs, rebuild via temp buffer)
     * ------------------------------------------------------------------------
     * Idea:
     *   Walk the array counting how many times the current value has appeared
     *   in a row. Copy an element into a temp buffer only while that running
     *   count is <= 2. Copy the buffer back into nums.
     *
     * Correct and easy to explain, but it allocates O(n) extra space — which
     * the prompt explicitly disallows. Useful only as a warm-up statement.
     *
     * Time  : O(n)
     * Space : O(n)   -- the temp buffer (violates the O(1) requirement)
     */
    public int removeDuplicatesBruteForce(int[] nums) {
        int[] temp = new int[nums.length];
        int k = 0;
        int runCount = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) {
                runCount++;
            } else {
                runCount = 1;           // new value: reset the run counter
            }
            if (runCount <= 2) {
                temp[k++] = nums[i];
            }
        }
        for (int i = 0; i < k; i++) {
            nums[i] = temp[i];
        }
        return k;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (two pointers, compare against k-2)
     * ------------------------------------------------------------------------
     * Idea:
     *   Maintain a WRITE index k = number of elements kept so far (also the
     *   next slot to write). Scan every element with read index i. We keep
     *   nums[i] if EITHER:
     *       - fewer than 2 elements are kept yet (k < 2), OR
     *       - nums[i] differs from the element two positions back, nums[k-2].
     *
     *   Why nums[k-2]? The last two KEPT elements sit at k-1 and k-2. If the
     *   incoming value equals nums[k-2], then (on sorted data) nums[k-1] also
     *   equals it — meaning we already have two copies of this value, so we
     *   must skip. Otherwise it's at most the second copy and we keep it.
     *
     *   This reads/writes in place with a fixed number of variables.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)   -- no auxiliary array
     *
     * Generalization: for "at most m copies", compare against nums[k-m] and
     * use the guard `k < m`. (m = 1 recovers LeetCode 26.)
     */
    public int removeDuplicates(int[] nums) {
        int k = 0;                        // count of kept elements / write index
        for (int i = 0; i < nums.length; i++) {
            if (k < 2 || nums[i] != nums[k - 2]) {
                nums[k++] = nums[i];
            }
        }
        return k;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Generalize to at most K copies of each element."
     *      -> Replace the constant 2 with K: keep nums[i] if
     *         `k < K || nums[i] != nums[k - K]`. Same O(n)/O(1) profile.
     *
     *  F2. "Why compare with nums[k-2] and not nums[i-2] or a counter?"
     *      -> k-2 points into the ALREADY-ACCEPTED region, so it correctly
     *         reflects what we've decided to keep even after skips. Using i-2
     *         would count skipped duplicates and over-keep. (A counter also
     *         works but k-2 is the cleanest invariant.)
     *
     *  F3. "What if the array were NOT sorted?"
     *      -> Contiguity is lost. Use a HashMap<value, keptCount> and keep an
     *         element while its count < 2. O(n) time, O(n) space — the O(1)
     *         trick no longer applies.
     *
     *  F4. "Also return, for each value, how many were removed."
     *      -> Track run lengths; removed = max(0, runLen - 2) per distinct
     *         value. Still one pass.
     *
     *  F5. "Solve LeetCode 26 (at most once) with the same code."
     *      -> Yes: it's the K=1 case -> `k < 1 || nums[i] != nums[k-1]`.
     *
     *  F6. "Streaming/sorted-input-too-large-for-memory version?"
     *      -> Remember the last value and how many times it's been emitted in
     *         the current run; emit while that count < 2. O(1) memory.
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
        RemoveDuplicatesFromSortedArrayII sol = new RemoveDuplicatesFromSortedArrayII();

        // Example 1
        report("Example 1 ->", new int[]{1, 1, 1, 2, 2, 3}, sol::removeDuplicates);
        // Example 2
        report("Example 2 ->", new int[]{0, 0, 1, 1, 1, 1, 2, 3, 3}, sol::removeDuplicates);

        // Edge cases
        report("Length < 2 (kept as-is) ->", new int[]{7}, sol::removeDuplicates);         // k=1
        report("All identical           ->", new int[]{4, 4, 4, 4, 4}, sol::removeDuplicates); // k=2
        report("No duplicates           ->", new int[]{1, 2, 3, 4}, sol::removeDuplicates);   // k=4
        report("Exactly two of each     ->", new int[]{1, 1, 2, 2}, sol::removeDuplicates);   // k=4
        report("With negatives          ->", new int[]{-2, -2, -2, 0, 0, 0, 1}, sol::removeDuplicates); // k=5

        // Brute-force sanity check on Example 2
        report("Example 2 (brute) ->", new int[]{0, 0, 1, 1, 1, 1, 2, 3, 3},
                sol::removeDuplicatesBruteForce);
    }
}
