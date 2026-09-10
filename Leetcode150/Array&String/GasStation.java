/*
 * ============================================================================
 * LeetCode 134. Gas Station                                  [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * There are n gas stations along a CIRCULAR route, where the amount of gas at
 * the i-th station is gas[i].
 *
 * You have a car with an unlimited gas tank and it costs cost[i] of gas to
 * travel from the i-th station to its next (i + 1)-th station. You begin the
 * journey with an empty tank at one of the gas stations.
 *
 * Given two integer arrays gas and cost, return the starting gas station's
 * index if you can travel around the circuit ONCE in the clockwise direction,
 * otherwise return -1. If there exists a solution, it is guaranteed to be
 * UNIQUE.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   n == gas.length == cost.length
 *   1 <= n <= 10^5
 *   0 <= gas[i], cost[i] <= 10^4
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  gas = [1,2,3,4,5], cost = [3,4,5,1,2]
 *   Output: 3
 *   Explanation:
 *     Start at station 3 (gas=4). Tank = 0 + 4 - 1 = 3.
 *     Travel to station 4. Tank = 3 + 5 - 2 = 6.
 *     Travel to station 0. Tank = 6 + 1 - 3 = 4.
 *     Travel to station 1. Tank = 4 + 2 - 4 = 2.
 *     Travel to station 2. Tank = 2 + 3 - 5 = 0.
 *     Back to station 3. The tank never went negative, so 3 is the answer.
 *
 * Example 2:
 *   Input:  gas = [2,3,4], cost = [3,4,3]
 *   Output: -1
 *   Explanation: Total gas (9) < total cost (10), so the circuit is impossible
 *                from ANY starting station.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. The route is CIRCULAR and I must complete exactly one full loop?
 *      -> Yes. From the last station you wrap back to station 0.
 *
 *  Q2. Is the solution guaranteed UNIQUE when one exists?
 *      -> Yes. That's a strong hint the greedy "single valid start" works.
 *
 *  Q3. Only CLOCKWISE travel (i -> i+1)?
 *      -> Yes, forward direction only.
 *
 *  Q4. Unlimited tank capacity (no cap on how much gas I can hold)?
 *      -> Yes, the tank never overflows; the only failure is going negative.
 *
 *  Q5. Can gas[i] or cost[i] be 0?
 *      -> Yes. A station may give 0 gas or cost 0 to leave.
 *
 *  Q6. If total gas < total cost, is it always -1?
 *      -> Yes — you physically lack the fuel for the loop regardless of start.
 *         Conversely, total gas >= total cost guarantees a solution exists.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class GasStation {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (try every starting station)
     * ------------------------------------------------------------------------
     * Idea:
     *   For each candidate start s, simulate the full circular trip. If the
     *   tank stays >= 0 for all n hops, s is the answer. Otherwise try the next.
     *
     * Time  : O(n^2)   -- n starts * up to n hops each
     * Space : O(1)
     *
     * Correct and intuitive, but too slow for n up to 1e5. Motivates greedy.
     */
    public int canCompleteCircuitBruteForce(int[] gas, int[] cost) {
        int n = gas.length;
        for (int start = 0; start < n; start++) {
            int tank = 0;
            int steps = 0;
            int i = start;
            while (steps < n) {
                tank += gas[i] - cost[i];
                if (tank < 0) break;         // ran dry before completing
                i = (i + 1) % n;             // move clockwise, wrapping around
                steps++;
            }
            if (steps == n) return start;    // completed the full loop
        }
        return -1;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (single pass greedy)
     * ------------------------------------------------------------------------
     * Idea — two independent facts:
     *   (A) FEASIBILITY: A full loop is possible IFF total(gas) >= total(cost).
     *       If the sum of all (gas[i] - cost[i]) is negative, no start works.
     *
     *   (B) WHERE to start: Track a running `tank` from the current candidate
     *       start. If `tank` ever drops below 0 at station i, then NO station
     *       in [start .. i] can be a valid start — every one of them would also
     *       fail by station i (they each had >= 0 fuel arriving at start, and
     *       still couldn't survive to i). So the next viable candidate is i+1;
     *       reset tank to 0 and continue.
     *
     *   Combine: maintain `total` (never reset, tests feasibility) and `tank`
     *   (resettable, picks the start). One pass.
     *
     *   Why it's correct: if total >= 0 a unique answer exists, and the last
     *   station after which tank never again goes negative is exactly that
     *   start. The "skip past the failing prefix" argument guarantees we land
     *   on it.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)
     */
    public int canCompleteCircuit(int[] gas, int[] cost) {
        int total = 0;      // net gas over the WHOLE circuit -> feasibility test
        int tank = 0;       // running fuel since the current candidate start
        int start = 0;      // current best guess for the starting station
        for (int i = 0; i < gas.length; i++) {
            int diff = gas[i] - cost[i];
            total += diff;
            tank += diff;
            if (tank < 0) {          // can't reach station i+1 from `start`
                start = i + 1;       // so the earliest possible start is i+1
                tank = 0;            // fresh tank from the new candidate
            }
        }
        return total >= 0 ? start : -1;   // feasible only if net gas is non-negative
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Prove that if tank goes negative at i, no start in [start..i] works."
     *      -> Each station j in (start, i] was reached with tank >= 0 from
     *         start. Starting AT j means arriving with tank 0 <= the amount we
     *         actually had, so j can only do WORSE over [j..i]. Since the run
     *         from start already failed by i, so would any j. Hence skip to i+1.
     *
     *  F2. "Prove total >= 0 implies a solution exists."
     *      -> Sum of (gas-cost) >= 0 means fuel is globally sufficient. The
     *         greedy's final `start` never fails afterward (no negative dip
     *         after it), and the wrap-around deficit before it is covered by
     *         the surplus after it since the total is non-negative.
     *
     *  F3. "Why is the answer guaranteed UNIQUE?"
     *      -> The problem states it. Intuition: two distinct valid starts would
     *         imply a strictly positive cycle sub-structure allowing either,
     *         which the equality total = gas-cost balance forbids under the
     *         given constraints.
     *
     *  F4. "Return ALL valid starts if uniqueness weren't guaranteed."
     *      -> Drop the uniqueness assumption: after finding feasibility, do a
     *         second pass simulating from each 'reset point' candidate, or run
     *         the doubled-array sliding window to collect every start. O(n).
     *
     *  F5. "What if travel could be COUNTER-clockwise too?"
     *      -> Run the same greedy on the reversed route; compare both
     *         directions. Still O(n).
     *
     *  F6. "Streaming / very large n — memory?"
     *      -> The greedy already uses O(1) space and one pass, so it streams
     *         naturally as long as we can read gas[i], cost[i] in order (with
     *         the caveat that we can't wrap without a second pass over the head).
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] gas, int[] cost, GasStation sol) {
        System.out.println(label
                + " gas="  + Arrays.toString(gas)
                + " cost=" + Arrays.toString(cost)
                + " -> greedy=" + sol.canCompleteCircuit(gas, cost)
                + ", brute=" + sol.canCompleteCircuitBruteForce(gas, cost));
    }

    public static void main(String[] args) {
        GasStation sol = new GasStation();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 4, 5}, new int[]{3, 4, 5, 1, 2}, sol);  // 3
        report("Example 2 ->", new int[]{2, 3, 4}, new int[]{3, 4, 3}, sol);              // -1

        // Edge cases
        report("Single station ok  ->", new int[]{5}, new int[]{4}, sol);                 // 0
        report("Single station bad ->", new int[]{3}, new int[]{4}, sol);                 // -1
        report("Start at 0         ->", new int[]{4, 3, 2}, new int[]{1, 3, 5}, sol);     // 0
        report("Exact balance      ->", new int[]{2, 2, 2}, new int[]{2, 2, 2}, sol);     // 0
        report("Wrap-around start  ->", new int[]{5, 1, 2, 3, 4}, new int[]{4, 4, 1, 5, 1}, sol); // 4
    }
}
