/*
 * ============================================================================
 * LeetCode 11. Container With Most Water                     [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an integer array height of length n. There are n vertical lines
 * drawn such that the two endpoints of the i-th line are (i, 0) and
 * (i, height[i]).
 *
 * Find two lines that together with the x-axis form a container, such that the
 * container contains the MOST water.
 *
 * Return the maximum amount of water a container can store.
 *
 * Notice that you may not slant the container.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   n == height.length
 *   2 <= n <= 10^5
 *   0 <= height[i] <= 10^4
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  height = [1,8,6,2,5,4,8,3,7]
 *   Output: 49
 *   Explanation: The lines at index 1 (height 8) and index 8 (height 7) form a
 *                container. Width = 8 - 1 = 7, bounded height = min(8,7) = 7,
 *                area = 7 * 7 = 49. No other pair does better.
 *
 * Example 2:
 *   Input:  height = [1,1]
 *   Output: 1
 *   Explanation: Width 1, height min(1,1) = 1, area = 1.
 *
 * ----------------------------------------------------------------------------
 * INTUITION (the governing formula)
 * ----------------------------------------------------------------------------
 * For lines at indices i < j, the container holds:
 *     area = (j - i) * min(height[i], height[j])
 * The water level is capped by the SHORTER of the two walls (can't slant), and
 * the width is the horizontal distance. We want to maximize this over all pairs.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Area is width * min(two heights) — the shorter wall bounds it?
 *      -> Yes. You can't slant, so the taller wall's excess height is wasted.
 *
 *  Q2. The lines have zero thickness and width is just the index difference?
 *      -> Yes, width = j - i (each unit of index is one unit of horizontal
 *         distance).
 *
 *  Q3. Can heights be 0?
 *      -> Yes; a 0-height wall yields 0 area for any pair including it.
 *
 *  Q4. Return the AREA (a number), not the pair of indices?
 *      -> Correct, just the maximum area.
 *
 *  Q5. Overflow concern on the area?
 *      -> Max area ~ (1e5) * (1e4) = 1e9, which fits in int (< 2.1e9), but I'll
 *         be mindful; a long is a safe habit.
 *
 *  Q6. n >= 2 always, so a container always exists?
 *      -> Yes; there's always at least one pair.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class ContainerWithMostWater {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check every pair of lines)
     * ------------------------------------------------------------------------
     * Idea:
     *   Evaluate area = (j - i) * min(height[i], height[j]) for all pairs i < j
     *   and keep the maximum. Directly encodes the definition.
     *
     * Time  : O(n^2)
     * Space : O(1)
     *
     * Correct but too slow for n up to 1e5. Motivates the two-pointer.
     */
    public int maxAreaBruteForce(int[] height) {
        int best = 0;
        for (int i = 0; i < height.length; i++) {
            for (int j = i + 1; j < height.length; j++) {
                int area = (j - i) * Math.min(height[i], height[j]);
                best = Math.max(best, area);
            }
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (two pointers, move the SHORTER wall inward)
     * ------------------------------------------------------------------------
     * Idea:
     *   Start with the WIDEST possible container: left = 0, right = n - 1.
     *   Compute its area. Then move the pointer at the SHORTER wall inward.
     *
     *   Why move the shorter one? The area is bounded by the shorter wall. If we
     *   moved the TALLER wall inward, width strictly decreases and the height
     *   cap can't increase (still bounded by the same-or-shorter wall), so area
     *   can only shrink. Moving the SHORTER wall is the only move that could
     *   find a taller bound to offset the lost width. So we never miss the
     *   optimum by discarding the shorter wall at its current (widest) position.
     *
     * Time  : O(n)   -- pointers converge, each step drops one line
     * Space : O(1)
     */
    public int maxArea(int[] height) {
        int left = 0, right = height.length - 1;
        int best = 0;
        while (left < right) {
            int h = Math.min(height[left], height[right]);
            int area = (right - left) * h;
            best = Math.max(best, area);
            // Move the shorter wall inward (if equal, moving either is fine).
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Prove moving the shorter wall never skips the optimal pair."
     *      -> Consider the shorter wall at its current position paired with ANY
     *         line between the pointers. Every such pairing has a smaller width
     *         AND is still capped by that same shorter wall, so its area is <=
     *         the current one. Hence the shorter wall can be safely retired at
     *         this width; the optimum involving a taller partner still lies
     *         inside the remaining range.
     *
     *  F2. "When the two walls are EQUAL, does it matter which you move?"
     *      -> No. Any pairing of the wall you keep with an inner line is capped
     *         by <= that equal height and is narrower, so both choices are safe.
     *         (Moving both at once is also valid but unnecessary.)
     *
     *  F3. "How is this DIFFERENT from Trapping Rain Water (LeetCode 42)?"
     *      -> Here we pick TWO lines and the container is between them (ignoring
     *         inner lines). In 42, ALL bars matter and water sits on top of the
     *         terrain; the objective and formula differ, though both use two
     *         pointers.
     *
     *  F4. "Return the actual indices of the best pair, not just the area."
     *      -> Track bestLeft/bestRight whenever `best` improves; return them.
     *
     *  F5. "What if lines had WIDTH (thickness) or non-unit spacing?"
     *      -> Replace width = (j - i) with the actual x-distance from an x-coord
     *         array; the pointer logic is unchanged.
     *
     *  F6. "Could a divide-and-conquer or sorting approach beat O(n)?"
     *      -> No; you must at least look at each line once, so O(n) is optimal.
     *         Sorting would destroy the positional (width) information.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] height, ContainerWithMostWater sol) {
        System.out.println(label + " " + Arrays.toString(height)
                + " -> twoPtr=" + sol.maxArea(height)
                + ", brute=" + sol.maxAreaBruteForce(height));
    }

    public static void main(String[] args) {
        ContainerWithMostWater sol = new ContainerWithMostWater();

        // Examples
        report("Example 1 ->", new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}, sol); // 49
        report("Example 2 ->", new int[]{1, 1}, sol);                       // 1

        // Edge cases
        report("Increasing       ->", new int[]{1, 2, 3, 4, 5}, sol);      // 6  (idx1&4: min(2,5)*3)
        report("Decreasing       ->", new int[]{5, 4, 3, 2, 1}, sol);      // 6
        report("Tall ends        ->", new int[]{9, 1, 1, 1, 9}, sol);      // 36 (min(9,9)*4)
        report("Contains zeros   ->", new int[]{0, 2, 0, 4, 0}, sol);      // 4  (min(2,4)*2)
        report("Flat             ->", new int[]{3, 3, 3, 3}, sol);         // 9  (min*3)
    }
}
