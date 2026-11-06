package Leetcode150.Matrix;
/*
 * ============================================================================
 * LeetCode 54. Spiral Matrix                                  [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an m x n matrix, return ALL elements of the matrix in SPIRAL ORDER
 * (starting from the top-left, going right, then down, then left, then up, and
 * spiraling inward).
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   m == matrix.length
 *   n == matrix[i].length
 *   1 <= m, n <= 10
 *   -100 <= matrix[i][j] <= 100
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  matrix = [[1,2,3],
 *                     [4,5,6],
 *                     [7,8,9]]
 *   Output: [1,2,3,6,9,8,7,4,5]
 *
 * Example 2:
 *   Input:  matrix = [[1, 2, 3, 4],
 *                     [5, 6, 7, 8],
 *                     [9,10,11,12]]
 *   Output: [1,2,3,4,8,12,11,10,9,5,6,7]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Is the matrix always rectangular (every row the same length)?
 *      -> Yes; m x n with m, n >= 1. No jagged rows.
 *
 *  Q2. Can it be non-square (m != n)?
 *      -> Yes — e.g. Example 2 is 3x4. The boundary logic must handle both
 *         wider-than-tall and taller-than-wide.
 *
 *  Q3. Spiral direction — clockwise starting top-left?
 *      -> Yes: right -> down -> left -> up, spiraling inward.
 *
 *  Q4. Can the matrix be empty?
 *      -> Constraints say m, n >= 1, so no; but I'll still guard against null/
 *         empty defensively.
 *
 *  Q5. Return a List<Integer> of exactly m*n elements?
 *      -> Yes, every element appears exactly once, in spiral order.
 *
 *  Q6. May I mutate the input matrix?
 *      -> The boundary approach does NOT need to; I'll avoid mutation so the
 *         caller's data is preserved.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

public class SpiralMatrix {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — LAYER-BY-LAYER BOUNDARIES (top/bottom/left/right) <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     * Track four shrinking boundaries: top, bottom, left, right. Repeatedly
     * walk the perimeter of the current rectangle:
     * 1) left -> right along the TOP row, then top++
     * 2) top -> bottom down the RIGHT column, then right--
     * 3) right -> left along the BOTTOM row (if top <= bottom), then bottom--
     * 4) bottom -> top up the LEFT column (if left <= right), then left++
     * Continue while top <= bottom AND left <= right.
     *
     * The two guards before steps 3 and 4 are ESSENTIAL for non-square
     * matrices: after finishing the top row and right column, a single
     * remaining row or column must not be traversed twice.
     *
     * Time : O(m * n) -- each cell visited exactly once
     * Space : O(1) -- excluding the output list
     */
    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> res = new ArrayList<>();
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
            return res;

        int top = 0, bottom = matrix.length - 1;
        int left = 0, right = matrix[0].length - 1;

        while (top <= bottom && left <= right) {
            // 1) top row, left -> right
            for (int c = left; c <= right; c++)
                res.add(matrix[top][c]);
            top++;

            // 2) right column, top -> bottom
            for (int r = top; r <= bottom; r++)
                res.add(matrix[r][right]);
            right--;

            // 3) bottom row, right -> left (only if a row remains)
            if (top <= bottom) {
                for (int c = right; c >= left; c--)
                    res.add(matrix[bottom][c]);
                bottom--;
            }

            // 4) left column, bottom -> top (only if a column remains)
            if (left <= right) {
                for (int r = bottom; r >= top; r--)
                    res.add(matrix[r][left]);
                left++;
            }
        }
        return res;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — DIRECTION VECTORS + VISITED matrix (simulation)
     * ------------------------------------------------------------------------
     * Idea:
     * Simulate a walker that moves in the current direction until it would
     * step off the grid or onto a visited cell, then turns clockwise. Use
     * direction deltas dr/dc cycling right -> down -> left -> up. Mark cells
     * visited to know when to turn.
     *
     * Time : O(m * n)
     * Space : O(m * n) -- the visited matrix
     *
     * Easier to reason about (no boundary bookkeeping) but uses extra space;
     * good to mention as the "just simulate it" alternative.
     */
    public List<Integer> spiralOrderSimulation(int[][] matrix) {
        List<Integer> res = new ArrayList<>();
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
            return res;

        int m = matrix.length, n = matrix[0].length;
        boolean[][] visited = new boolean[m][n];
        int[] dr = { 0, 1, 0, -1 }; // right, down, left, up
        int[] dc = { 1, 0, -1, 0 };
        int r = 0, c = 0, dir = 0;

        for (int i = 0; i < m * n; i++) {
            res.add(matrix[r][c]);
            visited[r][c] = true;
            int nr = r + dr[dir], nc = c + dc[dir];
            // turn clockwise if the next cell is off-grid or already visited
            if (nr < 0 || nr >= m || nc < 0 || nc >= n || visited[nr][nc]) {
                dir = (dir + 1) % 4;
                nr = r + dr[dir];
                nc = c + dc[dir];
            }
            r = nr;
            c = nc;
        }
        return res;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why the `if (top <= bottom)` / `if (left <= right)` guards?"
     * -> For non-square matrices the inner rectangle can collapse to a
     * single row or column mid-loop. Without the guards, that lone row/
     * column would be traversed a second time (duplicates). The guards
     * confirm a distinct bottom row / left column still exists.
     *
     * F2. "Boundary approach vs. visited-matrix simulation — trade-offs?"
     * -> Same O(m*n) time. Boundaries use O(1) extra space and don't touch
     * the input; simulation is arguably simpler to write but needs an
     * O(m*n) visited grid (or destructive marking of the matrix).
     *
     * F3. "Generate the spiral instead of reading it (LeetCode 59, Spiral II)?"
     * -> Same boundary walk, but WRITE 1..n^2 into the cells as you go
     * rather than read. The control flow is identical.
     *
     * F4. "Counter-clockwise, or start from a different corner?"
     * -> Reorder the four passes (e.g. down -> right -> up -> left) or
     * reverse the direction vectors; the structure is unchanged.
     *
     * F5. "Can you avoid the visited matrix without boundaries?"
     * -> Yes — mark visited cells in-place with a sentinel value (if the
     * value range allows one), trading input mutation for O(1) space.
     * Only valid if mutation is permitted and a safe sentinel exists.
     *
     * F6. "Return the k-th element in spiral order without materializing all?"
     * -> You can still walk the boundaries and stop at the k-th add; the
     * perimeter formula also lets you jump layers, but that's rarely
     * worth the complexity for m, n <= 10.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[][] matrix, SpiralMatrix sol) {
        System.out.println(label
                + " -> boundary=" + sol.spiralOrder(matrix)
                + ", simulation=" + sol.spiralOrderSimulation(matrix));
    }

    public static void main(String[] args) {
        SpiralMatrix sol = new SpiralMatrix();

        // Examples
        report("Example 1 (3x3) ->", new int[][] {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 } }, sol); // [1,2,3,6,9,8,7,4,5]

        report("Example 2 (3x4) ->", new int[][] {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 },
                { 9, 10, 11, 12 } }, sol); // [1,2,3,4,8,12,11,10,9,5,6,7]

        // Edge cases
        report("Single element  ->", new int[][] { { 42 } }, sol); // [42]
        report("Single row      ->", new int[][] { { 1, 2, 3, 4 } }, sol); // [1,2,3,4]
        report("Single column   ->", new int[][] { { 1 }, { 2 }, { 3 } }, sol); // [1,2,3]
        report("Tall 4x2        ->", new int[][] {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 },
                { 7, 8 } }, sol); // [1,2,4,6,8,7,5,3]
        report("Wide 2x4        ->", new int[][] {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 } }, sol); // [1,2,3,4,8,7,6,5]
    }
}
