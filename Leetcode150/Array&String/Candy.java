
import java.util.Arrays;

public class Candy {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (repeated relaxation until stable)
     * ------------------------------------------------------------------------
     * Idea:
     * Start everyone at 1 candy. Repeatedly sweep the array; whenever a child
     * violates the rule versus a neighbor (higher rating but not more candy),
     * bump it to neighbor+1. Keep sweeping until a full pass makes no change.
     *
     * Time : O(n^2) worst case -- a long slope may need up to n sweeps
     * Space : O(n) -- the candy array
     *
     * Correct (it's fixpoint relaxation) but slow; motivates the two-pass sol.
     */
    public int candyBruteForce(int[] ratings) {
        int n = ratings.length;
        int[] candies = new int[n];
        Arrays.fill(candies, 1);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < n; i++) {
                if (i > 0 && ratings[i] > ratings[i - 1]
                        && candies[i] <= candies[i - 1]) {
                    candies[i] = candies[i - 1] + 1;
                    changed = true;
                }
                if (i < n - 1 && ratings[i] > ratings[i + 1]
                        && candies[i] <= candies[i + 1]) {
                    candies[i] = candies[i + 1] + 1;
                    changed = true;
                }
            }
        }
        int total = 0;
        for (int c : candies)
            total += c;
        return total;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (two passes: left-to-right, then right-to-left)
     * ------------------------------------------------------------------------
     * Idea:
     * The two neighbor constraints are independent directions, so handle them
     * in two sweeps and take the max at each position:
     * Pass 1 (L -> R): if ratings[i] > ratings[i-1], candies[i] must exceed
     * its LEFT neighbor -> candies[i] = candies[i-1] + 1.
     * Pass 2 (R -> L): if ratings[i] > ratings[i+1], candies[i] must exceed
     * its RIGHT neighbor -> candies[i] = max(candies[i],
     * candies[i+1] + 1). (max preserves the left result.)
     * Everyone starts at 1 (the minimum). Taking the max satisfies BOTH
     * directions with the smallest legal value at each child.
     *
     * Walkthrough on [1,3,2,2,1]:
     * init [1,1,1,1,1]
     * L->R [1,2,1,1,1] (only index1 rises over its left)
     * R->L [1,2,1,2,1] (index3 > index4 -> bump to 2; index2 not
     * > index3 so stays 1)
     * total = 1+2+1+2+1 = 7
     *
     * Time : O(n) -- two linear passes
     * Space : O(n) -- the candies array
     */
    public int candy(int[] ratings) {
        int n = ratings.length;
        int[] candies = new int[n];
        Arrays.fill(candies, 1); // rule: at least 1 each

        // Pass 1: satisfy the LEFT-neighbor constraint.
        for (int i = 1; i < n; i++) {
            if (ratings[i] > ratings[i - 1]) {
                candies[i] = candies[i - 1] + 1;
            }
        }
        // Pass 2: satisfy the RIGHT-neighbor constraint without breaking pass 1.
        for (int i = n - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) {
                candies[i] = Math.max(candies[i], candies[i + 1] + 1);
            }
        }
        long total = 0;
        for (int c : candies)
            total += c;
        return (int) total;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL O(1) SPACE (single-pass slope counting)
     * ------------------------------------------------------------------------
     * Idea (advanced follow-up):
     * Walk once, counting the lengths of increasing (`up`) and decreasing
     * (`down`) runs plus the length of the current `peak`. Each ascending
     * step contributes 1..up candies (a triangular sum); each descending step
     * likewise. When a descending run grows longer than the last peak, the
     * peak itself needs one extra candy. Uses arithmetic on run lengths
     * instead of an array.
     *
     * Time : O(n)
     * Space : O(1) -- no candies array
     *
     * Trickier to reason about; the two-pass version is what I'd write first,
     * and I'd offer this only if asked to remove the O(n) array.
     */
    public int candyConstantSpace(int[] ratings) {
        int n = ratings.length;
        if (n <= 1)
            return n;
        int total = 1; // first child gets at least 1
        int up = 0; // length of current increasing run
        int down = 0; // length of current decreasing run
        int peak = 0; // candies at the last peak (top of the up-run)
        for (int i = 1; i < n; i++) {
            if (ratings[i] > ratings[i - 1]) { // ascending
                up++;
                down = 0;
                peak = up + 1;
                total += peak;
            } else if (ratings[i] == ratings[i - 1]) { // flat: reset, give 1
                up = 0;
                down = 0;
                peak = 1;
                total += 1;
            } else { // descending
                up = 0;
                down++;
                total += down + (peak > down ? 0 : 1); // bump peak if run is longer
            }
        }
        return total;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Why does taking the MAX in pass 2 preserve pass 1's guarantees?"
     * -> Pass 1 fixed left constraints. Pass 2 only ever INCREASES values,
     * and a larger candies[i] can't violate "greater than a smaller-
     * rated left neighbor", so the left property is never lost.
     *
     * F2. "Can you do it in O(1) extra space?"
     * -> Yes — the slope-counting single pass (Approach 3): sum the
     * triangular contributions of each up/down run, adjusting the peak
     * when a down-run outgrows it.
     *
     * F3. "What if EQUAL ratings had to get EQUAL candies?"
     * -> Different problem: equalities create extra coupling. You'd union
     * equal-rating runs or do constrained relaxation; the simple two-
     * pass max no longer suffices unmodified.
     *
     * F4. "What if the line were CIRCULAR (last child neighbors the first)?"
     * -> The two-pass trick can deadlock on a strictly monotonic cycle
     * (impossible to satisfy). Detect infeasibility; otherwise relax
     * around the ring until stable.
     *
     * F5. "Prove the two-pass result is MINIMAL, not just valid."
     * -> Each child's candy = max(longest increasing run ending at it from
     * the left, longest decreasing run starting at it to the right) + 1.
     * Any valid assignment must be at least this at every position, so
     * the per-position minimum sums to the global minimum.
     *
     * F6. "Only rewards go UP, never reset — variant?"
     * -> That relaxes the down-neighbor constraint; a single left-to-right
     * pass then suffices. Recognizing which constraints apply drives the
     * number of passes.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] ratings, Candy sol) {
        System.out.println(label + " " + Arrays.toString(ratings)
                + " -> twoPass=" + sol.candy(ratings)
                + ", constSpace=" + sol.candyConstantSpace(ratings)
                + ", brute=" + sol.candyBruteForce(ratings));
    }

    public static void main(String[] args) {
        Candy sol = new Candy();

        // Examples
        report("Example 1 ->", new int[] { 1, 0, 2 }, sol); // 5
        report("Example 2 ->", new int[] { 1, 2, 2 }, sol); // 4

        // Edge cases
        report("Single child     ->", new int[] { 5 }, sol); // 1
        report("All equal        ->", new int[] { 3, 3, 3, 3 }, sol); // 4
        report("Strictly up      ->", new int[] { 1, 2, 3, 4 }, sol); // 10 (1+2+3+4)
        report("Strictly down    ->", new int[] { 4, 3, 2, 1 }, sol); // 10
        report("Valley           ->", new int[] { 1, 3, 2, 2, 1 }, sol); // 7
        report("Peak long descent->", new int[] { 1, 2, 87, 87, 87, 2, 1 }, sol); // 13
    }
}
