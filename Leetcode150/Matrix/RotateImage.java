package Leetcode150.Matrix;
/*
 * ============================================================================
 * LeetCode 48. Rotate Image                                   [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an n x n 2D matrix representing an image. Rotate the image by
 * 90 degrees (CLOCKWISE).
 *
 * You have to rotate the image IN-PLACE, which means you have to modify the
 * input 2D matrix directly. DO NOT allocate another 2D matrix and do the
 * rotation.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   n == matrix.length == matrix[i].length
 *   1 <= n <= 20
 *   -1000 <= matrix[i][j] <= 1000
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  [[1,2,3],
 *            [4,5,6],
 *            [7,8,9]]
 *   Output: [[7,4,1],
 *            [8,5,2],
 *            [9,6,3]]
 *
 * Example 2:
 *   Input:  [[ 5, 1, 9,11],
 *            [ 2, 4, 8,10],
 *            [13, 3, 6, 7],
 *            [15,14,12,16]]
 *   Output: [[15,13, 2, 5],
 *            [14, 3, 4, 1],
 *            [12, 6, 8, 9],
 *            [16, 7,10,11]]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Rotation direction — clockwise or counter-clockwise?
 *      -> Clockwise (90 degrees). Element at (r, c) moves to (c, n-1-r).
 *
 *  Q2. Must it be strictly IN-PLACE (O(1) extra space)?
 *      -> Yes, the problem forbids allocating another n x n matrix. So no full
 *         copy — only swaps.
 *
 *  Q3. Is the matrix always square (n x n)?
 *      -> Yes. In-place rotation is only well-defined for square matrices;
 *         a non-square rotation changes dimensions and can't be done in place.
 *
 *  Q4. Return value or void?
 *      -> Void — the caller reads back the mutated matrix. (I'll return it too
 *         from a helper only for easy testing/printing.)
 *
 *  Q5. Can n be 1?
 *      -> Yes; a 1x1 matrix rotates to itself (no-op). Handled naturally.
 *
 *  Q6. Values can repeat / be negative?
 *      -> Yes; irrelevant to the index math, which just moves cells around.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class RotateImage {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — TRANSPOSE + REVERSE EACH ROW <-- the clean, preferred answer
     * ------------------------------------------------------------------------
     * Idea:
     * A 90-degree CLOCKWISE rotation equals:
     * (a) TRANSPOSE the matrix (swap matrix[i][j] with matrix[j][i]), then
     * (b) REVERSE each row.
     *
     * Why it works: transpose sends (i, j) -> (j, i); reversing a row sends
     * (i, j) -> (i, n-1-j). Composing, (i, j) -> (i, n-1-j) after the value
     * originally at (j, i) landed at (i, j)... the net effect maps the original
     * (r, c) to (c, n-1-r), which is exactly the clockwise rotation.
     *
     * Both steps are in-place swaps -> O(1) extra space.
     *
     * Time : O(n^2) -- touches each cell a constant number of times
     * Space : O(1)
     */
    public void rotate(int[][] matrix) {
        int n = matrix.length;

        // (a) Transpose: swap across the main diagonal. Iterate j > i only so
        // each pair is swapped exactly once (i == j stays put).
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int tmp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = tmp;
            }
        }

        // (b) Reverse each row.
        for (int i = 0; i < n; i++) {
            int lo = 0, hi = n - 1;
            while (lo < hi) {
                int tmp = matrix[i][lo];
                matrix[i][lo] = matrix[i][hi];
                matrix[i][hi] = tmp;
                lo++;
                hi--;
            }
        }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — LAYER-BY-LAYER 4-WAY CYCLIC SWAP (rotate rings directly)
     * ------------------------------------------------------------------------
     * Idea:
     * Rotate the matrix ring by ring (outermost layer inward). For each layer,
     * walk the positions along the top edge and rotate four cells at a time in
     * a single cycle:
     * top-left <- bottom-left
     * bottom-left <- bottom-right
     * bottom-right <- top-right
     * top-right <- top-left (saved)
     * Using temp for one corner, we shift all four with one extra variable.
     *
     * The clockwise mapping used per cell: (r, c) receives the value from
     * (n-1-c, r).
     *
     * Time : O(n^2)
     * Space : O(1)
     *
     * Does the rotation in a single pass without the intermediate transpose;
     * slightly trickier index math but a nice "show me you understand the
     * geometry" alternative.
     */
    public void rotateCyclic(int[][] matrix) {
        int n = matrix.length;
        for (int layer = 0; layer < n / 2; layer++) {
            int first = layer;
            int last = n - 1 - layer;
            for (int i = first; i < last; i++) {
                int offset = i - first;
                int top = matrix[first][i]; // save top

                // top <- left
                matrix[first][i] = matrix[last - offset][first];
                // left <- bottom
                matrix[last - offset][first] = matrix[last][last - offset];
                // bottom <- right
                matrix[last][last - offset] = matrix[i][last];
                // right <- top (saved)
                matrix[i][last] = top;
            }
        }
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Prove transpose + row-reverse == 90-degree clockwise."
     * -> Transpose: (r, c) -> (c, r). Then reverse each row: (c, r) ->
     * (c, n-1-r). Net: original (r, c) ends at (c, n-1-r) — the exact
     * clockwise destination. (For COUNTER-clockwise: transpose then
     * reverse each COLUMN, or reverse rows first then transpose.)
     *
     * F2. "Rotate COUNTER-clockwise instead?"
     * -> Transpose then reverse each COLUMN (i.e., reverse the order of the
     * rows), or equivalently reverse each row THEN transpose.
     *
     * F3. "Rotate 180 degrees?"
     * -> Reverse the row order AND reverse each row (two reversals), or run
     * the 90-degree rotation twice.
     *
     * F4. "Why is this only possible in-place for a SQUARE matrix?"
     * -> A 90-degree rotation of an m x n matrix yields n x m dimensions;
     * when m != n the shape changes, so you can't reuse the same storage
     * cell-for-cell. Square keeps dimensions fixed.
     *
     * F5. "Transpose+reverse vs. 4-way cyclic swap — trade-offs?"
     * -> Both O(n^2)/O(1). Transpose+reverse is far easier to read and less
     * error-prone; the cyclic version does fewer total writes (one pass)
     * and avoids a second sweep, which can matter for very large n.
     *
     * F6. "What if we could use extra space?"
     * -> Trivial: allocate out[c][n-1-r] = matrix[r][c] in one pass, O(n^2)
     * time and O(n^2) space. The problem forbids this to force the
     * in-place insight.
     */

    // ------------------------------------------------------------------------
    // Driver: rotates DEEP COPIES with each approach so both start fresh.
    // ------------------------------------------------------------------------
    private static int[][] copy(int[][] m) {
        int[][] c = new int[m.length][];
        for (int i = 0; i < m.length; i++)
            c[i] = Arrays.copyOf(m[i], m[i].length);
        return c;
    }

    private static void report(String label, int[][] matrix, RotateImage sol) {
        int[][] a = copy(matrix);
        int[][] b = copy(matrix);
        sol.rotate(a);
        sol.rotateCyclic(b);
        System.out.println(label);
        System.out.println("   transpose+reverse -> " + Arrays.deepToString(a));
        System.out.println("   cyclic 4-way swap -> " + Arrays.deepToString(b));
    }

    public static void main(String[] args) {
        RotateImage sol = new RotateImage();

        // Examples
        report("Example 1 (3x3) ->", new int[][] {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 } }, sol);
        // Expected: [[7,4,1],[8,5,2],[9,6,3]]

        report("Example 2 (4x4) ->", new int[][] {
                { 5, 1, 9, 11 },
                { 2, 4, 8, 10 },
                { 13, 3, 6, 7 },
                { 15, 14, 12, 16 } }, sol);
        // Expected: [[15,13,2,5],[14,3,4,1],[12,6,8,9],[16,7,10,11]]

        // Edge cases
        report("Single element  ->", new int[][] { { 42 } }, sol); // [[42]]
        report("2x2             ->", new int[][] { { 1, 2 }, { 3, 4 } }, sol); // [[3,1],[4,2]]
    }
}
