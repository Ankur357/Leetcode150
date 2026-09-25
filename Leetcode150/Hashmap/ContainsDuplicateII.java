package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 219. Contains Duplicate II                          [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an integer array nums and an integer k, return true if there are two
 * DISTINCT INDICES i and j in the array such that:
 *   - nums[i] == nums[j], and
 *   - abs(i - j) <= k.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= nums.length <= 10^5
 *   -10^9 <= nums[i] <= 10^9
 *   0 <= k <= 10^5
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [1,2,3,1], k = 3
 *   Output: true
 *   Explanation: nums[0] == nums[3] == 1 and abs(0 - 3) = 3 <= 3.
 *
 * Example 2:
 *   Input:  nums = [1,0,1,1], k = 1
 *   Output: true
 *   Explanation: nums[2] == nums[3] == 1 and abs(2 - 3) = 1 <= 1.
 *
 * Example 3:
 *   Input:  nums = [1,2,3,1,2,3], k = 2
 *   Output: false
 *   Explanation: The nearest equal pair (the two 1s) is 3 apart > k = 2.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Must the indices be DISTINCT?
 *      -> Yes, i != j. A single element is never a pair with itself.
 *
 *  Q2. Is it abs(i - j) <= k (inclusive) or strictly < k?
 *      -> Inclusive: abs(i - j) <= k.
 *
 *  Q3. What if k == 0?
 *      -> Then no valid pair exists (distinct indices can't be 0 apart), so the
 *         answer is always false. Handled naturally.
 *
 *  Q4. Do only VALUES need to match, or something else?
 *      -> Just equal values within an index distance of k.
 *
 *  Q5. Value range — negatives / large ints?
 *      -> Yes (+/-1e9). Values are used as keys/compared directly; no arithmetic
 *         that could overflow.
 *
 *  Q6. Return type?
 *      -> boolean: true iff such a close equal pair exists.
 *
 * ============================================================================
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContainsDuplicateII {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (check each element against the next k)
     * ------------------------------------------------------------------------
     * Idea:
     * For each i, compare nums[i] with nums[j] for j in (i, i+k]. If any match,
     * return true. Only look ahead k positions since abs(i - j) <= k.
     *
     * Time : O(n * k)
     * Space : O(1)
     *
     * Correct; degrades when k is large. The hash approaches make it O(n).
     */
    public boolean containsNearbyDuplicateBruteForce(int[] nums, int k) {
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j <= i + k && j < n; j++) {
                if (nums[i] == nums[j])
                    return true;
            }
        }
        return false;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — LAST-INDEX HASH MAP (value -> most recent index)
     * ------------------------------------------------------------------------
     * Idea:
     * Walk once, storing value -> last index seen. When we see a value again,
     * check the gap i - lastIndex; if <= k, return true. Otherwise UPDATE the
     * stored index to the current one (the closest previous occurrence is what
     * matters for future matches).
     *
     * Time : O(n)
     * Space : O(n) -- up to n distinct values
     */
    public boolean containsNearbyDuplicateMap(int[] nums, int k) {
        Map<Integer, Integer> lastIndex = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer prev = lastIndex.get(nums[i]);
            if (prev != null && i - prev <= k) {
                return true;
            }
            lastIndex.put(nums[i], i); // keep only the most recent index
        }
        return false;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL: SLIDING-WINDOW HASH SET (size <= k) <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     * Maintain a set of the values in the current window of the last k indices.
     * For each i:
     * - If nums[i] is already in the window set, there is a duplicate within
     * distance k -> return true.
     * - Add nums[i] to the set.
     * - If the window now exceeds size k, remove the element that just fell
     * out of range: nums[i - k].
     * The set holds at most k elements, so a membership hit directly means a
     * nearby duplicate.
     *
     * Time : O(n)
     * Space : O(min(n, k)) -- window holds at most k values
     */
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Set<Integer> window = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            if (window.contains(nums[i])) {
                return true; // duplicate within the last k indices
            }
            window.add(nums[i]);
            if (window.size() > k) { // shrink: drop the element leaving the window
                window.remove(nums[i - k]);
            }
        }
        return false;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Map-of-last-index vs. sliding-window set — which is better?"
     * -> Both O(n) time. The set uses O(min(n, k)) space (bounded by the
     * window) vs the map's O(n). The set is the tighter-space answer;
     * the map is arguably simpler and also reports WHERE the match was.
     *
     * F2. "Why is it safe for the map to keep only the MOST RECENT index?"
     * -> For any future index j, the closest prior equal value gives the
     * smallest gap; if even that is > k, an older one is only farther.
     * So overwriting with the latest index never misses a valid pair.
     *
     * F3. "Handle k == 0 or k >= n?"
     * -> k == 0: window never holds a prior element (size cap 0) -> always
     * false, correct. k >= n: window never shrinks, so it's a plain
     * 'contains duplicate anywhere' check (LC 217).
     *
     * F4. "Relationship to LeetCode 217 (Contains Duplicate) and 220 (III)?"
     * -> 217 is k = infinity (any duplicate). 220 adds a VALUE tolerance
     * (|nums[i]-nums[j]| <= t) on top of the index window, needing a
     * sorted structure (TreeSet) or bucketing.
     *
     * F5. "What if we needed to RETURN the indices, not a boolean?"
     * -> Use the last-index map and return {prev, i} on the first hit.
     *
     * F6. "Very large stream where you can't hold all values?"
     * -> The sliding-window set already bounds memory to k, so it fits a
     * stream: add the new value, evict the one k positions back.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, int k, ContainsDuplicateII sol) {
        System.out.println(label + " nums=" + java.util.Arrays.toString(nums) + ", k=" + k
                + " -> window=" + sol.containsNearbyDuplicate(nums, k)
                + ", map=" + sol.containsNearbyDuplicateMap(nums, k)
                + ", brute=" + sol.containsNearbyDuplicateBruteForce(nums, k));
    }

    public static void main(String[] args) {
        ContainsDuplicateII sol = new ContainsDuplicateII();

        // Examples
        report("Example 1 ->", new int[] { 1, 2, 3, 1 }, 3, sol); // true
        report("Example 2 ->", new int[] { 1, 0, 1, 1 }, 1, sol); // true
        report("Example 3 ->", new int[] { 1, 2, 3, 1, 2, 3 }, 2, sol); // false

        // Edge cases
        report("k == 0          ->", new int[] { 1, 1 }, 0, sol); // false (distinct idx)
        report("Adjacent dup    ->", new int[] { 99, 99 }, 1, sol); // true
        report("No duplicates   ->", new int[] { 1, 2, 3, 4, 5 }, 5, sol); // false
        report("Dup just outside->", new int[] { 1, 2, 1 }, 1, sol); // false (gap 2 > 1)
        report("Dup just inside ->", new int[] { 1, 2, 1 }, 2, sol); // true (gap 2 <= 2)
        report("Negatives       ->", new int[] { -1, 3, -1 }, 2, sol); // true
        report("Large k         ->", new int[] { 1, 2, 3, 1 }, 100, sol); // true (k >= n)
    }
}
