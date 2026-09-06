/*
 * ============================================================================
 * LeetCode 274. H-Index                                      [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an array of integers citations where citations[i] is the number of
 * citations a researcher received for their i-th paper, return the
 * researcher's h-index.
 *
 * According to the definition of h-index on Wikipedia: the h-index is the
 * MAXIMUM value of h such that the given researcher has published AT LEAST h
 * papers that have EACH been cited at least h times.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   n == citations.length
 *   1 <= n <= 5000
 *   0 <= citations[i] <= 1000
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  citations = [3,0,6,1,5]
 *   Output: 3
 *   Explanation: The researcher has 5 papers with 3,0,6,1,5 citations. Three of
 *                them (3, 6, 5) have >= 3 citations each, and the remaining two
 *                have <= 3, so the h-index is 3. (It's not 4 because only two
 *                papers have >= 4 citations.)
 *
 * Example 2:
 *   Input:  citations = [1,3,1]
 *   Output: 1
 *   Explanation: At least 1 paper has >= 1 citation, but not 2 papers with >= 2.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Definition check: h-index = max h with at least h papers each cited >= h?
 *      -> Yes. h can range from 0 (no qualifying papers) up to n.
 *
 *  Q2. Can the h-index exceed the number of papers n?
 *      -> No. You can't have more than n papers with >= h citations, so
 *         h <= n always. This caps the search space.
 *
 *  Q3. Can citation counts be 0?
 *      -> Yes. A paper with 0 citations simply can't contribute to any h >= 1.
 *
 *  Q4. Is the array sorted?
 *      -> Not guaranteed. Sorting is one valid strategy; counting sort is
 *         another that avoids the comparison-sort cost.
 *
 *  Q5. Are ties / duplicate citation counts allowed?
 *      -> Yes, duplicates are fine and common.
 *
 *  Q6. What's returned when all citations are 0?
 *      -> 0 (no paper has >= 1 citation).
 *
 * ============================================================================
 */

import java.util.Arrays;

public class HIndex {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (try every candidate h from n down to 0)
     * ------------------------------------------------------------------------
     * Idea:
     *   For each candidate h in [n, n-1, ..., 0], count how many papers have
     *   >= h citations. The first (largest) h whose count >= h is the answer.
     *
     * Time  : O(n^2)   -- for each of n+1 candidates, scan all n papers
     * Space : O(1)
     *
     * Directly encodes the definition. Fine for n <= 5000 but improvable.
     */
    public int hIndexBruteForce(int[] citations) {
        int n = citations.length;
        for (int h = n; h >= 0; h--) {
            int count = 0;
            for (int c : citations) {
                if (c >= h) count++;
            }
            if (count >= h) return h;      // largest h that satisfies the rule
        }
        return 0;                          // unreachable (h = 0 always works)
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — SORTING (descending, find the crossover point)
     * ------------------------------------------------------------------------
     * Idea:
     *   Sort citations in DESCENDING order. Then the i-th paper (0-based) is the
     *   (i+1)-th most cited. We can guarantee (i+1) papers with >= citations[i]
     *   citations. So we scan while citations[i] > i (i.e. the (i+1)-th paper
     *   still has more citations than its rank index). The count where this
     *   holds is the h-index.
     *
     *   Equivalently (ascending sort): for each i, n - i papers have >=
     *   citations[i] citations; the h-index is the largest such (n - i) with
     *   citations[i] >= n - i.
     *
     * Time  : O(n log n)   -- dominated by the sort
     * Space : O(1) extra   -- (ignoring sort internals; we sort a copy here)
     */
    public int hIndexSorting(int[] citations) {
        int[] sorted = Arrays.copyOf(citations, citations.length);
        Arrays.sort(sorted);                       // ascending
        int n = sorted.length;
        for (int i = 0; i < n; i++) {
            // n - i papers have >= sorted[i] citations.
            // The h-index is the first point where citations meet/exceed that count.
            if (sorted[i] >= n - i) {
                return n - i;
            }
        }
        return 0;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (counting sort / bucket by citation count)
     * ------------------------------------------------------------------------
     * Idea:
     *   The h-index is at most n, so any citation count above n is "as good as
     *   n" for our purposes. Bucket papers by citation count, clamping counts
     *   >= n into bucket[n]. Then sweep h from n down to 0, accumulating how
     *   many papers have >= h citations. The first h whose running total >= h
     *   is the answer.
     *
     * Time  : O(n)   -- one pass to bucket, one pass to accumulate
     * Space : O(n)   -- the bucket array of size n + 1
     *
     * Beats the sort by avoiding the log factor; ideal when counts are bounded.
     */
    public int hIndex(int[] citations) {
        int n = citations.length;
        int[] bucket = new int[n + 1];             // bucket[k] = #papers with exactly k citations (k>=n clamped to n)
        for (int c : citations) {
            bucket[Math.min(c, n)]++;              // clamp: counts > n can't push h past n
        }
        int total = 0;                             // #papers with >= h citations
        for (int h = n; h >= 0; h--) {
            total += bucket[h];
            if (total >= h) return h;              // largest h satisfying the rule
        }
        return 0;                                  // unreachable
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "What if the array is ALREADY sorted (ascending)?" (LeetCode 275)
     *      -> Binary search for the smallest index i where citations[i] >= n-i;
     *         the h-index is n - i. O(log n) time, O(1) space.
     *
     *  F2. "Why clamp citation counts to n in the bucket approach?"
     *      -> h can never exceed n (only n papers exist). A paper with 10^9
     *         citations contributes to every threshold h <= n identically to a
     *         paper with exactly n citations, so bucketing it at n loses nothing
     *         and bounds memory to O(n).
     *
     *  F2b. "Prove the counting-sort sweep is correct."
     *      -> Sweeping h from high to low, `total` accumulates papers with
     *         citation count >= h monotonically. The first h with total >= h is
     *         by construction the MAXIMUM such h (we go high->low), matching the
     *         definition exactly.
     *
     *  F3. "Compute the h-index of a STREAM of citation counts online."
     *      -> Maintain the bucket counts incrementally; recompute or maintain
     *         the crossover as new papers arrive. Each update O(1) amortized
     *         with a maintained running frontier.
     *
     *  F4. "Return the g-index or i10-index instead."
     *      -> Similar rank-vs-value crossover logic; g-index uses cumulative
     *         citations >= g^2, i10 just counts papers with >= 10 citations.
     *
     *  F5. "n is enormous and counts are unbounded — memory matters."
     *      -> Fall back to the O(n log n) sort (O(1) extra if in place); the
     *         bucket method's O(n) memory is usually fine given h <= n clamping.
     *
     *  F6. "Compare your three approaches."
     *      -> Brute force O(n^2): simplest, ties to definition. Sorting
     *         O(n log n): clean, low memory. Counting sort O(n) time / O(n)
     *         space: fastest, exploits bounded/clamped counts. I'd ship the
     *         counting-sort version.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] citations, HIndex sol) {
        System.out.println(label + " " + Arrays.toString(citations)
                + " -> counting=" + sol.hIndex(citations)
                + ", sorting=" + sol.hIndexSorting(citations)
                + ", brute=" + sol.hIndexBruteForce(citations));
    }

    public static void main(String[] args) {
        HIndex sol = new HIndex();

        // Examples
        report("Example 1 ->", new int[]{3, 0, 6, 1, 5}, sol);  // 3
        report("Example 2 ->", new int[]{1, 3, 1}, sol);        // 1

        // Edge cases
        report("All zeros        ->", new int[]{0, 0, 0}, sol);        // 0
        report("Single paper hi  ->", new int[]{100}, sol);           // 1
        report("Single paper zero->", new int[]{0}, sol);             // 0
        report("All same >= n    ->", new int[]{4, 4, 4, 4}, sol);    // 4
        report("Large counts     ->", new int[]{10, 8, 5, 4, 3}, sol);// 4
        report("Descending mix   ->", new int[]{6, 5, 3, 1, 0}, sol); // 3
    }
}
