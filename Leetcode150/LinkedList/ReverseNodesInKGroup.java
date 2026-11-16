package Leetcode150.LinkedList;
/*
 * ============================================================================
 * LeetCode 25. Reverse Nodes in k-Group                         [Difficulty: Hard]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given the head of a linked list, reverse the nodes of the list k at a time,
 * and return the modified list.
 *
 * k is a positive integer and is less than or equal to the length of the
 * linked list. If the number of nodes is not a multiple of k then the
 * left-out nodes, in the end, should REMAIN AS IT IS.
 *
 * You may not alter the values in the list's nodes, only nodes themselves may
 * be changed.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   The number of nodes in the list is n.
 *   1 <= k <= n <= 5000
 *   0 <= Node.val <= 1000
 *
 *   Follow-up: Can you solve the problem in O(1) extra memory space?
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  head = [1,2,3,4,5], k = 2
 *   Output: [2,1,4,3,5]
 *
 *     groups: [1,2] [3,4] [5]       <- [5] has fewer than k nodes: unchanged
 *     result: [2,1] [4,3] [5]
 *
 * Example 2:
 *   Input:  head = [1,2,3,4,5], k = 3
 *   Output: [3,2,1,4,5]
 *
 *     groups: [1,2,3] [4,5]          <- [4,5] is incomplete: unchanged
 *     result: [3,2,1] [4,5]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. What happens to the last group if it has fewer than k nodes?
 *      -> It stays in its original order. So before reversing a group I must
 *         CHECK that k nodes are available; I can't reverse first and undo.
 *
 *  Q2. Can I just swap values?
 *      -> No, explicitly forbidden. I must relink nodes.
 *
 *  Q3. What if k == 1?
 *      -> Every group is a single node, so the list is unchanged. The code
 *         should handle it naturally (each "reversal" of 1 node is a no-op).
 *
 *  Q4. What if k == n?
 *      -> One group: the whole list reversed, same as LeetCode 206.
 *
 *  Q5. Is k guaranteed to be in range?
 *      -> Yes, 1 <= k <= n, so there is always at least one full group and
 *         k is never 0.
 *
 *  Q6. Extra memory?
 *      -> The follow-up wants O(1). Recursion uses O(n/k) stack, so the
 *         expected answer is the iterative version.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReverseNodesInKGroup {

    // Standard LeetCode singly linked list node.
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE: node array, reverse each full block, relink
     * ------------------------------------------------------------------------
     * Idea:
     *   Copy every node reference into an ArrayList. For each FULL block of k
     *   (indices i .. i+k-1 with i + k <= size), reverse that slice. A partial
     *   last block fails the i + k <= size check and is left alone. Then
     *   relink everything in array order and cap the last node with null.
     *
     *   Relinks real nodes (values untouched), but uses O(n) memory.
     *
     * Time  : O(n)
     * Space : O(n)  -- the array of node references
     */
    public ListNode reverseKGroupBruteForce(ListNode head, int k) {
        List<ListNode> nodes = new ArrayList<>();
        for (ListNode c = head; c != null; c = c.next) nodes.add(c);

        for (int i = 0; i + k <= nodes.size(); i += k) {
            Collections.reverse(nodes.subList(i, i + k));      // only full blocks
        }

        for (int i = 0; i < nodes.size() - 1; i++) {
            nodes.get(i).next = nodes.get(i + 1);
        }
        nodes.get(nodes.size() - 1).next = null;
        return nodes.get(0);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: ITERATIVE, group by group, O(1) space  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Keep `groupPrev`: the node just BEFORE the current group (starts as a
     *   dummy, so the first group needs no special case). Repeat:
     *     1. kth = the node k steps after groupPrev. If it doesn't exist, fewer
     *        than k nodes remain: stop, leaving them as they are (Q1).
     *     2. groupNext = kth.next, the first node after this group.
     *     3. Reverse the group with the usual three-pointer loop, but start
     *        `prev` at groupNext instead of null. That way the group's old
     *        first node (its new last node) already points to the next group.
     *     4. The old first node is now the group's tail. Link groupPrev to kth
     *        (the group's new head) and make that tail the next groupPrev.
     *
     *   Trace on [1,2,3,4,5], k=2:
     *     groupPrev=dummy, kth=2, groupNext=3  -> dummy->2->1->3->4->5   groupPrev=1
     *     groupPrev=1,     kth=4, groupNext=5  -> dummy->2->1->4->3->5   groupPrev=3
     *     groupPrev=3,     kth=null (only 5 left)  -> stop
     *
     * Time  : O(n)  -- each node is walked once to find kth and once to reverse
     * Space : O(1)
     */
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode groupPrev = dummy;

        while (true) {
            ListNode kth = getKth(groupPrev, k);
            if (kth == null) break;                            // fewer than k left
            ListNode groupNext = kth.next;

            // Reverse the group; its old first node ends up pointing at groupNext.
            ListNode prev = groupNext;
            ListNode curr = groupPrev.next;
            while (curr != groupNext) {
                ListNode next = curr.next;
                curr.next = prev;
                prev = curr;
                curr = next;
            }

            ListNode groupTail = groupPrev.next;               // old first node = new tail
            groupPrev.next = kth;                              // kth is the new group head
            groupPrev = groupTail;
        }
        return dummy.next;
    }

    // Node k steps after `curr`, or null if the list runs out first.
    private static ListNode getKth(ListNode curr, int k) {
        while (curr != null && k > 0) {
            curr = curr.next;
            k--;
        }
        return curr;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — RECURSIVE (reverse the first group, recurse on the rest)
     * ------------------------------------------------------------------------
     * Idea:
     *   1. Walk k nodes from head. If fewer than k exist, return head as is.
     *      Otherwise `curr` is now the first node of the NEXT group.
     *   2. Recursively solve the rest: rest = reverseKGroup(curr, k).
     *   3. Reverse the first k nodes, starting prev at `rest` so the reversed
     *      group's tail links straight onto the already-solved remainder.
     *   4. Return prev, the new head of this group.
     *
     *   Short and clear, but uses one stack frame per group: O(n/k) space,
     *   which fails the follow-up (and is 5000 frames when k = 1).
     *
     * Time  : O(n)
     * Space : O(n/k)  -- recursion depth
     */
    public ListNode reverseKGroupRecursive(ListNode head, int k) {
        ListNode curr = head;
        int count = 0;
        while (curr != null && count < k) {
            curr = curr.next;
            count++;
        }
        if (count < k) return head;                            // incomplete group: unchanged

        ListNode prev = reverseKGroupRecursive(curr, k);       // solved remainder
        ListNode node = head;
        for (int i = 0; i < k; i++) {                          // reverse this group onto it
            ListNode next = node.next;
            node.next = prev;
            prev = node;
            node = next;
        }
        return prev;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why start prev at groupNext instead of null?"
     *      -> The first node of the group becomes its last, and it must point
     *         to the next group. Starting prev there makes the reversal connect
     *         that link for free, so there's no separate "fix the tail" step.
     *
     *  F2. "Why check for k nodes BEFORE reversing?"
     *      -> An incomplete last group must stay unchanged. If you reversed
     *         first, you'd have to detect the short group and reverse it back:
     *         more code and an extra pass.
     *
     *  F3. "What if the last partial group SHOULD be reversed too?"
     *      -> Drop the check: reverse up to k nodes per group, stopping at
     *         null. In the iterative version, groupNext is simply null for the
     *         last group.
     *
     *  F4. "Isn't this O(n) + O(n), walking each group twice?"
     *      -> Yes, every node is visited twice (getKth then reverse), which is
     *         still O(n). Counting the length once up front lets you skip
     *         getKth: process length / k full groups, no checks needed.
     *
     *  F5. "Reverse alternate groups only (reverse k, skip k, reverse k, ...)?"
     *      -> After reversing a group, advance groupPrev an extra k nodes
     *         without reversing them, then continue.
     *
     *  F6. "How does this relate to LeetCode 24 (Swap Nodes in Pairs)?"
     *      -> LC 24 is exactly this problem with k = 2.
     */

    // ------------------------------------------------------------------------
    // Driver. Every approach rewires the nodes, so each one gets a freshly
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

    private static void report(String label, int[] vals, int k, ReverseNodesInKGroup sol) {
        System.out.println(label + " head=" + show(build(vals)) + ", k=" + k
                + " -> iterative=" + show(sol.reverseKGroup(build(vals), k))
                + ", recursive=" + show(sol.reverseKGroupRecursive(build(vals), k))
                + ", brute=" + show(sol.reverseKGroupBruteForce(build(vals), k)));
    }

    public static void main(String[] args) {
        ReverseNodesInKGroup sol = new ReverseNodesInKGroup();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 4, 5}, 2, sol);         // [2,1,4,3,5]
        report("Example 2 ->", new int[]{1, 2, 3, 4, 5}, 3, sol);         // [3,2,1,4,5]

        // Edge cases
        report("k = 1 (unchanged) ->", new int[]{1, 2, 3}, 1, sol);       // [1,2,3]
        report("k = n (full rev)  ->", new int[]{1, 2, 3, 4}, 4, sol);    // [4,3,2,1]
        report("n multiple of k   ->", new int[]{1, 2, 3, 4, 5, 6}, 3, sol); // [3,2,1,6,5,4]
        report("Pairs, even length->", new int[]{1, 2, 3, 4}, 2, sol);    // [2,1,4,3]
        report("Long leftover     ->", new int[]{1, 2, 3, 4, 5, 6, 7}, 4, sol); // [4,3,2,1,5,6,7]
        report("Single node       ->", new int[]{9}, 1, sol);             // [9]
    }
}
