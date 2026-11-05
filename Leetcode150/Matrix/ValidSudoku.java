package Leetcode150.Matrix;

/*
 * ============================================================================
 * LeetCode 36. Valid Sudoku                                   [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Determine if a 9 x 9 Sudoku board is VALID. Only the filled cells need to be
 * validated according to the following rules:
 *   1. Each ROW must contain the digits 1-9 without repetition.
 *   2. Each COLUMN must contain the digits 1-9 without repetition.
 *   3. Each of the nine 3 x 3 SUB-BOXES must contain the digits 1-9 without
 *      repetition.
 *
 * Note:
 *   - A Sudoku board (partially filled) could be valid but is NOT necessarily
 *     solvable.
 *   - Only the filled cells need to be validated according to the rules.
 *   - Empty cells are represented by the character '.'.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   board.length == 9
 *   board[i].length == 9
 *   board[i][j] is a digit '1'-'9' or '.'
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1 (valid):
 *   board =
 *     [["5","3",".",".","7",".",".",".","."]
 *     ,["6",".",".","1","9","5",".",".","."]
 *     ,[".","9","8",".",".",".",".","6","."]
 *     ,["8",".",".",".","6",".",".",".","3"]
 *     ,["4",".",".","8",".","3",".",".","1"]
 *     ,["7",".",".",".","2",".",".",".","6"]
 *     ,[".","6",".",".",".",".","2","8","."]
 *     ,[".",".",".","4","1","9",".",".","5"]
 *     ,[".",".",".",".","8",".",".","7","9"]]
 *   Output: true
 *
 * Example 2 (invalid):
 *   Same board as Example 1 except the top-left "5" is changed to "8".
 *   Output: false
 *   Explanation: There are now two 8s — one at the top-left and one already in
 *                the top-left 3x3 sub-box (and the same column), violating the
 *                sub-box (and column) rule.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Do I need to check SOLVABILITY, or just current validity?
 *      -> Just validity of the filled cells. A valid board need not be solvable.
 *
 *  Q2. What marks an empty cell?
 *      -> '.'. Empty cells are skipped entirely (no constraint on them).
 *
 *  Q3. Board is always exactly 9x9 with chars '1'-'9' or '.'?
 *      -> Yes per constraints; no need to validate the character set or size.
 *
 *  Q4. Are the three rule sets (row/col/box) all required simultaneously?
 *      -> Yes — a digit is a conflict if it repeats in its row, its column, OR
 *         its 3x3 box.
 *
 *  Q5. How is the 3x3 box index computed?
 *      -> boxIndex = (row / 3) * 3 + (col / 3), giving 0..8. Integer division
 *         groups rows/cols into bands of three.
 *
 *  Q6. Should I return early on the first conflict?
 *      -> Yes — as soon as any duplicate is found we can return false.
 *
 * ============================================================================
 */

import java.util.HashSet;
import java.util.Set;

public class ValidSudoku {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — "BRUTE FORCE" (three separate passes with sets)
     * ------------------------------------------------------------------------
     * Idea:
     * Do three independent scans:
     * - For each row, collect digits into a Set; a repeat -> invalid.
     * - For each column, same.
     * - For each 3x3 box, same.
     * Straightforward and easy to reason about, just multiple passes.
     *
     * Time : O(81) = O(1) -- fixed 9x9 board (3 passes over 81 cells)
     * Space : O(9) -- one set at a time
     *
     * Correct; the single-pass version below checks all three constraints at
     * once, which is cleaner but the same asymptotic cost.
     */
    public boolean isValidSudokuThreePass(char[][] board) {
        // Rows
        for (int r = 0; r < 9; r++) {
            Set<Character> seen = new HashSet<>();
            for (int c = 0; c < 9; c++) {
                char ch = board[r][c];
                if (ch != '.' && !seen.add(ch))
                    return false;
            }
        }
        // Columns
        for (int c = 0; c < 9; c++) {
            Set<Character> seen = new HashSet<>();
            for (int r = 0; r < 9; r++) {
                char ch = board[r][c];
                if (ch != '.' && !seen.add(ch))
                    return false;
            }
        }
        // 3x3 boxes
        for (int br = 0; br < 9; br += 3) {
            for (int bc = 0; bc < 9; bc += 3) {
                Set<Character> seen = new HashSet<>();
                for (int r = br; r < br + 3; r++) {
                    for (int c = bc; c < bc + 3; c++) {
                        char ch = board[r][c];
                        if (ch != '.' && !seen.add(ch))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL SINGLE PASS (9 sets each for rows/cols/boxes) <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     * Keep three arrays of sets: rows[9], cols[9], boxes[9]. Walk every cell
     * once; for a filled cell with digit d at (r, c):
     * - box = (r / 3) * 3 + (c / 3)
     * - if d is already in rows[r], cols[c], or boxes[box] -> invalid.
     * - otherwise add d to all three.
     * One pass validates all three constraints simultaneously.
     *
     * Time : O(81) = O(1)
     * Space : O(81) worst case across the 27 sets = O(1) (bounded by board size)
     */
    @SuppressWarnings("unchecked") // arrays of generic sets -> unchecked cast is safe here
    public boolean isValidSudoku(char[][] board) {
        Set<Character>[] rows = new HashSet[9];
        Set<Character>[] cols = new HashSet[9];
        Set<Character>[] boxes = new HashSet[9];
        for (int i = 0; i < 9; i++) {
            rows[i] = new HashSet<>();
            cols[i] = new HashSet<>();
            boxes[i] = new HashSet<>();
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                char d = board[r][c];
                if (d == '.')
                    continue;
                int box = (r / 3) * 3 + (c / 3);
                // adding returns false if already present -> duplicate -> invalid
                if (!rows[r].add(d) || !cols[c].add(d) || !boxes[box].add(d)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL with BITMASKS (no hashing, fastest)
     * ------------------------------------------------------------------------
     * Idea:
     * Replace each set with a single int used as a 9-bit bitmask (bit d-1 set
     * means digit d is present). Checking/adding is one AND + OR — no hashing,
     * no boxing. This is the most memory- and cache-friendly variant.
     *
     * Time : O(81) = O(1)
     * Space : O(27) ints = O(1)
     */
    public boolean isValidSudokuBitmask(char[][] board) {
        int[] rows = new int[9];
        int[] cols = new int[9];
        int[] boxes = new int[9];

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                char ch = board[r][c];
                if (ch == '.')
                    continue;
                int bit = 1 << (ch - '1'); // digit '1'..'9' -> bit 0..8
                int box = (r / 3) * 3 + (c / 3);
                if ((rows[r] & bit) != 0
                        || (cols[c] & bit) != 0
                        || (boxes[box] & bit) != 0) {
                    return false; // already seen this digit
                }
                rows[r] |= bit;
                cols[c] |= bit;
                boxes[box] |= bit;
            }
        }
        return true;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why is the box index (r/3)*3 + (c/3)?"
     * -> r/3 and c/3 each map 0..8 into band 0..2. Multiplying the row band
     * by 3 and adding the column band gives a unique 0..8 id per 3x3 box
     * in row-major order.
     *
     * F2. "Sets vs. bitmasks — trade-offs?"
     * -> Same O(1) complexity for a fixed board, but bitmasks avoid hashing,
     * autoboxing, and allocations; they're faster and lower memory. Sets
     * read more clearly and generalize to non-digit symbols.
     *
     * F3. "Generalize to an N x N Sudoku (N a perfect square, box size sqrt(N))?"
     * -> Same algorithm with box = (r / s) * s + (c / s) where s = sqrt(N);
     * bitmask needs N bits (use long or a bitset for N > 32/64).
     *
     * F4. "Now actually SOLVE the Sudoku."
     * -> Backtracking (LeetCode 37): pick an empty cell, try digits 1-9 that
     * don't conflict (reuse these same row/col/box masks), recurse, undo
     * on failure. The validity structures here are exactly the pruning.
     *
     * F5. "Single pass vs three passes — any real difference?"
     * -> Same asymptotics; single pass touches each cell once and can
     * short-circuit earlier on average. Three passes are marginally
     * simpler to write but re-scan the board.
     *
     * F6. "What if the board could contain invalid characters or wrong size?"
     * -> Add validation: check dimensions and that each char is '.' or
     * '1'-'9'; reject otherwise before running the constraint checks.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, char[][] board, ValidSudoku sol) {
        System.out.println(label
                + " -> single=" + sol.isValidSudoku(board)
                + ", bitmask=" + sol.isValidSudokuBitmask(board)
                + ", threePass=" + sol.isValidSudokuThreePass(board));
    }

    private static char[][] board(String... rows) {
        char[][] b = new char[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                b[r][c] = rows[r].charAt(c);
            }
        }
        return b;
    }

    public static void main(String[] args) {
        ValidSudoku sol = new ValidSudoku();

        // Example 1 — valid board
        char[][] valid = board(
                "53..7....",
                "6..195...",
                ".98....6.",
                "8...6...3",
                "4..8.3..1",
                "7...2...6",
                ".6....28.",
                "...419..5",
                "....8..79");
        report("Example 1 (valid)   ->", valid, sol); // true

        // Example 2 — invalid (top-left 5 -> 8, clashes in column & box)
        char[][] invalid = board(
                "83..7....",
                "6..195...",
                ".98....6.",
                "8...6...3",
                "4..8.3..1",
                "7...2...6",
                ".6....28.",
                "...419..5",
                "....8..79");
        report("Example 2 (invalid) ->", invalid, sol); // false

        // Edge cases
        char[][] empty = board(
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........");
        report("All empty (valid)   ->", empty, sol); // true

        char[][] dupRow = board(
                "12......1", // two 1s in the same row
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........");
        report("Duplicate in row    ->", dupRow, sol); // false

        char[][] dupCol = board(
                "5........",
                "5........", // two 5s in the same column
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........");
        report("Duplicate in column ->", dupCol, sol); // false

        char[][] dupBox = board(
                "9........",
                "..9......", // both 9s in the top-left 3x3 box
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........",
                ".........");
        report("Duplicate in box    ->", dupBox, sol); // false
    }
}
