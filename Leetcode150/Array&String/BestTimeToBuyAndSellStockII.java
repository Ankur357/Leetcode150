/*
 * ============================================================================
 * LeetCode 122. Best Time to Buy and Sell Stock II          [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an integer array prices where prices[i] is the price of a
 * given stock on the i-th day.
 *
 * On each day, you may decide to buy and/or sell the stock. You can only hold
 * AT MOST ONE share of the stock at any time. However, you can buy it then
 * immediately sell it on the SAME day.
 *
 * Find and return the MAXIMUM profit you can achieve.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= prices.length <= 3 * 10^4
 *   0 <= prices[i] <= 10^4
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  prices = [7,1,5,3,6,4]
 *   Output: 7
 *   Explanation: Buy on day 2 (price=1), sell on day 3 (price=5), profit = 4.
 *                Then buy on day 4 (price=3), sell on day 5 (price=6), profit = 3.
 *                Total profit = 4 + 3 = 7.
 *
 * Example 2:
 *   Input:  prices = [1,2,3,4,5]
 *   Output: 4
 *   Explanation: Buy day 1 (price=1), sell day 5 (price=5), profit = 4.
 *                (Equivalently, capture each daily rise: 1+1+1+1 = 4.)
 *
 * Example 3:
 *   Input:  prices = [7,6,4,3,1]
 *   Output: 0
 *   Explanation: Prices only fall, so never trade -> profit 0.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. UNLIMITED transactions allowed (as many buy/sell pairs as I want)?
 *      -> Yes. This is the key difference from LeetCode 121 (single txn).
 *
 *  Q2. Can I hold more than one share at a time?
 *      -> No. At most one share held at any moment; must sell before buying
 *         again.
 *
 *  Q3. Can I buy and sell on the SAME day?
 *      -> Yes, the prompt allows it (nets 0, so it never hurts or helps).
 *
 *  Q4. Any transaction fee or cooldown?
 *      -> No fee, no cooldown here. (Those are LeetCode 714 / 309.)
 *
 *  Q5. If prices only decrease, the answer is 0 (just don't trade)?
 *      -> Correct. Profit is floored at 0.
 *
 *  Q6. Overflow worries?
 *      -> No. Max total profit <= (n-1)*10^4 ~ 3e8, fits in int.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class BestTimeToBuyAndSellStockII {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (recursion over all buy/sell decisions)
     * ------------------------------------------------------------------------
     * Idea:
     *   At each day, in each state (holding or not), try every choice:
     *     - not holding: either skip today, or buy today (pay prices[day]).
     *     - holding:     either skip today, or sell today (gain prices[day]).
     *   Recurse and take the max. This explores the full decision tree.
     *
     * Time  : O(2^n)   -- exponential branching, no memoization
     * Space : O(n)     -- recursion depth
     *
     * Purely to demonstrate the decision structure; will TLE. Adding memo on
     * (day, holding) makes it O(n) — the DP in Approach 3.
     */
    public int maxProfitBruteForce(int[] prices) {
        return dfs(prices, 0, false);
    }

    private int dfs(int[] prices, int day, boolean holding) {
        if (day == prices.length) return 0;

        int skip = dfs(prices, day + 1, holding);          // do nothing today
        int act;
        if (holding) {
            act = prices[day] + dfs(prices, day + 1, false); // sell today
        } else {
            act = -prices[day] + dfs(prices, day + 1, true); // buy today
        }
        return Math.max(skip, act);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (greedy: sum every positive daily delta)
     * ------------------------------------------------------------------------
     * Idea:
     *   With unlimited transactions and no fees, ANY multi-day gain can be
     *   decomposed into consecutive single-day gains:
     *       (p[j] - p[i]) = (p[i+1]-p[i]) + (p[i+2]-p[i+1]) + ... + (p[j]-p[j-1])
     *   So the maximum total profit is simply the sum of every POSITIVE
     *   day-to-day increase. We "ride" every up-slope and ignore down-slopes.
     *
     *   Intuitively: whenever tomorrow is higher than today, buy today and sell
     *   tomorrow. Overlapping/adjacent buys-sells telescope into longer holds,
     *   so this greedy is equivalent to the optimal set of transactions.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)
     */
    public int maxProfit(int[] prices) {
        int profit = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i - 1]) {
                profit += prices[i] - prices[i - 1];   // capture this up-slope
            }
        }
        return profit;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (DP state machine: hold vs. cash)
     * ------------------------------------------------------------------------
     * Idea:
     *   Track two running states after each day:
     *     cash = max profit while NOT holding a share
     *     hold = max profit while holding one share (a negative "invested" cost)
     *   Transitions per day at price p:
     *     cash = max(cash, hold + p)   // stay in cash, or sell today
     *     hold = max(hold, cash - p)   // keep holding, or buy today
     *   Answer is `cash` at the end (never beneficial to end while holding).
     *
     *   This is the general framework that extends cleanly to fees, cooldowns,
     *   and transaction caps — worth showing even though the greedy is shorter.
     *
     * Time  : O(n)
     * Space : O(1)
     */
    public int maxProfitDP(int[] prices) {
        int cash = 0;                       // not holding
        int hold = Integer.MIN_VALUE;       // holding (impossible before buying)
        for (int p : prices) {
            cash = Math.max(cash, hold + p);
            hold = Math.max(hold, cash - p);
        }
        return cash;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Prove the greedy is optimal."
     *      -> Any transaction (buy@i, sell@j) equals the telescoping sum of
     *         consecutive deltas from i to j. Positive deltas add profit,
     *         negative ones only subtract, so taking exactly the positive
     *         deltas is an upper bound AND achievable -> optimal.
     *
     *  F2. "Add a transaction FEE per sale." (LeetCode 714)
     *      -> Greedy breaks (a fee can make short up-slopes unprofitable). Use
     *         the state machine: cash = max(cash, hold + p - fee). O(n)/O(1).
     *
     *  F3. "Add a 1-day COOLDOWN after selling." (LeetCode 309)
     *      -> Add a `rest`/prev-cash state so a buy can't immediately follow a
     *         sell. Three-state DP. O(n)/O(1).
     *
     *  F4. "Cap transactions at K." (LeetCode 188 / 123)
     *      -> DP over k transactions: buy[k], sell[k] arrays. O(nK). If K is
     *         large (>= n/2) it degenerates to this unlimited problem.
     *
     *  F5. "Return the actual list of buy/sell days."
     *      -> Walk the deltas: a positive run's start is a buy day, its end is
     *         a sell day. Merge adjacent up-days into one hold interval.
     *
     *  F6. "Which solution do you ship — greedy or DP?"
     *      -> Greedy: shortest and fastest for THIS exact problem. DP state
     *         machine: I'd reach for it if fees/cooldowns/caps are even
     *         remotely on the table, since it generalizes without a rewrite.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] prices, BestTimeToBuyAndSellStockII sol) {
        System.out.println(label + " " + Arrays.toString(prices)
                + " -> greedy=" + sol.maxProfit(prices)
                + ", dp=" + sol.maxProfitDP(prices)
                + ", brute=" + sol.maxProfitBruteForce(prices));
    }

    public static void main(String[] args) {
        BestTimeToBuyAndSellStockII sol = new BestTimeToBuyAndSellStockII();

        // Examples
        report("Example 1 ->", new int[]{7, 1, 5, 3, 6, 4}, sol);  // 7
        report("Example 2 ->", new int[]{1, 2, 3, 4, 5}, sol);     // 4
        report("Example 3 ->", new int[]{7, 6, 4, 3, 1}, sol);     // 0

        // Edge cases
        report("Single day     ->", new int[]{5}, sol);                 // 0
        report("All equal      ->", new int[]{3, 3, 3}, sol);           // 0
        report("Zigzag         ->", new int[]{1, 5, 2, 8, 3}, sol);     // 10 (4 + 6)
        report("Peak then drop ->", new int[]{2, 9, 1}, sol);           // 7
    }
}
