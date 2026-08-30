/*
 * ============================================================================
 * LeetCode 27. Remove Element                                  [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an integer array nums and an integer val, remove all occurrences of
 * val in nums IN-PLACE. The order of the elements may be changed. Then return
 * the number of elements in nums which are NOT equal to val.
 *
 * Consider the number of elements in nums which are not equal to val to be k.
 * To be accepted, you must do the following:
 *   - Change the array nums such that the first k elements of nums contain the
 *     elements which are not equal to val. The remaining elements of nums are
 *     not important, as are the size of nums.
 *   - Return k.
 *
 * The judge checks: the returned k, and that the first k elements of nums
 * (in any order) are exactly the kept elements.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   0 <= nums.length <= 100
 *   0 <= nums[i] <= 50
 *   0 <= val <= 100
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [3,2,2,3], val = 3
 *   Output: k = 2, nums = [2,2,_,_]
 *   Explanation: Two elements (both 2) are not equal to 3. Trailing slots
 *                (underscores) may hold anything.
 *
 * Example 2:
 *   Input:  nums = [0,1,2,2,3,0,4,2], val = 2
 *   Output: k = 5, nums = [0,1,3,0,4,_,_,_]
 *   Explanation: Five elements are not equal to 2. Their order among the first
 *                k slots does not matter.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Does the relative ORDER of the kept elements need to be preserved?
 *      -> No. The judge only checks the multiset of the first k elements, so
 *         we are free to reorder. This unlocks a cheaper approach when the
 *         element being removed is rare.
 *
 *  Q2. Must this be done in-place, or can I return a new array?
 *      -> In-place. We mutate nums and return the count k.
 *
 *  Q3. What should the elements beyond index k be?
 *      -> Irrelevant. We don't have to zero them out or shrink the array.
 *
 *  Q4. Can the array be empty?
 *      -> Yes, length can be 0. Then k = 0 and there's nothing to do.
 *
 *  Q5. Can val be absent from the array entirely?
 *      -> Yes. Then every element is kept and k == nums.length.
 *
 *  Q6. Can all elements equal val?
 *      -> Yes. Then k == 0.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class RemoveElement {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (build into a temp buffer, copy back)
     * ------------------------------------------------------------------------
     * Idea:
     *   1. Scan nums; copy every element != val into a temporary array.
     *   2. Copy the temp buffer back into the front of nums.
     *   3. Return the count.
     *
     * Straightforward and obviously correct. Uses extra memory, which violates
     * the spirit of "in-place" — a fine first statement before optimizing.
     *
     * Time  : O(n)   -- one pass to filter, one pass to copy back
     * Space : O(n)   -- the temporary buffer
     */
    public int removeElementBruteForce(int[] nums, int val) {
        int[] temp = new int[nums.length];
        int count = 0;
        for (int x : nums) {
            if (x != val) {
                temp[count++] = x;
            }
        }
        for (int i = 0; i < count; i++) {
            nums[i] = temp[i];
        }
        return count;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (two pointers, same direction — "slow/fast")
     * ------------------------------------------------------------------------
     * Idea:
     *   Maintain a WRITE pointer k marking the next slot for a kept element.
     *   Scan with a READ pointer i over the whole array. Whenever nums[i] is
     *   NOT val, copy it to nums[k] and advance k. Values equal to val are
     *   simply skipped. At the end, the first k slots are all the keepers.
     *
     * This is the canonical in-place "stable partition keep" pattern and it
     * PRESERVES relative order for free.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)   -- no auxiliary array
     */
    public int removeElement(int[] nums, int val) {
        int k = 0;                       // write index / count of kept elements
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != val) {
                nums[k++] = nums[i];
            }
        }
        return k;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL VARIANT (two pointers from both ends)
     * ------------------------------------------------------------------------
     * Idea:
     *   When occurrences of val are RARE, we'd rather not shift many elements.
     *   Keep a left pointer i and a shrinking size n. If nums[i] == val, swap
     *   in the last element (nums[n-1]) and shrink n (do NOT advance i, since
     *   the swapped-in value still needs checking). Otherwise advance i.
     *
     * This does FEWER writes when val is uncommon (each removal costs one copy
     * regardless of how many keepers follow it), but does NOT preserve order.
     * Great answer to give when the interviewer says "order doesn't matter".
     *
     * Time  : O(n)   -- each element is examined at most once
     * Space : O(1)
     */
    public int removeElementFromEnds(int[] nums, int val) {
        int i = 0;
        int n = nums.length;
        while (i < n) {
            if (nums[i] == val) {
                nums[i] = nums[n - 1];   // pull the last element into this slot
                n--;                     // shrink the logical array; re-check nums[i]
            } else {
                i++;
            }
        }
        return n;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Which of your two O(n) solutions is better?"
     *      -> It depends on the data. The slow/fast pass preserves order and is
     *         simplest. The both-ends swap does fewer WRITES when val is rare
     *         (worst case for slow/fast is many needless copies). If order
     *         doesn't matter and removals are rare, prefer the swap variant.
     *
     *  F2. "Remove ALL duplicates so each remaining value is unique — related?"
     *      -> That's LeetCode 26/80; same slow/fast pointer skeleton but the
     *         'keep' predicate compares against the previously kept element.
     *
     *  F3. "Remove every element that satisfies an arbitrary predicate."
     *      -> Generalize: replace `nums[i] != val` with `predicate.test(nums[i])`.
     *         The two-pointer structure is unchanged.
     *
     *  F4. "What if you must PRESERVE order AND minimize writes?"
     *      -> The slow/fast pass already minimizes writes among order-preserving
     *         solutions: it only writes when a keeper must move left, and never
     *         moves a keeper that's already in place if you guard with i != k.
     *
     *  F5. "The array is huge and lives on disk / is a stream. Now what?"
     *      -> Streaming filter: read elements one at a time, emit those != val.
     *         O(1) memory beyond a buffer; output length is the returned k.
     *
     *  F6. "Micro-optimization: avoid self-copies in the slow/fast version?"
     *      -> Guard the write with `if (i != k)` so you skip writing an element
     *         onto itself. Saves stores when there are long runs of keepers.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against the optimal (order-preserving) solution.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, int val,
                               java.util.function.BiFunction<int[], Integer, Integer> fn) {
        int[] copy = Arrays.copyOf(nums, nums.length);
        int k = fn.apply(copy, val);
        System.out.println(label + " k=" + k
                + ", first k = " + Arrays.toString(Arrays.copyOf(copy, k)));
    }

    public static void main(String[] args) {
        RemoveElement sol = new RemoveElement();

        // Example 1
        report("Example 1 (slow/fast) ->", new int[]{3, 2, 2, 3}, 3, sol::removeElement);
        // Example 2
        report("Example 2 (slow/fast) ->", new int[]{0, 1, 2, 2, 3, 0, 4, 2}, 2, sol::removeElement);

        // Same inputs via the both-ends variant (order may differ, k must match)
        report("Example 1 (both-ends) ->", new int[]{3, 2, 2, 3}, 3, sol::removeElementFromEnds);
        report("Example 2 (both-ends) ->", new int[]{0, 1, 2, 2, 3, 0, 4, 2}, 2, sol::removeElementFromEnds);

        // Edge cases
        report("Empty array        ->", new int[]{}, 5, sol::removeElement);          // k=0
        report("val absent         ->", new int[]{1, 2, 3}, 9, sol::removeElement);    // k=3
        report("all equal val      ->", new int[]{7, 7, 7}, 7, sol::removeElement);    // k=0
    }
}
