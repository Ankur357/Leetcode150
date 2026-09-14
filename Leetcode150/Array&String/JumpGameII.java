/*
 * ============================================================================
 * LeetCode 45. Jump Game II                                  [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given a 0-indexed array of integers nums of length n. You are
 * initially positioned at nums[0].
 *
 * Each element nums[i] represents the MAXIMUM length of a forward jump from
 * index i. In other words, if you are at index i, you can jump to any index
 * (i + j) where 0 <= j <= nums[i] and i + j < n.
 *
 * Return the MINIMUM number of jumps to reach index n - 1. The test cases are
 * generated such that you CAN always reach the last index.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= nums.length <= 10^4
 *   0 <= nums[i] <= 1000
 *   It is guaranteed that you can reach nums[n - 1].
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [2,3,1,1,4]
 *   Output: 2
 *   Explanation: Jump 1 step from index 0 to 1, then 3 steps to the last index.
 *
 * Example 2:
 *   Input:  nums = [2,3,0,1,4]
 *   Output: 2
 *   Explanation: 0 -> 1 (jump 1), then 1 -> 4 (jump 3). Two jumps total.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Reaching the end is GUARANTEED, so I never handle "impossible"?
 *      -> Correct. Unlike LeetCode 55 (reachability), here I only minimize the
 *         jump count and can assume a path exists.
 *
 *  Q2. nums[i] is the MAX jump, so I may land on any index in [i+1, i+nums[i]]?
 *      -> Yes, any distance from 1..nums[i] (0 stays put and is never useful).
 *
 *  Q3. Do I count the number of JUMPS (edges), not indices visited?
 *      -> Yes. Starting at index 0 costs 0 jumps; each move adds 1.
 *
 *  Q4. Single-element array?
 *      -> n == 1 means we're already at the last index -> 0 jumps.
 *
 *  Q5. Can values be 0 mid-array?
 *      -> Yes (see Example 2). Since reachability is guaranteed, a 0 just means
 *         that index can't be a launch point, but we won't need it to be.
 *
 *  Q6. Any overflow in i + nums[i]?
 *      -> No. n <= 1e4, nums[i] <= 1000; sums stay well within int.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class JumpGameII {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (recursion: min jumps from each index)
     * ------------------------------------------------------------------------
     * Idea:
     *   minJumps(i) = 1 + min over all reachable next indices of minJumps(next),
     *   with minJumps(last) = 0. Explore every branch and take the minimum.
     *
     * Time  : O(k^n) worst case  -- exponential branching (k = avg jump range)
     * Space : O(n)               -- recursion depth
     *
     * Correct but times out. Memoizing on index (Approach 2) makes it O(n^2).
     */
    public int jumpBruteForce(int[] nums) {
        return dfs(nums, 0);
    }

    private int dfs(int[] nums, int pos) {
        if (pos >= nums.length - 1) return 0;      // already at (or past) the end
        int furthest = Math.min(pos + nums[pos], nums.length - 1);
        int best = Integer.MAX_VALUE;
        for (int next = pos + 1; next <= furthest; next++) {
            int sub = dfs(nums, next);
            if (sub != Integer.MAX_VALUE) {
                best = Math.min(best, sub + 1);
            }
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — DP (bottom-up min jumps)  [intermediate step]
     * ------------------------------------------------------------------------
     * Idea:
     *   dp[i] = min jumps to reach the last index from i. Fill from the RIGHT:
     *   dp[n-1] = 0; dp[i] = 1 + min(dp[i+1..i+nums[i]]). Answer is dp[0].
     *
     * Time  : O(n^2)   -- each index scans its whole jump range
     * Space : O(n)     -- the dp array
     *
     * Clear and correct; the greedy below removes the inner scan for O(n).
     */
    public int jumpDP(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[n - 1] = 0;                             // base case
        for (int i = n - 2; i >= 0; i--) {
            int furthest = Math.min(i + nums[i], n - 1);
            for (int j = i + 1; j <= furthest; j++) {
                if (dp[j] != Integer.MAX_VALUE) {
                    dp[i] = Math.min(dp[i], dp[j] + 1);
                }
            }
        }
        return dp[0];
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (greedy BFS "by levels" — implicit breadth-first)
     * ------------------------------------------------------------------------
     * Idea:
     *   Think of it as BFS where each "level" is the set of indices reachable
     *   with the same number of jumps. We DON'T track levels explicitly;
     *   instead we scan once, keeping:
     *       curEnd   = the farthest index reachable with the jumps taken so far
     *                  (the boundary of the current BFS level)
     *       farthest = the farthest index reachable by jumping from anywhere
     *                  within the current level (the boundary of the NEXT level)
     *
     *   As i advances, we relax `farthest`. When i reaches curEnd, we've
     *   exhausted the current level, so we MUST take another jump: increment
     *   jumps and push curEnd out to farthest (open the next level).
     *
     *   We stop scanning at n-1 (we never need to jump FROM the last index).
     *
     *   Why greedy is optimal: within one jump's worth of moves, the reachable
     *   set is exactly [current level start .. farthest]; extending the frontier
     *   as far as possible each level minimizes the number of levels, i.e. jumps.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)
     */
    public int jump(int[] nums) {
        int jumps = 0;
        int curEnd = 0;        // boundary of the current jump level
        int farthest = 0;      // farthest index reachable for the next level
        for (int i = 0; i < nums.length - 1; i++) {   // stop before the last index
            farthest = Math.max(farthest, i + nums[i]);
            if (i == curEnd) {                        // consumed this level
                jumps++;
                curEnd = farthest;                    // advance to the next level
                if (curEnd >= nums.length - 1) break; // last index now reachable
            }
        }
        return jumps;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "What if reaching the end is NOT guaranteed?" (blend with LeetCode 55)
     *      -> Detect the stuck case: if at some point i > farthest, the end is
     *         unreachable -> return -1. Otherwise the same greedy gives the
     *         min jump count.
     *
     *  F2. "Also return the actual sequence of indices you jump to."
     *      -> Within each level, remember the index that produced `farthest`;
     *         that becomes the chosen landing point for the next jump. Record
     *         those to reconstruct the path.
     *
     *  F3. "Prove the greedy equals BFS shortest path."
     *      -> Level L is exactly the set of indices at BFS distance L. curEnd
     *         is level L's max index; farthest is level L+1's max index.
     *         Because reachability within a level is a contiguous prefix, the
     *         greedy visits the same levels as BFS -> same minimal count.
     *
     *  F4. "Jumps cost different amounts (weighted)?"
     *      -> No longer uniform-cost; greedy/BFS breaks. Use Dijkstra or DP over
     *         costs. O(n log n) or O(n * maxJump).
     *
     *  F5. "n is huge but jumps are tiny — can we do better than O(n)?"
     *      -> No: we must at least read every element that could extend the
     *         frontier, so O(n) is optimal for the general case.
     *
     *  F6. "How does this relate to Jump Game (55) and Jump Game III (1306)?"
     *      -> 55 asks reachability (boolean), this asks MIN jumps, 1306 allows
     *         bidirectional jumps to any zero. All are frontier/BFS style; the
     *         state and objective differ.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, JumpGameII sol) {
        System.out.println(label + " " + Arrays.toString(nums)
                + " -> greedy=" + sol.jump(nums)
                + ", dp=" + sol.jumpDP(nums)
                + ", brute=" + sol.jumpBruteForce(nums));
    }

    public static void main(String[] args) {
        JumpGameII sol = new JumpGameII();

        // Examples
        report("Example 1 ->", new int[]{2, 3, 1, 1, 4}, sol);  // 2
        report("Example 2 ->", new int[]{2, 3, 0, 1, 4}, sol);  // 2

        // Edge cases
        report("Single element    ->", new int[]{0}, sol);              // 0 (already at end)
        report("Two elements      ->", new int[]{1, 2}, sol);           // 1
        report("One big jump      ->", new int[]{9, 0, 0, 0, 0}, sol);  // 1
        report("Forced step-by-step ->", new int[]{1, 1, 1, 1}, sol);   // 3
        report("Greedy trap       ->", new int[]{1, 2, 1, 1, 1}, sol);  // 3 (0->1->3->4)
    }
}
