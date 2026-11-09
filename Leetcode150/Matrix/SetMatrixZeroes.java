package Leetcode150.Matrix;
/*
 * ============================================================================
 * LeetCode 73. Set Matrix Zeroes                              [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an m x n integer matrix, if an element is 0, set its ENTIRE ROW and
 * ENTIRE COLUMN to 0's. You must do it IN PLACE.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   m == matrix.length
 *   n == matrix[0].length
 *   1 <= m, n <= 200
 *   -2^31 <= matrix[i][j] <= 2^31 - 1
 *
 * Follow-up:
 *   - A straightforward solution uses O(m*n) space (probably a bad idea).
 *   - A simple improvement uses O(m + n) space.
 *   - Could you devise a CONSTANT O(1) space solution?
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  [[1,1,1],
 *            [1,0,1],
 *            [1,1,1]]
 *   Output: [[1,0,1],
 *            [0,0,0],
 *            [1,0,1]]
 *
 * Example 2:
 *   Input:  [[0,1,2,0],
 *            [3,4,5,2],
 *            [1,3,1,5]]
 *   Output: [[0,0,0,0],
 *            [0,4,5,0],
 *            [0,3,1,0]]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Must the zeroing be IN PLACE?
 *      -> Yes. And the follow-up pushes for O(1) EXTRA space (beyond the input).
 *
 *  Q2. The trap: if I zero a row/column as I scan, will new zeros trigger MORE
 *      zeroing?
 *      -> Yes — that's the core pitfall. I must first RECORD which original
 *         cells are zero, then apply the zeroing in a second phase. Otherwise a
 *         zero I write gets mistaken for an original zero and cascades.
 *
 *  Q3. Are values bounded so I could use a sentinel instead of marker arrays?
 *      -> Values span the full int range, so there is NO safe unused sentinel
 *         value. That's why the O(1) trick reuses the matrix's own first
 *         row/column as marker storage rather than a magic number.
 *
 *  Q4. Rectangular (not jagged), m, n >= 1?
 *      -> Yes; every row has length n and there's at least one cell.
 *
 *  Q5. Return value?
 *      -> Void; mutate the matrix in place. (Helper returns it for testing.)
 *
 *  Q6. Can the whole matrix already be zero, or have no zeros at all?
 *      -> Both possible; handled naturally (no-op when there are no zeros).
 *
 * ============================================================================
 */

import java.util.Arrays;

public class SetMatrixZeroes {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — O(m + n) SPACE (row & column marker arrays) [clean baseline]
     * ------------------------------------------------------------------------
     * Idea:
     * Pass 1: record every row and column that contains an original zero into
     * two boolean arrays.
     * Pass 2: zero any cell whose row OR column was flagged.
     * Recording first avoids the cascade problem in Q2.
     *
     * Time : O(m * n)
     * Space : O(m + n)
     */
    public void setZeroesMarkerArrays(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        boolean[] zeroRow = new boolean[m];
        boolean[] zeroCol = new boolean[n];

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (matrix[r][c] == 0) {
                    zeroRow[r] = true;
                    zeroCol[c] = true;
                }
            }
        }

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (zeroRow[r] || zeroCol[c])
                    matrix[r][c] = 0;
            }
        }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL O(1) SPACE (use first row/col as markers) <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     * Reuse the matrix's OWN first row and first column as the boolean marker
     * storage from Approach 1:
     * - matrix[r][0] == 0 will mean "row r must be zeroed"
     * - matrix[0][c] == 0 will mean "column c must be zeroed"
     * But the first row and first column also hold real data and overlap at
     * matrix[0][0], so we track THEIR own zero-status in two scalars first.
     *
     * Steps:
     * 1) firstRowZero / firstColZero = does row 0 / col 0 contain a zero?
     * 2) For the inner cells (r,c >= 1): if matrix[r][c] == 0, mark
     * matrix[r][0] = 0 and matrix[0][c] = 0.
     * 3) For inner cells: zero matrix[r][c] if its row marker matrix[r][0]
     * or column marker matrix[0][c] is 0.
     * 4) Finally, if firstRowZero, zero the whole first row; if firstColZero,
     * zero the whole first column. (Done LAST so the markers survive
     * through step 3.)
     *
     * Time : O(m * n)
     * Space : O(1)
     */
    public void setZeroes(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;

        // 1) Does the first row / first column originally contain a zero?
        boolean firstRowZero = false, firstColZero = false;
        for (int c = 0; c < n; c++)
            if (matrix[0][c] == 0) {
                firstRowZero = true;
                break;
            }
        for (int r = 0; r < m; r++)
            if (matrix[r][0] == 0) {
                firstColZero = true;
                break;
            }

        // 2) Use first row/col as markers for the inner submatrix.
        for (int r = 1; r < m; r++) {
            for (int c = 1; c < n; c++) {
                if (matrix[r][c] == 0) {
                    matrix[r][0] = 0; // mark this row
                    matrix[0][c] = 0; // mark this column
                }
            }
        }

        // 3) Zero inner cells based on the markers.
        for (int r = 1; r < m; r++) {
            for (int c = 1; c < n; c++) {
                if (matrix[r][0] == 0 || matrix[0][c] == 0)
                    matrix[r][c] = 0;
            }
        }

        // 4) Handle the first row and first column LAST (their markers are done).
        if (firstRowZero) {
            for (int c = 0; c < n; c++)
                matrix[0][c] = 0;
        }
        if (firstColZero) {
            for (int r = 0; r < m; r++)
                matrix[r][0] = 0;
        }
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why capture firstRowZero / firstColZero BEFORE using them as markers?"
     * -> matrix[0][0] is shared by both the row-0 and col-0 markers, so a
     * single flag can't distinguish them. Two separate scalars record
     * the first row's and first column's original zero-status so we can
     * zero them correctly at the end without corrupting the markers used
     * for the inner cells.
     *
     * F2. "Why zero the first row/column LAST?"
     * -> The first row/col hold the markers for every inner row/col. If we
     * zeroed them early, we'd wipe the markers and lose the information
     * needed in step 3. Applying them last preserves the markers.
     *
     * F3. "Why not just use a sentinel like Integer.MIN_VALUE to mark cells?"
     * -> Values span the full int range, so any sentinel could collide with
     * real data and cause false zeros. Reusing the first row/col avoids
     * needing an unused value.
     *
     * F4. "The naive approach zeros as it scans — what breaks?"
     * -> A zero written for one original zero would be read as 'original'
     * later, cascading and zeroing rows/columns that shouldn't be. You
     * must separate DETECTION from MUTATION.
     *
     * F5. "Space trade-offs across the three approaches?"
     * -> O(m*n): copy the matrix (wasteful). O(m+n): two boolean arrays
     * (clean, easy). O(1): first-row/col markers (optimal, trickier).
     * All are O(m*n) time.
     *
     * F6. "Concurrency / very large matrix that doesn't fit in memory?"
     * -> Detection pass could stream row indices and column indices of
     * zeros to compact sets (O(m+n)); the mutation pass then streams
     * again. The O(1) trick needs random access to the first row/col.
     */

    // ------------------------------------------------------------------------
    // Driver: applies both approaches to independent DEEP COPIES of the input.
    // ------------------------------------------------------------------------
    private static int[][] copy(int[][] m) {
        int[][] c = new int[m.length][];
        for (int i = 0; i < m.length; i++)
            c[i] = Arrays.copyOf(m[i], m[i].length);
        return c;
    }

    private static void report(String label, int[][] matrix, SetMatrixZeroes sol) {
        int[][] a = copy(matrix);
        int[][] b = copy(matrix);
        sol.setZeroes(a); // O(1) space
        sol.setZeroesMarkerArrays(b); // O(m+n) space
        System.out.println(label);
        System.out.println("   O(1) markers   -> " + Arrays.deepToString(a));
        System.out.println("   O(m+n) markers -> " + Arrays.deepToString(b));
    }

    public static void main(String[] args) {
        SetMatrixZeroes sol = new SetMatrixZeroes();

        // Examples
        report("Example 1 ->", new int[][] {
                { 1, 1, 1 },
                { 1, 0, 1 },
                { 1, 1, 1 } }, sol);
        // Expected: [[1,0,1],[0,0,0],[1,0,1]]

        report("Example 2 ->", new int[][] {
                { 0, 1, 2, 0 },
                { 3, 4, 5, 2 },
                { 1, 3, 1, 5 } }, sol);
        // Expected: [[0,0,0,0],[0,4,5,0],[0,3,1,0]]

        // Edge cases
        report("No zeros        ->", new int[][] {
                { 1, 2 },
                { 3, 4 } }, sol); // unchanged

        report("Zero at (0,0)   ->", new int[][] {
                { 0, 2, 3 },
                { 4, 5, 6 } }, sol); // first row & col zeroed

        report("Single zero cell->", new int[][] { { 0 } }, sol); // [[0]]
        report("Single nonzero  ->", new int[][] { { 7 } }, sol); // [[7]]
        report("Single row zero ->", new int[][] { { 1, 0, 3 } }, sol); // [[0,0,0]]
        report("Single col zero ->", new int[][] { { 1 }, { 0 }, { 3 } }, sol); // [[0],[0],[0]]
        report("All zeros       ->", new int[][] {
                { 0, 0 },
                { 0, 0 } }, sol); // all zeros
    }
}
