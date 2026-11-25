package Leetcode150.LinkedList;
/*
 * ============================================================================
 * LeetCode 82. Remove Duplicates from Sorted List II          [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given the head of a SORTED linked list, delete ALL nodes that have duplicate
 * numbers, leaving only DISTINCT numbers from the original list. Return the
 * linked list sorted as well.
 *
 * (Unlike LeetCode 83, a value that appears more than once is removed
 * ENTIRELY, not reduced to one copy.)
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   The number of nodes in the list is in the range [0, 300].
 *   -100 <= Node.val <= 100
 *   The list is guaranteed to be sorted in ASCENDING order.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  head = [1,2,3,3,4,4,5]
 *   Output: [1,2,5]
 *   Explanation: 3 and 4 appear twice, so every 3 and every 4 is removed.
 *
 * Example 2:
 *   Input:  head = [1,1,1,2,3]
 *   Output: [2,3]
 *   Explanation: All three 1s are removed, including the HEAD, so the
 *                returned head is a different node.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Do I keep one copy of a duplicated value, or remove all copies?
 *      -> Remove ALL copies. [3,3] contributes nothing. (Keeping one copy is
 *         LeetCode 83 — F1.)
 *
 *  Q2. Is the list really sorted?
 *      -> Yes, ascending. So equal values are always ADJACENT, and one run of
 *         equal nodes can be detected and skipped in a single pass.
 *
 *  Q3. Can the head be removed?
 *      -> Yes (Example 2). A dummy node before head gives the head a
 *         predecessor, so removing it needs no special case.
 *
 *  Q4. Can the result be empty?
 *      -> Yes: [1,1] or [1,1,2,2] -> []. And the input itself can be empty.
 *
 *  Q5. Relink nodes or build a new list?
 *      -> Relink the existing nodes, O(1) extra space.
 *
 *  Q6. Any trap?
 *      -> The usual one is advancing `prev` after a skipped run. prev must
 *         only move onto a node that is KNOWN to be distinct, because the
 *         node after a run might start another run ([1,1,2,2]).
 *
 * ============================================================================
 */

import java.util.HashMap;
import java.util.Map;

public class RemoveDuplicatesFromSortedListII {

    // Standard LeetCode singly linked list node.
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE: count every value, keep only count == 1
     * ------------------------------------------------------------------------
     * Idea:
     *   Pass 1: count how many times each value occurs (HashMap).
     *   Pass 2: walk the list again and link together only the nodes whose
     *           value has count 1, using a dummy + tail. Cap the tail with
     *           null so a removed node can't hang off the end.
     *
     *   Doesn't use the fact that the list is sorted, so it would also work
     *   for an UNSORTED list (F2). Costs O(n) extra space.
     *
     * Time  : O(n)  -- two passes
     * Space : O(n)  -- the count map
     */
    public ListNode deleteDuplicatesBruteForce(ListNode head) {
        Map<Integer, Integer> count = new HashMap<>();
        for (ListNode c = head; c != null; c = c.next) {
            count.merge(c.val, 1, Integer::sum);
        }

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (ListNode c = head; c != null; c = c.next) {
            if (count.get(c.val) == 1) {                       // Integer vs int: unboxes, safe
                tail.next = c;
                tail = c;
            }
        }
        tail.next = null;                                      // cut off any removed nodes after it
        return dummy.next;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: ONE PASS with dummy + prev, skip whole runs  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   prev is the last node KNOWN to be kept (starts at a dummy). curr scans.
     *     - If curr starts a run (curr.next has the same value): remember that
     *       value, advance curr past EVERY node with it, and link
     *       prev.next = curr. Do NOT move prev: the new curr might start
     *       another run (Q6).
     *     - Otherwise curr is distinct: keep it by moving prev = curr, then
     *       curr = curr.next.
     *
     *   Trace on [1,2,3,3,4,4,5]:
     *     1 distinct -> prev=1;  2 distinct -> prev=2
     *     run of 3s  -> skip to 4,  2.next = 4   (prev stays 2)
     *     run of 4s  -> skip to 5,  2.next = 5   (prev stays 2)
     *     5 distinct -> prev=5                   => [1,2,5]
     *
     * Time  : O(n)  -- each node is visited once
     * Space : O(1)
     */
    public ListNode deleteDuplicates(ListNode head) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prev = dummy;
        ListNode curr = head;

        while (curr != null) {
            if (curr.next != null && curr.val == curr.next.val) {
                int dup = curr.val;
                while (curr != null && curr.val == dup) curr = curr.next;   // skip the whole run
                prev.next = curr;                              // unlink it; prev stays put
            } else {
                prev = curr;                                   // curr is distinct: keep it
                curr = curr.next;
            }
        }
        return dummy.next;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — RECURSIVE
     * ------------------------------------------------------------------------
     * Idea:
     *   - Empty or single node: nothing to remove, return it.
     *   - If head starts a run: skip the whole run and return the answer for
     *     whatever comes after it (head is dropped).
     *   - Otherwise head is distinct: keep it, and set head.next to the answer
     *     for the rest.
     *
     * Time  : O(n)
     * Space : O(n)  -- recursion depth (one frame per kept node or run)
     */
    public ListNode deleteDuplicatesRecursive(ListNode head) {
        if (head == null || head.next == null) return head;

        if (head.val == head.next.val) {
            int dup = head.val;
            ListNode curr = head;
            while (curr != null && curr.val == dup) curr = curr.next;
            return deleteDuplicatesRecursive(curr);            // drop the entire run
        }
        head.next = deleteDuplicatesRecursive(head.next);      // head is distinct: keep it
        return head;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Keep ONE copy of each value instead (LeetCode 83)?"
     *      -> Simpler: no dummy needed, since the head is never removed. For
     *         each node, while curr.next has the same value, set
     *         curr.next = curr.next.next; then advance curr.
     *
     *  F2. "What if the list is NOT sorted?"
     *      -> Duplicates are no longer adjacent, so skipping runs doesn't work.
     *         Use the counting approach (Approach 1): O(n) time, O(n) space.
     *         Without extra space you'd sort the list first (merge sort,
     *         LeetCode 148), O(n log n).
     *
     *  F3. "Why not move prev after removing a run?"
     *      -> The node after a run isn't known to be distinct yet. With
     *         [1,1,2,2], moving prev onto the first 2 would keep it even
     *         though it's duplicated. prev only advances in the "distinct"
     *         branch.
     *
     *  F4. "Keep values that appear at most k times, remove the rest?"
     *      -> Same run detection: measure each run's length, and link the run
     *         in (prev = its last node) if length <= k, else skip it.
     *
     *  F5. "Same problem on a sorted ARRAY?"
     *      -> Write pointer: copy nums[i] forward only if it differs from both
     *         neighbors (nums[i-1] and nums[i+1]). Compare with LeetCode 26/80,
     *         which keep one/two copies.
     *
     *  F6. "Do the removed nodes need freeing?"
     *      -> Not in Java (garbage collected). In C/C++ you'd free each skipped
     *         node inside the skip loop.
     */

    // ------------------------------------------------------------------------
    // Driver. Every approach relinks the nodes, so each one gets a freshly
    // built list.
    // ------------------------------------------------------------------------
    private static ListNode build(int[] vals) {
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (int v : vals) {
            tail.next = new ListNode(v);
            tail = tail.next;
        }
        return dummy.next;
    }

    private static String show(ListNode head) {
        StringBuilder sb = new StringBuilder("[");
        for (ListNode c = head; c != null; c = c.next) {
            sb.append(c.val);
            if (c.next != null) sb.append(',');
        }
        return sb.append(']').toString();
    }

    private static void report(String label, int[] vals, RemoveDuplicatesFromSortedListII sol) {
        System.out.println(label + " head=" + show(build(vals))
                + " -> onePass=" + show(sol.deleteDuplicates(build(vals)))
                + ", recursive=" + show(sol.deleteDuplicatesRecursive(build(vals)))
                + ", brute=" + show(sol.deleteDuplicatesBruteForce(build(vals))));
    }

    public static void main(String[] args) {
        RemoveDuplicatesFromSortedListII sol = new RemoveDuplicatesFromSortedListII();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 3, 4, 4, 5}, sol);   // [1,2,5]
        report("Example 2 ->", new int[]{1, 1, 1, 2, 3}, sol);         // [2,3]

        // Edge cases
        report("Empty list        ->", new int[]{}, sol);                  // []
        report("Single node       ->", new int[]{7}, sol);                 // [7]
        report("All duplicates    ->", new int[]{1, 1}, sol);              // []
        report("Back-to-back runs ->", new int[]{1, 1, 2, 2}, sol);        // [] (prev must not move)
        report("No duplicates     ->", new int[]{1, 2, 3}, sol);           // [1,2,3]
        report("Run at the tail   ->", new int[]{1, 2, 2}, sol);           // [1]
        report("Negatives + runs  ->", new int[]{-3, -3, -1, 0, 0, 0, 4}, sol); // [-1,4]
    }
}
