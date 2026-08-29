/*
 * ============================================================================
 * LeetCode 88. Merge Sorted Array                              [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given two integer arrays nums1 and nums2, sorted in NON-DECREASING
 * order, and two integers m and n, representing the number of elements in
 * nums1 and nums2 respectively.
 *
 * Merge nums1 and nums2 into a single array sorted in non-decreasing order.
 *
 * The final sorted array should NOT be returned by the function, but instead
 * be stored INSIDE the array nums1. To accommodate this, nums1 has a length of
 * m + n, where the first m elements denote the elements that should be merged,
 * and the last n elements are set to 0 and should be ignored. nums2 has a
 * length of n.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   nums1.length == m + n
 *   nums2.length == n
 *   0 <= m, n <= 200
 *   1 <= m + n <= 200
 *   -10^9 <= nums1[i], nums2[j] <= 10^9
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums1 = [1,2,3,0,0,0], m = 3, nums2 = [2,5,6], n = 3
 *   Output: [1,2,2,3,5,6]
 *   Explanation: We merge [1,2,3] and [2,5,6]. The result is stored in nums1.
 *
 * Example 2:
 *   Input:  nums1 = [1], m = 1, nums2 = [], n = 0
 *   Output: [1]
 *   Explanation: nums2 is empty, so nums1 stays as-is.
 *
 * Example 3:
 *   Input:  nums1 = [0], m = 0, nums2 = [1], n = 1
 *   Output: [1]
 *   Explanation: m = 0, so the first array contributes nothing. The single 0
 *                in nums1 is just a placeholder that gets overwritten.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Are both input arrays guaranteed to be already sorted (non-decreasing)?
 *      -> Yes. This is the key property that lets us avoid a full re-sort.
 *
 *  Q2. Can the arrays contain duplicates?
 *      -> Yes (e.g. the 2 appears in both in Example 1). "Non-decreasing"
 *         allows equal neighbours.
 *
 *  Q3. Can m or n be zero?
 *      -> Yes. Either array may be empty. My solution must handle both.
 *
 *  Q4. Must the merge happen IN-PLACE inside nums1, or can I return a new array?
 *      -> It must be in-place; the function returns void and mutates nums1.
 *         The trailing n zeros in nums1 are just reserved space.
 *
 *  Q5. Any constraint on extra memory?
 *      -> Not stated explicitly, but interviewers expect O(1) extra space
 *         because the extra room is already provided at the end of nums1.
 *
 *  Q6. What's the value range? Could there be overflow concerns?
 *      -> Values fit in int (|value| <= 1e9). No overflow in comparisons.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class MergeSortedArray {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (copy then sort)
     * ------------------------------------------------------------------------
     * Idea:
     *   1. Copy all n elements of nums2 into the tail slots of nums1
     *      (indices m .. m+n-1, which currently hold the placeholder zeros).
     *   2. Sort the whole nums1 array.
     *
     * We throw away the "already sorted" property entirely and just let a
     * general sort do the work. Simple, correct, easy to reason about — a fine
     * first cut to state out loud before optimizing.
     *
     * Time  : O((m + n) * log(m + n))   -- dominated by the sort
     * Space : O(1) auxiliary if we ignore the sort's internal stack; the array
     *         itself already has room, so no extra allocation.
     *
     * Weakness: We ignore that the inputs are pre-sorted, so we do strictly
     * more work than necessary. Interviewer will nudge us toward linear time.
     */
    public void mergeBruteForce(int[] nums1, int m, int[] nums2, int n) {
        for (int i = 0; i < n; i++) {
            nums1[m + i] = nums2[i];
        }
        Arrays.sort(nums1);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (three pointers, fill from the BACK)
     * ------------------------------------------------------------------------
     * Key insight:
     *   If we merge from the FRONT, writing into nums1 would overwrite elements
     *   of nums1 we haven't read yet. To avoid that, we fill from the BACK
     *   (largest elements first) into the free tail slots — those slots are
     *   guaranteed to be at or ahead of any element we still need to read.
     *
     * Pointers:
     *   i -> last valid element of nums1  (starts at m - 1)
     *   j -> last element of nums2        (starts at n - 1)
     *   k -> current write position       (starts at m + n - 1, the very end)
     *
     * At each step we place the LARGER of nums1[i] / nums2[j] at nums1[k],
     * then move that pointer and k leftward.
     *
     * After the main loop, if any nums2 elements remain (j >= 0) we copy them
     * in. We do NOT need to handle leftover nums1 elements — they are already
     * sitting in their correct final positions at the front of nums1.
     *
     * Time  : O(m + n)   -- each element is written exactly once
     * Space : O(1)       -- purely in-place, no auxiliary array
     */
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int i = m - 1;          // last real element in nums1
        int j = n - 1;          // last element in nums2
        int k = m + n - 1;      // write pointer at the tail of nums1

        while (j >= 0) {                          // once nums2 is exhausted we're done
            if (i >= 0 && nums1[i] > nums2[j]) {
                nums1[k--] = nums1[i--];          // nums1's element is bigger
            } else {
                nums1[k--] = nums2[j--];          // take from nums2 (also handles i < 0)
            }
        }
        // No cleanup for nums1 needed: any remaining nums1[0..i] are already placed.
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "What if nums1 did NOT have the extra room and you had to return a
     *       brand-new merged array?"
     *      -> Allocate a result[m + n] and do a standard forward two-pointer
     *         merge (like the merge step of merge sort). O(m+n) time/space.
     *
     *  F2. "Merge K sorted arrays instead of 2."
     *      -> Use a min-heap (PriorityQueue) of size K holding the current head
     *         of each array. Pop the min, push the next from that array.
     *         Time O(N log K) where N is the total element count.
     *
     *  F3. "What if the arrays are so large they don't fit in memory (external
     *       sort / streaming)?"
     *      -> K-way merge with buffered readers/iterators, streaming output to
     *         disk. Same heap idea but I/O-bound; read/write in chunks.
     *
     *  F4. "Remove duplicates while merging so the result has unique values."
     *      -> Track the last written value and skip a candidate if it equals it.
     *
     *  F5. "What if the arrays were sorted in DESCENDING order?"
     *      -> Fill from the FRONT taking the larger head, or flip comparisons
     *         and fill from the back with the smaller element.
     *
     *  F6. "Why fill from the back and not the front?" (very common)
     *      -> Filling from the front risks overwriting unread nums1 values.
     *         The tail slots are free, so back-to-front writes never clobber
     *         data we still need. This is the whole trick of the O(1)-space sol.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the provided examples against the optimal solution.
    // ------------------------------------------------------------------------
    public static void main(String[] args) {
        MergeSortedArray sol = new MergeSortedArray();

        // Example 1
        int[] a1 = {1, 2, 3, 0, 0, 0};
        int[] b1 = {2, 5, 6};
        sol.merge(a1, 3, b1, 3);
        System.out.println("Example 1 -> " + Arrays.toString(a1)); // [1, 2, 2, 3, 5, 6]

        // Example 2
        int[] a2 = {1};
        int[] b2 = {};
        sol.merge(a2, 1, b2, 0);
        System.out.println("Example 2 -> " + Arrays.toString(a2)); // [1]

        // Example 3
        int[] a3 = {0};
        int[] b3 = {1};
        sol.merge(a3, 0, b3, 1);
        System.out.println("Example 3 -> " + Arrays.toString(a3)); // [1]

        // Extra edge: nums1 all smaller than nums2
        int[] a4 = {1, 2, 3, 0, 0, 0};
        int[] b4 = {4, 5, 6};
        sol.merge(a4, 3, b4, 3);
        System.out.println("Edge (disjoint) -> " + Arrays.toString(a4)); // [1, 2, 3, 4, 5, 6]
    }
}
