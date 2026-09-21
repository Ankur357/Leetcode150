
import java.util.Arrays;

public class TrappingRainWater {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (for each bar, scan for left/right maxima)
     * ------------------------------------------------------------------------
     * Idea:
     * For every column i, scan left to find the tallest bar on the left and
     * scan right for the tallest on the right. Water above i is
     * min(leftMax, rightMax) - height[i] (if positive).
     *
     * Time : O(n^2) -- two inner scans per column
     * Space : O(1)
     *
     * Directly encodes the intuition; too slow but a clear starting point.
     */
    public int trapBruteForce(int[] height) {
        int n = height.length;
        long water = 0;
        for (int i = 0; i < n; i++) {
            int leftMax = 0, rightMax = 0;
            for (int l = 0; l <= i; l++)
                leftMax = Math.max(leftMax, height[l]);
            for (int r = i; r < n; r++)
                rightMax = Math.max(rightMax, height[r]);
            water += Math.min(leftMax, rightMax) - height[i];
        }
        return (int) water;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — DP (precompute prefix-max and suffix-max arrays)
     * ------------------------------------------------------------------------
     * Idea:
     * Remove the repeated scanning by precomputing:
     * leftMax[i] = tallest bar in height[0..i]
     * rightMax[i] = tallest bar in height[i..n-1]
     * Then water[i] = min(leftMax[i], rightMax[i]) - height[i], summed.
     *
     * Time : O(n) -- three linear passes
     * Space : O(n) -- the two max arrays
     *
     * This is the natural O(n) answer; the two-pointer trick then drops space.
     */
    public int trapDP(int[] height) {
        int n = height.length;
        if (n == 0)
            return 0;
        int[] leftMax = new int[n];
        int[] rightMax = new int[n];

        leftMax[0] = height[0];
        for (int i = 1; i < n; i++) {
            leftMax[i] = Math.max(leftMax[i - 1], height[i]);
        }
        rightMax[n - 1] = height[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            rightMax[i] = Math.max(rightMax[i + 1], height[i]);
        }
        long water = 0;
        for (int i = 0; i < n; i++) {
            water += Math.min(leftMax[i], rightMax[i]) - height[i];
        }
        return (int) water;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (two pointers, O(1) extra space)
     * ------------------------------------------------------------------------
     * Idea:
     * Keep pointers left/right converging inward, plus leftMax/rightMax seen
     * so far. The key realization: we don't need the EXACT other-side max —
     * only which side is the binding (shorter) wall.
     *
     * If height[left] < height[right], then the left wall is the limiting
     * side for column `left` (there's definitely something >= height[right]
     * on the right, so leftMax is what caps it). So:
     * - update leftMax; water at left += leftMax - height[left]; left++.
     * Otherwise mirror on the right side.
     *
     * Because we always advance the side with the smaller current bar, the
     * max on the FAR side is guaranteed >= the near side's bar, so
     * min(leftMax, rightMax) at the processed column is exactly the near max.
     *
     * Time : O(n) -- each index visited once
     * Space : O(1) -- four scalars
     */
    public int trap(int[] height) {
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        long water = 0;
        while (left < right) {
            if (height[left] < height[right]) {
                // Left bar is the shorter wall -> left side bounds the water here.
                leftMax = Math.max(leftMax, height[left]);
                water += leftMax - height[left];
                left++;
            } else {
                // Right bar is the shorter (or equal) wall -> right side bounds it.
                rightMax = Math.max(rightMax, height[right]);
                water += rightMax - height[right];
                right--;
            }
        }
        return (int) water;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why can the two-pointer method use leftMax without knowing the true
     * rightMax?"
     * -> We only move the smaller side. When height[left] < height[right],
     * some bar on the right is >= height[right] > height[left], so the
     * right wall can't be the limiting one for column left; leftMax
     * alone determines the water. Symmetric on the other side.
     *
     * F2. "Solve it with a monotonic STACK instead."
     * -> Maintain a decreasing stack of indices. When a taller bar arrives,
     * pop and fill the horizontal 'basin' it closes:
     * width * (min(left, cur) - popped height). O(n) time, O(n) space.
     * Nice for computing water layer-by-layer.
     *
     * F3. "Trapping Rain Water II — a 2D grid." (LeetCode 407)
     * -> Min-heap from the border inward (Dijkstra-like): the water level
     * at a cell is bounded by the lowest surrounding wall. O(mn log mn).
     *
     * F4. "Also return WHERE the water sits (per-column amounts)."
     * -> Emit min(leftMax[i], rightMax[i]) - height[i] per column instead
     * of summing; the DP arrays make this trivial.
     *
     * F5. "Prove the DP formula is correct."
     * -> Water above i rises until it would spill over the lower of the two
     * tallest flanking walls; that level is min(leftMax[i], rightMax[i]).
     * Subtract the bar itself; negatives clamp to 0 (they can't, since
     * leftMax[i] >= height[i] always).
     *
     * F6. "Streaming heights, can't random-access — approach?"
     * -> The monotonic stack processes left-to-right online; or if only a
     * running total is needed, buffer minimally. Two-pointer needs both
     * ends so it doesn't stream from one side.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] height, TrappingRainWater sol) {
        System.out.println(label + " " + Arrays.toString(height)
                + " -> twoPtr=" + sol.trap(height)
                + ", dp=" + sol.trapDP(height)
                + ", brute=" + sol.trapBruteForce(height));
    }

    public static void main(String[] args) {
        TrappingRainWater sol = new TrappingRainWater();

        // Examples
        report("Example 1 ->", new int[] { 0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1 }, sol); // 6
        report("Example 2 ->", new int[] { 4, 2, 0, 3, 2, 5 }, sol); // 9

        // Edge cases
        report("Fewer than 3 bars ->", new int[] { 2, 1 }, sol); // 0
        report("Monotonic up      ->", new int[] { 1, 2, 3, 4 }, sol); // 0
        report("Monotonic down    ->", new int[] { 4, 3, 2, 1 }, sol); // 0
        report("Single deep well  ->", new int[] { 5, 0, 0, 0, 5 }, sol); // 15
        report("Flat              ->", new int[] { 3, 3, 3 }, sol); // 0
        report("V shape           ->", new int[] { 3, 0, 3 }, sol); // 3
    }
}
