package Leetcode150.LinkedList;
/*
 * ============================================================================
 * LeetCode 206. Reverse Linked List                            [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given the head of a singly linked list, reverse the list, and return the
 * reversed list.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   The number of nodes in the list is the range [0, 5000].
 *   -5000 <= Node.val <= 5000
 *
 *   Follow up: A linked list can be reversed either iteratively or
 *   recursively. Could you implement both?
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  head = [1,2,3,4,5]
 *   Output: [5,4,3,2,1]
 *
 *     before: 1 -> 2 -> 3 -> 4 -> 5 -> null
 *     after:  null <- 1 <- 2 <- 3 <- 4 <- 5      (5 is the new head)
 *
 * Example 2:
 *   Input:  head = [1,2]
 *   Output: [2,1]
 *
 * Example 3:
 *   Input:  head = []
 *   Output: []
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Should I reverse the LINKS, or is swapping the values acceptable?
 *      -> Reverse the links. Swapping values "looks" right but isn't what's
 *         asked, and is wrong when nodes carry other data or are referenced
 *         elsewhere.
 *
 *  Q2. In place, or may I build a new list?
 *      -> In place with O(1) extra space is the expected answer. A new list
 *         or an auxiliary array is the brute-force baseline.
 *
 *  Q3. Empty list or a single node?
 *      -> Both are allowed. Each is its own reverse: return head unchanged.
 *
 *  Q4. Singly linked, so no prev pointers?
 *      -> Yes, singly linked. That's the whole difficulty: once I redirect
 *         curr.next, I lose the rest of the list unless I saved it first.
 *
 *  Q5. Return value?
 *      -> The NEW head, which is the old tail.
 *
 *  Q6. Can the list contain a cycle?
 *      -> No. (With a cycle, "reverse" isn't well defined and the loop
 *         wouldn't terminate.)
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

public class ReverseLinkedList {

    // Standard LeetCode singly linked list node.
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE: store the nodes in an array, relink backward
     * ------------------------------------------------------------------------
     * Idea:
     *   Random access makes reversing trivial. Copy every node reference into
     *   an ArrayList, then walk it from the back, pointing each node at the one
     *   before it. The old first node becomes the tail, so its next is null.
     *
     *   This still reverses LINKS (not values), but uses extra memory.
     *
     * Time  : O(n)
     * Space : O(n)  -- the list of node references
     */
    public ListNode reverseListBruteForce(ListNode head) {
        if (head == null) return null;

        List<ListNode> nodes = new ArrayList<>();
        for (ListNode c = head; c != null; c = c.next) nodes.add(c);

        for (int i = nodes.size() - 1; i > 0; i--) {
            nodes.get(i).next = nodes.get(i - 1);
        }
        nodes.get(0).next = null;                      // old head is the new tail
        return nodes.get(nodes.size() - 1);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: ITERATIVE, three pointers  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Walk the list once with prev (already reversed part) and curr (next
     *   node to flip). For each node:
     *     1. next = curr.next     save the rest of the list BEFORE breaking it
     *     2. curr.next = prev     flip the link
     *     3. prev = curr          the reversed part grows by one
     *     4. curr = next          move on
     *   When curr is null, prev is the new head.
     *
     *   Trace on 1 -> 2 -> 3:
     *     prev=null, curr=1  ->  1->null          prev=1, curr=2
     *                        ->  2->1->null       prev=2, curr=3
     *                        ->  3->2->1->null    prev=3, curr=null  => return 3
     *
     * Time  : O(n)
     * Space : O(1)
     */
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;                 // 1. save the rest
            curr.next = prev;                          // 2. flip the link
            prev = curr;                               // 3. grow the reversed part
            curr = next;                               // 4. advance
        }
        return prev;                                   // new head (null for empty list)
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — RECURSIVE (the follow-up)
     * ------------------------------------------------------------------------
     * Idea:
     *   Trust the recursion to reverse everything AFTER head and return the
     *   new head (the old tail). At that point head.next still points to the
     *   node that is now the LAST node of the reversed part, so:
     *     head.next.next = head   hang head after it
     *     head.next      = null   head is the new tail (prevents a 2-node cycle)
     *   Base case: an empty or one-node list is already reversed.
     *
     *   The newHead returned from the deepest call is passed back unchanged
     *   through every level.
     *
     * Time  : O(n)
     * Space : O(n)  -- recursion depth; 5000 frames is fine, but very long
     *                  lists can overflow the stack, so iterative is safer.
     */
    public ListNode reverseListRecursive(ListNode head) {
        if (head == null || head.next == null) return head;

        ListNode newHead = reverseListRecursive(head.next);
        head.next.next = head;                         // the node after head now points back to it
        head.next = null;                              // head becomes the tail
        return newHead;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "What breaks if you skip saving next first?"
     *      -> After curr.next = prev, the original curr.next is gone: the rest
     *         of the list is unreachable and the loop stops after one node.
     *         Always save before you overwrite.
     *
     *  F2. "In the recursive version, why is head.next = null needed?"
     *      -> Without it, the old head still points to the second node while
     *         the second node points back to it: a 2-node cycle at the tail.
     *
     *  F3. "Reverse only positions left..right (LeetCode 92)?"
     *      -> Walk to the node BEFORE position left (use a dummy head in case
     *         left = 1), then reverse the next (right - left + 1) nodes with the
     *         same three-pointer loop and reconnect both ends. One pass, O(1).
     *
     *  F4. "Reverse in groups of k (LeetCode 25)?"
     *      -> Repeatedly check that k nodes remain, reverse that block with
     *         this loop, and connect the previous block's tail to the new block
     *         head. Leftover nodes (< k) stay as they are. O(n), O(1).
     *
     *  F5. "Check if a list is a palindrome in O(1) space (LeetCode 234)?"
     *      -> Find the middle with slow/fast pointers, reverse the second half
     *         with this function, compare the halves node by node, then
     *         (politely) reverse it back to restore the input.
     *
     *  F6. "Reverse a DOUBLY linked list?"
     *      -> For each node, swap its next and prev pointers, then move to
     *         what is now node.prev (the old next). The last node visited is
     *         the new head.
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

    private static void report(String label, int[] vals, ReverseLinkedList sol) {
        System.out.println(label + " head=" + show(build(vals))
                + " -> iterative=" + show(sol.reverseList(build(vals)))
                + ", recursive=" + show(sol.reverseListRecursive(build(vals)))
                + ", brute=" + show(sol.reverseListBruteForce(build(vals))));
    }

    public static void main(String[] args) {
        ReverseLinkedList sol = new ReverseLinkedList();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 4, 5}, sol);   // [5,4,3,2,1]
        report("Example 2 ->", new int[]{1, 2}, sol);            // [2,1]
        report("Example 3 ->", new int[]{}, sol);                // []

        // Edge cases
        report("Single node   ->", new int[]{7}, sol);           // [7]
        report("Duplicates    ->", new int[]{1, 1, 2, 2}, sol);  // [2,2,1,1]
        report("Negatives     ->", new int[]{-5000, 0, 5000}, sol); // [5000,0,-5000]

        // Reversing twice must give back the original order.
        ListNode twice = sol.reverseList(sol.reverseList(build(new int[]{1, 2, 3})));
        System.out.println("Reverse twice -> " + show(twice));          // [1,2,3]

        // Max size: check the ends rather than printing 5000 values.
        int[] big = new int[5000];
        for (int i = 0; i < big.length; i++) big[i] = i;
        ListNode it = sol.reverseList(build(big));
        ListNode rec = sol.reverseListRecursive(build(big));      // 5000-deep recursion
        System.out.println("Max size (5000) -> iterative head=" + it.val
                + ", recursive head=" + rec.val);                 // 4999, 4999
    }
}
