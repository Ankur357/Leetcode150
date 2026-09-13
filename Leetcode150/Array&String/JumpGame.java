/*
 * ============================================================================
 * LeetCode 55. Jump Game                                     [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an integer array nums. You are initially positioned at the
 * array's FIRST index, and each element nums[i] represents your MAXIMUM jump
 * length at that position.
 *
 * Return true if you can reach the LAST index, or false otherwise.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= nums.length <= 10^4
 *   0 <= nums[i] <= 10^5
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [2,3,1,1,4]
 *   Output: true
 *   Explanation: Jump 1 step from index 0 to 1, then 3 steps to the last index.
 *
 * Example 2:
 *   Input:  nums = [3,2,1,0,4]
 *   Output: false
 *   Explanation: You will always arrive at index 3 (its value is 0). No matter
 *                how you jump, index 3 is a dead end, so the last index is
 *                unreachable.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. nums[i] is the MAX jump, so I may jump any distance from 1..nums[i]?
 *      -> Yes. A value of 3 means I can advance 1, 2, or 3 indices.
 *
 *  Q2. Can a value be 0?
 *      -> Yes. A 0 is a potential dead end — you can't move forward FROM it
 *         (though you may still land ON it and be stuck).
 *
 *  Q3. Do I only move to the RIGHT (forward)?
 *      -> Yes, jumps are forward-only toward higher indices.
 *
 *  Q4. Is the array guaranteed non-empty?
 *      -> Yes, length >= 1. A single-element array is already at the last
 *         index -> trivially true.
 *
 *  Q5. Do I need the MINIMUM number of jumps, or just reachability?
 *      -> Just reachability here (boolean). Min jumps is LeetCode 45.
 *
 *  Q6. Any overflow concern adding index + nums[i]?
 *      -> Values up to 1e5 and n up to 1e4; sums stay well within int range.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class JumpGame {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (recursion / backtracking over all jumps)
     * ------------------------------------------------------------------------
     * Idea:
     *   From index i, try every jump length 1..nums[i] and recurse. Return true
     *   if any path reaches the last index. Explores the full decision tree.
     *
     * Time  : O(2^n) worst case  -- exponential branching, revisits positions
     * Space : O(n)               -- recursion depth
     *
     * Correct but times out. Memoizing per index (Approach 2) makes it O(n^2).
     */
    public boolean canJumpBruteForce(int[] nums) {
        return dfs(nums, 0);
    }

    private boolean dfs(int[] nums, int pos) {
        if (pos >= nums.length - 1) return true;   // reached (or passed) the end
        int furthest = Math.min(pos + nums[pos], nums.length - 1);
        for (int next = pos + 1; next <= furthest; next++) {
            if (dfs(nums, next)) return true;
        }
        return false;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — DP (memoized reachability)  [intermediate step]
     * ------------------------------------------------------------------------
     * Idea:
     *   good[i] = can we reach the last index starting from i? Fill from the
     *   RIGHT: the last index is trivially good; index i is good if any
     *   reachable neighbor within its jump range is good.
     *
     * Time  : O(n^2)   -- each index scans up to n neighbors
     * Space : O(n)     -- the memo array
     *
     * Better than exponential, but the greedy below drops it to linear.
     */
    public boolean canJumpDP(int[] nums) {
        int n = nums.length;
        boolean[] good = new boolean[n];
        good[n - 1] = true;                          // base case: already at end
        for (int i = n - 2; i >= 0; i--) {
            int furthest = Math.min(i + nums[i], n - 1);
            for (int j = i + 1; j <= furthest; j++) {
                if (good[j]) { good[i] = true; break; }
            }
        }
        return good[0];
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (greedy: track the furthest reachable index)
     * ------------------------------------------------------------------------
     * Idea:
     *   Sweep left to right maintaining `reach` = the furthest index reachable
     *   so far. At each index i:
     *     - If i > reach, we can never step onto i (a gap/dead end) -> false.
     *     - Otherwise extend reach = max(reach, i + nums[i]).
     *   If reach ever covers the last index, return true.
     *
     *   Why greedy works: reachability is "downward closed" — if you can reach
     *   index r, you can reach everything in [0, r]. So a single running max of
     *   the frontier is all the state we need; there's no benefit to remembering
     *   HOW we got somewhere, only how far we can get.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)
     */
    public boolean canJump(int[] nums) {
        int reach = 0;                               // furthest index reachable so far
        for (int i = 0; i < nums.length; i++) {
            if (i > reach) return false;             // stuck before index i
            reach = Math.max(reach, i + nums[i]);
            if (reach >= nums.length - 1) return true; // last index within reach
        }
        return true;                                 // covers single-element array
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Return the MINIMUM number of jumps to reach the end." (LeetCode 45)
     *      -> Greedy BFS-by-levels: track current jump's end (`curEnd`) and the
     *         `farthest` reachable within it; increment jumps when i hits
     *         curEnd. O(n) time, O(1) space.
     *
     *  F2. "A backward variant — greedy from the RIGHT?"
     *      -> Track the leftmost 'good' index (start = n-1). Moving left, if
     *         i + nums[i] >= goodStart, set goodStart = i. Answer: goodStart == 0.
     *         Also O(n)/O(1).
     *
     *  F3. "What if you can also jump BACKWARD?"
     *      -> Reachability becomes a graph connectivity problem; use BFS/DFS or
     *         union-find over the implied edges. O(n * maxJump) worst case.
     *
     *  F4. "Jump Game III: from index i you may go i+nums[i] or i-nums[i], reach
     *       ANY index with value 0." (LeetCode 1306)
     *      -> BFS/DFS from the start over those two moves, mark visited. O(n).
     *
     *  F5. "Why is greedy correct — no need to try shorter jumps?"
     *      -> Because reachability is monotone: extending `reach` never removes
     *         a previously reachable index. The max frontier dominates every
     *         individual path, so tracking it alone is sufficient.
     *
     *  F6. "Early-exit vs. full scan — does it matter for complexity?"
     *      -> Not asymptotically (both O(n)), but returning as soon as
     *         reach >= n-1 avoids a needless tail scan in practice.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, JumpGame sol) {
        System.out.println(label + " " + Arrays.toString(nums)
                + " -> greedy=" + sol.canJump(nums)
                + ", dp=" + sol.canJumpDP(nums)
                + ", brute=" + sol.canJumpBruteForce(nums));
    }

    public static void main(String[] args) {
        JumpGame sol = new JumpGame();

        // Examples
        report("Example 1 ->", new int[]{2, 3, 1, 1, 4}, sol);  // true
        report("Example 2 ->", new int[]{3, 2, 1, 0, 4}, sol);  // false

        // Edge cases
        report("Single element     ->", new int[]{0}, sol);            // true (already at end)
        report("Leading zero stuck ->", new int[]{0, 1}, sol);         // false
        report("Big first jump     ->", new int[]{5, 0, 0, 0, 0}, sol);// true
        report("Zero at last index ->", new int[]{2, 0, 0}, sol);      // true (land on end)
        report("Just barely makes  ->", new int[]{1, 1, 1, 1}, sol);   // true
    }
}
