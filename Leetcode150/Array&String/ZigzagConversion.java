/*
 * ============================================================================
 * LeetCode 6. Zigzag Conversion                             [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * The string "PAYPALISHIRING" is written in a zigzag pattern on a given number
 * of rows like this (you may want to display this pattern in a fixed font for
 * better legibility):
 *
 *   P   A   H   N
 *   A P L S I I G
 *   Y   I   R
 *
 * And then read line by line: "PAHNAPLSIIGYIR".
 *
 * Write the code that will take a string and make this conversion given a
 * number of rows:
 *
 *   string convert(string s, int numRows);
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length <= 1000
 *   s consists of English letters (lower- and upper-case), ',' and '.'.
 *   1 <= numRows <= 1000
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "PAYPALISHIRING", numRows = 3
 *   Output: "PAHNAPLSIIGYIR"
 *
 * Example 2:
 *   Input:  s = "PAYPALISHIRING", numRows = 4
 *   Output: "PINALSIGYAHRPI"
 *   Explanation:
 *     P     I    N
 *     A   L S  I G
 *     Y A   H R
 *     P     I
 *
 * Example 3:
 *   Input:  s = "A", numRows = 1
 *   Output: "A"
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. When numRows == 1, there's no zigzag — return s unchanged?
 *      -> Yes. A single row has no diagonal; the string is written straight.
 *         This is the key edge case that breaks naive index formulas.
 *
 *  Q2. The path goes DOWN the rows, then diagonally UP, repeating?
 *      -> Correct. Down from row 0 to row numRows-1, then up to row 0, forming
 *         a "zig" (vertical) + "zag" (diagonal) cycle.
 *
 *  Q3. Read output row by row, top to bottom, left to right?
 *      -> Yes. Concatenate each row's characters in the order they were placed.
 *
 *  Q4. Can numRows exceed the string length?
 *      -> Yes. Then the zigzag never reaches the bottom; some rows stay empty
 *         and just contribute nothing — the logic still holds.
 *
 *  Q5. Any non-letter characters to worry about?
 *      -> ',' and '.' may appear, but they're treated as ordinary characters;
 *         no special handling.
 *
 *  Q6. Output length equals input length (a permutation of characters)?
 *      -> Yes, it's a rearrangement; every character appears exactly once.
 *
 * ============================================================================
 */

public class ZigzagConversion {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (build a 2D grid, then read it)
     * ------------------------------------------------------------------------
     * Idea:
     *   Literally simulate writing onto a numRows x numCols matrix following the
     *   zigzag path, then scan the grid row by row collecting non-empty cells.
     *
     *   The full period is (2*numRows - 2) characters and spans numRows-1
     *   columns, so numCols is bounded by ceil(len / (2*numRows-2)) * (numRows-1).
     *
     * Time  : O(numRows * numCols) = O(len + empty cells)
     * Space : O(numRows * numCols)  -- the grid, much of it empty (wasteful)
     *
     * Correct and very visual, but allocates a big mostly-empty matrix.
     */
    public String convertGrid(String s, int numRows) {
        if (numRows == 1) return s;                 // no zigzag possible
        int n = s.length();
        int cycle = 2 * numRows - 2;                // characters per full zigzag period
        int numCols = ((n + cycle - 1) / cycle) * (numRows - 1);
        char[][] grid = new char[numRows][numCols];

        int row = 0, col = 0;
        boolean goingDown = true;
        for (int i = 0; i < n; i++) {
            grid[row][col] = s.charAt(i);
            if (goingDown) {
                if (row == numRows - 1) {           // hit the bottom: turn diagonal-up
                    goingDown = false;
                    row--; col++;
                } else {
                    row++;
                }
            } else {
                if (row == 0) {                     // hit the top: turn straight-down
                    goingDown = true;
                    row++;                          // (col stays; next placement goes down)
                    // NOTE: we do NOT advance col here; the down-run shares a column.
                } else {
                    row--; col++;
                }
            }
        }
        StringBuilder sb = new StringBuilder(n);
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                if (grid[r][c] != 0) sb.append(grid[r][c]);
            }
        }
        return sb.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (one StringBuilder per row, simulate the bounce)
     * ------------------------------------------------------------------------
     * Idea:
     *   We don't need a 2D grid — only the ROW each character lands on. Keep a
     *   StringBuilder per row. Walk the string, appending each char to the
     *   current row, and BOUNCE direction at the top (row 0) and bottom
     *   (row numRows-1). Finally concatenate all rows.
     *
     *   The direction flips exactly when we're at the first or last row, which
     *   traces the zigzag without ever computing column indices.
     *
     * Time  : O(n)   -- one pass, each char appended once
     * Space : O(n)   -- the per-row buffers total n characters
     *
     * This is the version I'd write: no wasted grid, trivial to reason about.
     */
    public String convert(String s, int numRows) {
        if (numRows == 1) return s;                 // guard: no bounce with one row

        StringBuilder[] rows = new StringBuilder[Math.min(numRows, s.length())];
        for (int i = 0; i < rows.length; i++) rows[i] = new StringBuilder();

        int curRow = 0;
        boolean goingDown = false;
        for (char c : s.toCharArray()) {
            rows[curRow].append(c);
            // Flip direction at the top or bottom boundary.
            if (curRow == 0 || curRow == numRows - 1) {
                goingDown = !goingDown;
            }
            curRow += goingDown ? 1 : -1;
        }

        StringBuilder result = new StringBuilder(s.length());
        for (StringBuilder row : rows) result.append(row);
        return result.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL O(1) SPACE (direct index formula per row)
     * ------------------------------------------------------------------------
     * Idea (math, no per-row buffers):
     *   Let cycle = 2*numRows - 2. Emit row by row directly into the output.
     *   For row r, characters appear at base indices j = r, r+cycle, r+2*cycle,
     *   ... For every INNER row (0 < r < numRows-1) there's ALSO a diagonal
     *   character at index j + cycle - 2*r (the char on the way up). First and
     *   last rows have only the vertical characters.
     *
     * Time  : O(n)
     * Space : O(1) extra (besides the output).
     *
     * Same complexity as Approach 2 but avoids the row buffers; the index
     * arithmetic is the elegant "show you understand the pattern" answer.
     */
    public String convertFormula(String s, int numRows) {
        if (numRows == 1) return s;
        int n = s.length();
        int cycle = 2 * numRows - 2;
        StringBuilder sb = new StringBuilder(n);
        for (int r = 0; r < numRows; r++) {
            for (int j = r; j < n; j += cycle) {
                sb.append(s.charAt(j));                       // vertical character
                int diag = j + cycle - 2 * r;                 // the up-diagonal partner
                if (r != 0 && r != numRows - 1 && diag < n) {
                    sb.append(s.charAt(diag));
                }
            }
        }
        return sb.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why is numRows == 1 a special case?"
     *      -> cycle = 2*1 - 2 = 0, so the index step becomes 0 -> infinite loop
     *         in the formula, and there's no bounce in the simulation. Returning
     *         s directly avoids both.
     *
     *  F2. "Derive the index formula for each row."
     *      -> Period cycle = 2*numRows-2. Row r's vertical chars sit at r + k*cycle.
     *         The diagonal char between two verticals of an inner row is
     *         (k+1)*cycle - r, i.e. j + cycle - 2*r. Top/bottom rows lack it.
     *
     *  F3. "DECODE: given the zigzag output and numRows, recover the original."
     *      -> Invert the mapping: compute, for each original index, its position
     *         in the output; then scatter characters back. O(n).
     *
     *  F4. "What if we read the grid COLUMN by column instead of row by row?"
     *      -> Just change the read order; the placement (bounce) logic is
     *         identical, only the final concatenation traversal differs.
     *
     *  F5. "Memory is tight and n is huge — which approach?"
     *      -> The formula approach (Approach 3): O(1) extra space, writes each
     *         output character exactly once with no intermediate buffers.
     *
     *  F6. "Generalize the movement (e.g., different turn points / patterns)."
     *      -> The per-row simulation generalizes easily: change the boundary
     *         condition that flips direction; the formula would need re-deriving
     *         for a new period.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, int numRows, ZigzagConversion sol) {
        System.out.println(label + " s=\"" + s + "\", numRows=" + numRows
                + " -> sim=\"" + sol.convert(s, numRows) + "\""
                + ", formula=\"" + sol.convertFormula(s, numRows) + "\""
                + ", grid=\"" + sol.convertGrid(s, numRows) + "\"");
    }

    public static void main(String[] args) {
        ZigzagConversion sol = new ZigzagConversion();

        // Examples
        report("Example 1 ->", "PAYPALISHIRING", 3, sol);  // "PAHNAPLSIIGYIR"
        report("Example 2 ->", "PAYPALISHIRING", 4, sol);  // "PINALSIGYAHRPI"
        report("Example 3 ->", "A", 1, sol);               // "A"

        // Edge cases
        report("numRows = 1       ->", "ABCDEF", 1, sol);        // "ABCDEF" (unchanged)
        report("numRows >= length ->", "ABC", 5, sol);          // "ABC" (never reaches bottom)
        report("Two rows          ->", "ABCDE", 2, sol);        // "ACEBD"
        report("Full period       ->", "ABCD", 3, sol);         // "ABDC"
        report("With punctuation  ->", "a.b,c", 2, sol);        // "abc.," (row0=a,b,c ; row1=.,,)
    }
}
