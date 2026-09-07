/*
 * ============================================================================
 * LeetCode 189. Rotate Array                                 [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an integer array nums, rotate the array to the RIGHT by k steps, where
 * k is non-negative.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= nums.length <= 10^5
 *   -2^31 <= nums[i] <= 2^31 - 1
 *   0 <= k <= 10^5
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [1,2,3,4,5,6,7], k = 3
 *   Output: [5,6,7,1,2,3,4]
 *   Explanation:
 *     rotate 1 step  to the right: [7,1,2,3,4,5,6]
 *     rotate 2 steps to the right: [6,7,1,2,3,4,5]
 *     rotate 3 steps to the right: [5,6,7,1,2,3,4]
 *
 * Example 2:
 *   Input:  nums = [-1,-100,3,99], k = 2
 *   Output: [3,99,-1,-100]
 *   Explanation:
 *     rotate 1 step  to the right: [99,-1,-100,3]
 *     rotate 2 steps to the right: [3,99,-1,-100]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Rotate to the RIGHT, so the last k elements wrap around to the front?
 *      -> Yes. Element at index i moves to index (i + k) % n.
 *
 *  Q2. Can k be LARGER than the array length?
 *      -> Yes (k up to 1e5). Rotating by n is a no-op, so I must reduce
 *         k = k % n first. Skipping this over-rotates or breaks index math.
 *
 *  Q3. Must this be IN-PLACE, or can I use an auxiliary array?
 *      -> The classic follow-up demands O(1) extra space in-place. I'll show
 *         the simple O(n)-space version first, then the reversal trick.
 *
 *  Q4. Can k be 0?
 *      -> Yes. After k %= n, k == 0 means no rotation — return unchanged.
 *
 *  Q5. Any concern with the value range (INT_MIN present)?
 *      -> Values fit in int; we only move them, never do arithmetic on the
 *         values, so no overflow. Index arithmetic stays within n.
 *
 *  Q6. Single-element array?
 *      -> n == 1: any rotation leaves it unchanged (k % 1 == 0).
 *
 * ============================================================================
 */

import java.util.Arrays;

public class RotateArray {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (extra array via modular placement)
     * ------------------------------------------------------------------------
     * Idea:
     *   Allocate a result array and place each element at its rotated position:
     *   result[(i + k) % n] = nums[i]. Then copy result back into nums.
     *
     * Dead simple and hard to get wrong, but uses O(n) extra space, which the
     * interview follow-up forbids.
     *
     * Time  : O(n)
     * Space : O(n)   -- the result array
     */
    public void rotateBruteForce(int[] nums, int k) {
        int n = nums.length;
        k %= n;                               // normalize k into [0, n)
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[(i + k) % n] = nums[i];
        }
        System.arraycopy(result, 0, nums, 0, n);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (reverse three times)  <-- the canonical answer
     * ------------------------------------------------------------------------
     * Idea:
     *   A right rotation by k splits the array into two blocks:
     *       [ 0 .. n-k-1 ] [ n-k .. n-1 ]
     *   and the answer is those two blocks swapped: [ n-k .. n-1 ][ 0 .. n-k-1 ].
     *   That block swap is achieved by three in-place reversals:
     *     1. Reverse the WHOLE array.
     *     2. Reverse the first k elements.
     *     3. Reverse the remaining n - k elements.
     *
     *   Walkthrough on [1,2,3,4,5,6,7], k = 3:
     *     reverse all      -> [7,6,5,4,3,2,1]
     *     reverse first 3  -> [5,6,7,4,3,2,1]
     *     reverse last 4   -> [5,6,7,1,2,3,4]   ✓
     *
     * Time  : O(n)   -- each element is touched a constant number of times
     * Space : O(1)   -- pure in-place swaps
     */
    public void rotate(int[] nums, int k) {
        int n = nums.length;
        k %= n;                               // guard against k >= n (and k == 0)
        if (k == 0) return;                   // nothing to do
        reverse(nums, 0, n - 1);              // whole array
        reverse(nums, 0, k - 1);              // first k
        reverse(nums, k, n - 1);              // remaining n - k
    }

    // Reverses nums[left..right] in place using two converging pointers.
    private void reverse(int[] nums, int left, int right) {
        while (left < right) {
            int tmp = nums[left];
            nums[left] = nums[right];
            nums[right] = tmp;
            left++;
            right--;
        }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL VARIANT (cyclic replacements)
     * ------------------------------------------------------------------------
     * Idea:
     *   Move each element DIRECTLY to its final slot (i -> (i + k) % n),
     *   carrying the displaced value forward. We follow cycles; a `count` of
     *   how many elements have been placed tells us when to stop. When a cycle
     *   returns to its start, we bump the start index to begin the next cycle.
     *
     *   The number of independent cycles equals gcd(n, k), which is why a
     *   single start pointer isn't always enough.
     *
     * Time  : O(n)   -- every element is moved exactly once
     * Space : O(1)
     *
     * Trickier to implement correctly than the reversal method; good to show
     * you understand the cycle structure, but reversal is usually preferred.
     */
    public void rotateCyclic(int[] nums, int k) {
        int n = nums.length;
        k %= n;
        if (k == 0) return;

        int count = 0;                        // total elements relocated
        for (int start = 0; count < n; start++) {
            int current = start;
            int prev = nums[start];
            do {
                int next = (current + k) % n;
                int temp = nums[next];
                nums[next] = prev;            // drop the carried value into place
                prev = temp;                  // pick up whatever was there
                current = next;
                count++;
            } while (start != current);       // stop when the cycle closes
        }
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Do it in-place with O(1) extra space." (the headline follow-up)
     *      -> That's exactly the reversal method (Approach 2) or the cyclic
     *         method (Approach 3). Reversal is simpler and cache-friendly.
     *
     *  F2. "Rotate to the LEFT by k instead of right."
     *      -> Left rotation by k == right rotation by (n - k). Or reverse the
     *         first k, then last n-k, then the whole array (mirror of Approach 2).
     *
     *  F3. "Why must you reduce k modulo n first?"
     *      -> Rotating by n returns the original array, so only k % n matters.
     *         Without it, k > n causes redundant work (brute force) or
     *         out-of-range block splits (reversal).
     *
     *  F4. "Why does the cyclic method need gcd(n, k) cycles?"
     *      -> Starting at index 0 and repeatedly adding k mod n only visits
     *         indices that are multiples of gcd(n,k). There are gcd(n,k) such
     *         residue classes, so we restart from a fresh index that many times.
     *
     *  F5. "The array is a fixed-size ring buffer / stream — how would rotation
     *       differ?"
     *      -> A ring buffer rotates for FREE by moving the logical head pointer
     *         (index (head + k) % n); no element movement at all. O(1) time.
     *
     *  F6. "Compare the three approaches — which do you pick?"
     *      -> Extra-array: simplest, O(n) space. Reversal: O(1) space, simple,
     *         3n swaps — the usual pick. Cyclic: O(1) space, exactly n moves
     *         (fewest writes) but fiddliest to get right.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, int k, RotateArray sol) {
        int[] a = Arrays.copyOf(nums, nums.length);
        int[] b = Arrays.copyOf(nums, nums.length);
        int[] c = Arrays.copyOf(nums, nums.length);
        sol.rotate(a, k);
        sol.rotateBruteForce(b, k);
        sol.rotateCyclic(c, k);
        System.out.println(label + " k=" + k
                + "\n   reversal   = " + Arrays.toString(a)
                + "\n   bruteForce = " + Arrays.toString(b)
                + "\n   cyclic     = " + Arrays.toString(c));
    }

    public static void main(String[] args) {
        RotateArray sol = new RotateArray();

        // Example 1
        report("Example 1 ->", new int[]{1, 2, 3, 4, 5, 6, 7}, 3, sol);
        // Example 2
        report("Example 2 ->", new int[]{-1, -100, 3, 99}, 2, sol);

        // Edge cases
        report("k = 0 (no-op)        ->", new int[]{1, 2, 3}, 0, sol);   // [1,2,3]
        report("k == n (full turn)   ->", new int[]{1, 2, 3}, 3, sol);   // [1,2,3]
        report("k > n (wraps around) ->", new int[]{1, 2, 3}, 4, sol);   // [3,1,2]
        report("single element       ->", new int[]{42}, 5, sol);        // [42]
    }
}
