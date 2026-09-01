/*
 * ============================================================================
 * LeetCode 121. Best Time to Buy and Sell Stock               [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an array prices where prices[i] is the price of a given stock
 * on the i-th day.
 *
 * You want to maximize your profit by choosing a SINGLE day to buy one stock
 * and choosing a DIFFERENT day in the FUTURE to sell that stock.
 *
 * Return the maximum profit you can achieve from this transaction. If you
 * cannot achieve any profit, return 0.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= prices.length <= 10^5
 *   0 <= prices[i] <= 10^4
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  prices = [7,1,5,3,6,4]
 *   Output: 5
 *   Explanation: Buy on day 2 (price = 1) and sell on day 5 (price = 6),
 *                profit = 6 - 1 = 5. Note buying at 1 and selling at 6 is best;
 *                you cannot buy on day 5 and sell on day 2 (must sell later).
 *
 * Example 2:
 *   Input:  prices = [7,6,4,3,1]
 *   Output: 0
 *   Explanation: Prices only fall, so no profitable transaction exists;
 *                the best you can do is not trade -> profit 0.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Exactly ONE buy and ONE sell (a single transaction)?
 *      -> Yes. Multiple transactions is a different problem (LeetCode 122).
 *
 *  Q2. Must the sell day be strictly AFTER the buy day?
 *      -> Yes, sell in the future. Can't sell on the same day as buying (that
 *         would be profit 0 anyway).
 *
 *  Q3. If no profit is possible, return 0 (i.e. we simply don't trade)?
 *      -> Correct. Profit is never negative — 0 is the floor.
 *
 *  Q4. Can prices be equal on different days?
 *      -> Yes. Equal prices just yield 0 profit for that pair.
 *
 *  Q5. Any overflow concern?
 *      -> No. Max profit <= 10^4, fits comfortably in int.
 *
 *  Q6. Single-day array?
 *      -> n can be 1. You can't buy and sell, so profit is 0.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class BestTimeToBuyAndSellStock {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check every buy/sell pair)
     * ------------------------------------------------------------------------
     * Idea:
     *   Try every pair (i, j) with i < j and track the maximum of
     *   prices[j] - prices[i]. Directly encodes the definition.
     *
     * Time  : O(n^2)   -- nested loops over all pairs
     * Space : O(1)
     *
     * Weakness: quadratic — times out for n up to 1e5. Interviewer will push
     * for linear.
     */
    public int maxProfitBruteForce(int[] prices) {
        int best = 0;
        for (int i = 0; i < prices.length; i++) {
            for (int j = i + 1; j < prices.length; j++) {
                best = Math.max(best, prices[j] - prices[i]);
            }
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL (one pass, track the minimum price so far)
     * ------------------------------------------------------------------------
     * Idea:
     *   The key realization: to sell on day j for max profit, we want to have
     *   bought at the LOWEST price seen on any day before j. So as we scan
     *   left to right, keep:
     *       minPrice  = cheapest price seen so far (best day to have bought)
     *       best      = best profit achievable if we sell today
     *   For each price p:
     *       best     = max(best, p - minPrice)   // sell today vs. previous best
     *       minPrice = min(minPrice, p)          // update cheapest buy point
     *
     *   Because minPrice only ever looks at prior days, the "sell in the
     *   future" constraint is automatically respected.
     *
     * Time  : O(n)   -- single pass
     * Space : O(1)   -- two scalars
     */
    public int maxProfit(int[] prices) {
        int minPrice = Integer.MAX_VALUE;   // cheapest price seen so far
        int best = 0;                       // max profit so far (>= 0)
        for (int p : prices) {
            best = Math.max(best, p - minPrice);  // profit if we sell today
            minPrice = Math.min(minPrice, p);     // maybe today is a better buy day
        }
        return best;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "What if you can make AS MANY transactions as you like?" (LeetCode 122)
     *      -> Greedy: sum every positive day-to-day delta
     *         (add max(0, prices[i] - prices[i-1])). Captures every up-slope.
     *         O(n) time, O(1) space.
     *
     *  F2. "At most TWO transactions?" (LeetCode 123)
     *      -> DP with four states: buy1, sell1, buy2, sell2, updated each day.
     *         buy1 = max(buy1, -p); sell1 = max(sell1, buy1 + p); etc. O(n)/O(1).
     *
     *  F3. "At most K transactions?" (LeetCode 188)
     *      -> Generalize F2 to arrays buy[k], sell[k]. O(nK) time, O(K) space
     *         (if K >= n/2 it reduces to the unlimited case F1).
     *
     *  F4. "Add a transaction FEE or a COOLDOWN day." (LeetCode 714 / 309)
     *      -> State-machine DP (hold / sold / rest). Subtract the fee on sell,
     *         or forbid buying the day right after a sell for cooldown.
     *
     *  F5. "Also RETURN which days to buy and sell, not just the profit."
     *      -> Track the buy-day index that produced the current minPrice and,
     *         whenever `best` improves, record (buyDay, sellDay = current day).
     *
     *  F6. "Why does tracking only the running minimum suffice — no future info?"
     *      -> The optimal sell on day j pairs with the min price in [0, j).
     *         Scanning left to right, minPrice already holds exactly that, so a
     *         single pass considers every day as a candidate sell against its
     *         own best prior buy.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] prices, BestTimeToBuyAndSellStock sol) {
        System.out.println(label + " " + Arrays.toString(prices)
                + " -> optimal=" + sol.maxProfit(prices)
                + ", brute=" + sol.maxProfitBruteForce(prices));
    }

    public static void main(String[] args) {
        BestTimeToBuyAndSellStock sol = new BestTimeToBuyAndSellStock();

        // Example 1
        report("Example 1 ->", new int[]{7, 1, 5, 3, 6, 4}, sol);  // 5
        // Example 2
        report("Example 2 ->", new int[]{7, 6, 4, 3, 1}, sol);     // 0

        // Edge cases
        report("Single day       ->", new int[]{5}, sol);              // 0
        report("Monotonic up     ->", new int[]{1, 2, 3, 4, 5}, sol);  // 4
        report("All equal        ->", new int[]{3, 3, 3}, sol);        // 0
        report("Dip then peak    ->", new int[]{2, 4, 1, 7}, sol);     // 6 (buy 1, sell 7)
        report("Min at end       ->", new int[]{9, 8, 2, 6, 1}, sol);  // 4 (buy 2, sell 6)
    }
}
