/*
 * ============================================================================
 * LeetCode 239. Sliding Window Maximum                          [Difficulty: Hard]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an array of integers nums, and there is a sliding window of
 * size k which is moving from the very left of the array to the very right.
 * You can only see the k numbers in the window. Each time the sliding window
 * moves right by one position.
 *
 * Return the MAX in each window, as an array (one value per window position).
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= nums.length <= 10^5
 *   -10^4 <= nums[i] <= 10^4
 *   1 <= k <= nums.length
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [1,3,-1,-3,5,3,6,7], k = 3
 *   Output: [3,3,5,5,6,7]
 *   Explanation:
 *     Window position          Max
 *     ---------------          ---
 *     [1  3  -1] -3  5 ...       3
 *      1 [3  -1  -3] 5 ...       3
 *      1  3 [-1  -3  5] ...      5
 *      1  3  -1 [-3  5  3] ...   5
 *      ...        [5  3  6]      6
 *      ...           [3  6  7]   7
 *
 * Example 2:
 *   Input:  nums = [1], k = 1
 *   Output: [1]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Is k always valid (1 <= k <= n)?
 *      -> Yes per constraints, so the number of windows is exactly n - k + 1.
 *
 *  Q2. Can values be negative / duplicated?
 *      -> Yes (range includes negatives, duplicates allowed). The deque logic
 *         must handle both — I store indices and compare by value.
 *
 *  Q3. Output size?
 *      -> n - k + 1 maxima, in left-to-right window order.
 *
 *  Q4. Do we return values or indices of the maxima?
 *      -> The max VALUES. (Storing indices internally is a means, not the output.)
 *
 *  Q5. Any expectation on time complexity?
 *      -> With n up to 1e5, aim for O(n). A per-window scan is O(n*k) and may be
 *         too slow; the monotonic deque gives O(n).
 *
 *  Q6. Ties in the window — does it matter which index is "the" max?
 *      -> No, we only report the value; equal values are handled consistently
 *         by the deque's pop rule.
 *
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;

public class SlidingWindowMaximum {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (scan each window)
     * ------------------------------------------------------------------------
     * Idea:
     *   For each of the n - k + 1 window start positions, scan the k elements
     *   and take the max.
     *
     * Time  : O(n * k)
     * Space : O(1) extra (besides the output)
     *
     * Simple; too slow when n and k are both large. Monotonic deque fixes it.
     */
    public int[] maxSlidingWindowBruteForce(int[] nums, int k) {
        int n = nums.length;
        int[] res = new int[n - k + 1];
        for (int i = 0; i + k <= n; i++) {
            int mx = nums[i];
            for (int j = i + 1; j < i + k; j++) {
                mx = Math.max(mx, nums[j]);
            }
            res[i] = mx;
        }
        return res;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (monotonic decreasing deque of indices)  <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Maintain a deque of INDICES whose corresponding values are in DECREASING
     *   order. The front always holds the index of the current window's maximum.
     *
     *   For each new index i:
     *     1) Pop from the BACK while nums[back] <= nums[i]: those elements can
     *        never be the max while nums[i] is in the window (i is newer and >=),
     *        so they're useless.
     *     2) Push i to the back.
     *     3) Pop from the FRONT if it's out of the window (front <= i - k).
     *     4) Once the first full window is formed (i >= k - 1), the front index
     *        is the max of the current window -> record nums[front].
     *
     *   Each index is pushed once and popped at most once -> O(n) total.
     *
     * Time  : O(n)
     * Space : O(k)  -- deque holds at most k indices
     */
    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] res = new int[n - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();   // indices, values decreasing front->back

        for (int i = 0; i < n; i++) {
            // (1) drop smaller/equal values from the back — they can't be max anymore
            while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i]) {
                dq.pollLast();
            }
            // (2) add current index
            dq.offerLast(i);
            // (3) evict the front if it has slid out of the window
            if (dq.peekFirst() <= i - k) {
                dq.pollFirst();
            }
            // (4) record the max once the window is fully formed
            if (i >= k - 1) {
                res[i - k + 1] = nums[dq.peekFirst()];
            }
        }
        return res;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — Max-heap of (value, index)  (alternative)
     * ------------------------------------------------------------------------
     * Idea:
     *   Push (value, index) into a max-heap keyed by value. For each window, peek
     *   the top and lazily discard any top whose index has fallen out of the
     *   window (index <= i - k) before reading the max.
     *
     * Time  : O(n log n)   -- each element pushed once; lazy pops amortized
     * Space : O(n)         -- heap can hold up to n entries before eviction
     *
     * Simpler to reason about than the deque, but log-factor slower and more
     * memory. Good to mention as a fallback / when a running max structure is
     * already in use.
     */
    public int[] maxSlidingWindowHeap(int[] nums, int k) {
        int n = nums.length;
        int[] res = new int[n - k + 1];
        // max-heap by value; tie-break irrelevant. Entry = {value, index}.
        PriorityQueue<int[]> heap =
                new PriorityQueue<>((a, b) -> b[0] - a[0]);

        for (int i = 0; i < n; i++) {
            heap.offer(new int[]{nums[i], i});
            if (i >= k - 1) {
                // discard maxima that are outside the current window [i-k+1, i]
                while (heap.peek()[1] <= i - k) {
                    heap.poll();
                }
                res[i - k + 1] = heap.peek()[0];
            }
        }
        return res;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why is the deque approach O(n) despite the inner while-loop?"
     *      -> Amortized analysis: every index is added to the deque exactly once
     *         and removed at most once. The total number of pops across the whole
     *         run is <= n, so the inner loop does O(n) work in aggregate.
     *
     *  F2. "Why store INDICES in the deque, not values?"
     *      -> We need to know when the current max has slid out of the window,
     *         which requires its position. Values alone can't tell you that
     *         (and duplicates would be ambiguous).
     *
     *  F3. "Why pop the back on `<=` (equal) rather than strict `<`?"
     *      -> Popping equal values too keeps the deque smaller and is safe: a
     *         newer equal element stays in the window at least as long, so the
     *         older equal one is never needed. Strict `<` also works but keeps
     *         redundant duplicates.
     *
     *  F4. "Sliding window MINIMUM instead?"
     *      -> Symmetric: keep an INCREASING deque and pop the back while
     *         nums[back] >= nums[i]; front is the min.
     *
     *  F5. "What if k changes / the window is variable-size?"
     *      -> The deque still gives the max/min over the current [left, right];
     *         it underpins problems like 'Longest subarray with max-min <= limit'
     *         (LC 1438) using two monotonic deques (one max, one min).
     *
     *  F6. "Streaming data (infinite nums), report max of last k seen?"
     *      -> The deque adapts directly: it only ever references indices in the
     *         last k, so you can keep it bounded and process one element at a time.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, int k, SlidingWindowMaximum sol) {
        System.out.println(label + " nums=" + java.util.Arrays.toString(nums) + ", k=" + k
                + " -> deque=" + java.util.Arrays.toString(sol.maxSlidingWindow(nums, k))
                + ", heap=" + java.util.Arrays.toString(sol.maxSlidingWindowHeap(nums, k))
                + ", brute=" + java.util.Arrays.toString(sol.maxSlidingWindowBruteForce(nums, k)));
    }

    public static void main(String[] args) {
        SlidingWindowMaximum sol = new SlidingWindowMaximum();

        // Examples
        report("Example 1 ->", new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3, sol);  // [3,3,5,5,6,7]
        report("Example 2 ->", new int[]{1}, 1, sol);                         // [1]

        // Edge cases
        report("k == n            ->", new int[]{9, 2, 7, 4}, 4, sol);        // [9]
        report("k == 1            ->", new int[]{4, 3, 2, 1}, 1, sol);        // [4,3,2,1]
        report("All equal         ->", new int[]{5, 5, 5, 5}, 2, sol);        // [5,5,5]
        report("Decreasing        ->", new int[]{8, 6, 4, 2}, 2, sol);        // [8,6,4] (front evicts)
        report("Increasing        ->", new int[]{1, 2, 3, 4}, 2, sol);        // [2,3,4] (back pops)
        report("With negatives    ->", new int[]{-7, -8, 7, 5, 7, 1, 6, 0}, 4, sol); // [7,7,7,7,7]
        report("Max at boundaries ->", new int[]{9, 1, 1, 9, 1, 1, 9}, 3, sol);      // [9,9,9,9,9]
    }
}
