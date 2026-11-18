package Leetcode150.LinkedList;
/*
 * ============================================================================
 * LeetCode 92. Reverse Linked List II                         [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given the head of a singly linked list and two integers left and right where
 * left <= right, reverse the nodes of the list from position left to position
 * right, and return the reversed list.
 *
 * Positions are 1-INDEXED.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   The number of nodes in the list is n.
 *   1 <= n <= 500
 *   -500 <= Node.val <= 500
 *   1 <= left <= right <= n
 *
 *   Follow up: Could you do it in ONE PASS?
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  head = [1,2,3,4,5], left = 2, right = 4
 *   Output: [1,4,3,2,5]
 *
 *     before: 1 -> [2 -> 3 -> 4] -> 5
 *     after:  1 -> [4 -> 3 -> 2] -> 5
 *
 * Example 2:
 *   Input:  head = [5], left = 1, right = 1
 *   Output: [5]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Are positions 1-indexed?
 *      -> Yes. left = 1 means the reversal starts at the head.
 *
 *  Q2. What if left == 1, so the HEAD itself changes?
 *      -> Then the returned head is a different node. A dummy node in front
 *         of head means there is always a "node before left", so this needs
 *         no special case.
 *
 *  Q3. What if left == right?
 *      -> Reversing one node changes nothing; return head as is.
 *
 *  Q4. Are left and right always valid (1 <= left <= right <= n)?
 *      -> Yes, guaranteed. So no bounds checks while walking.
 *
 *  Q5. Reverse the links or just swap values?
 *      -> Reverse the links, same as LeetCode 206. Swapping values is
 *         simpler but not what's being tested.
 *
 *  Q6. Is one pass required?
 *      -> It's the follow-up. A two-pass solution (find the segment, then
 *         reverse it) is fine to start with; the head-insertion trick does it
 *         in one.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReverseLinkedListII {

    // Standard LeetCode singly linked list node.
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE: node array, reverse the slice, relink all
     * ------------------------------------------------------------------------
     * Idea:
     *   Copy every node reference into an ArrayList. Reverse the slice of
     *   references for positions left..right (indices left-1 .. right-1),
     *   then relink the whole list in array order and cap the last node with
     *   null.
     *
     *   This reverses real links (not values), but needs O(n) extra memory and
     *   touches every node twice.
     *
     * Time  : O(n)
     * Space : O(n)  -- the array of node references
     */
    public ListNode reverseBetweenBruteForce(ListNode head, int left, int right) {
        List<ListNode> nodes = new ArrayList<>();
        for (ListNode c = head; c != null; c = c.next) nodes.add(c);

        Collections.reverse(nodes.subList(left - 1, right));    // subList end is exclusive

        for (int i = 0; i < nodes.size() - 1; i++) {
            nodes.get(i).next = nodes.get(i + 1);
        }
        nodes.get(nodes.size() - 1).next = null;
        return nodes.get(0);
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — CUT, REVERSE, RECONNECT (reuses LeetCode 206)
     * ------------------------------------------------------------------------
     * Idea:
     *   1. With a dummy before head, walk to `before`, the node just before
     *      position left.
     *   2. Walk on to `end`, the node at position right. Remember `after`,
     *      the node following it, and cut the segment off (end.next = null).
     *   3. Reverse the cut segment with the standard three-pointer loop. Its
     *      old first node (`start`) is now its last node, and `end` is first.
     *   4. Reconnect: before.next = end, start.next = after.
     *
     *   Easy to explain because it reuses the plain reversal. It walks the
     *   segment twice (once to find end, once to reverse), so it's not
     *   strictly one pass.
     *
     * Time  : O(n)
     * Space : O(1)
     */
    public ListNode reverseBetweenCutAndReverse(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        ListNode before = dummy;
        for (int i = 1; i < left; i++) before = before.next;   // node at position left-1

        ListNode start = before.next;                          // position left
        ListNode end = start;
        for (int i = left; i < right; i++) end = end.next;     // position right
        ListNode after = end.next;
        end.next = null;                                       // cut the segment off

        reverse(start);                                        // segment is now end ... start
        before.next = end;
        start.next = after;
        return dummy.next;
    }

    private static ListNode reverse(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL: ONE PASS, HEAD INSERTION  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Walk `prev` to the node before position left (dummy handles left = 1).
     *   Let `curr` be the node at position left. It will end up as the LAST
     *   node of the reversed segment, and it never moves; instead, the node
     *   right after it is repeatedly pulled out and inserted right after prev.
     *   Do this (right - left) times:
     *
     *     move       = curr.next      the node to pull forward
     *     curr.next  = move.next      unlink it
     *     move.next  = prev.next      insert it at the front of the segment
     *     prev.next  = move
     *
     *   Trace on [1,2,3,4,5], left=2, right=4  (prev = 1, curr = 2):
     *     pull 3:  1 -> 3 -> 2 -> 4 -> 5
     *     pull 4:  1 -> 4 -> 3 -> 2 -> 5      done after right - left = 2 pulls
     *
     *   Every node is visited once, and nothing is ever cut off, so there's
     *   no reconnecting step at the end.
     *
     * Time  : O(n)  -- one pass: left-1 steps to prev, then right-left pulls
     * Space : O(1)
     */
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        ListNode prev = dummy;
        for (int i = 1; i < left; i++) prev = prev.next;       // node before position left

        ListNode curr = prev.next;                             // stays put; becomes segment tail
        for (int i = 0; i < right - left; i++) {
            ListNode move = curr.next;
            curr.next = move.next;
            move.next = prev.next;
            prev.next = move;
        }
        return dummy.next;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why the dummy node?"
     *      -> When left = 1 there is no real node before the segment, and the
     *         head changes. The dummy gives every case a "node before left" and
     *         dummy.next is always the correct head to return.
     *
     *  F2. "In head insertion, why does curr never move?"
     *      -> curr is the original left node; after all pulls it's the
     *         segment's tail and still links to whatever follows the segment.
     *         That's why no final reconnect is needed.
     *
     *  F3. "Reverse in groups of k (LeetCode 25)?"
     *      -> Same head insertion, applied block by block: check that k nodes
     *         remain, do k - 1 pulls, then set prev = curr (the block's new
     *         tail) and continue. O(n), O(1).
     *
     *  F4. "Reverse every alternate group of k?"
     *      -> Same loop; after reversing a block, just walk prev forward k
     *         nodes without reversing, then reverse the next block.
     *
     *  F5. "What if left/right could be out of range?"
     *      -> Clamp right to n while walking (stop if curr.next is null) and
     *         return head unchanged if left > n.
     *
     *  F6. "Could you do it recursively?"
     *      -> Yes: recurse until left == 1 (shifting left and right down),
     *         then reverse the first `right` nodes, remembering the node after
     *         them as the successor. O(n) stack, so iterative is preferred.
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

    private static void report(String label, int[] vals, int left, int right, ReverseLinkedListII sol) {
        System.out.println(label + " head=" + show(build(vals)) + ", left=" + left + ", right=" + right
                + " -> onePass=" + show(sol.reverseBetween(build(vals), left, right))
                + ", cutReverse=" + show(sol.reverseBetweenCutAndReverse(build(vals), left, right))
                + ", brute=" + show(sol.reverseBetweenBruteForce(build(vals), left, right)));
    }

    public static void main(String[] args) {
        ReverseLinkedListII sol = new ReverseLinkedListII();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 4, 5}, 2, 4, sol);   // [1,4,3,2,5]
        report("Example 2 ->", new int[]{5}, 1, 1, sol);               // [5]

        // Edge cases
        report("Whole list        ->", new int[]{1, 2, 3, 4, 5}, 1, 5, sol); // [5,4,3,2,1]
        report("Prefix (head moves)->", new int[]{1, 2, 3, 4, 5}, 1, 3, sol); // [3,2,1,4,5]
        report("Suffix (to tail)  ->", new int[]{1, 2, 3, 4, 5}, 3, 5, sol); // [1,2,5,4,3]
        report("left == right     ->", new int[]{1, 2, 3}, 2, 2, sol);       // [1,2,3]
        report("Two nodes swapped ->", new int[]{3, 5}, 1, 2, sol);          // [5,3]
        report("Adjacent pair mid ->", new int[]{1, 2, 3, 4}, 2, 3, sol);    // [1,3,2,4]
    }
}
