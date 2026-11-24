package Leetcode150.LinkedList;
/*
 * ============================================================================
 * LeetCode 19. Remove Nth Node From End of List               [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given the head of a linked list, remove the n-th node from the END of the
 * list and return its head.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   The number of nodes in the list is sz.
 *   1 <= sz <= 30
 *   0 <= Node.val <= 100
 *   1 <= n <= sz
 *
 *   Follow up: Could you do this in ONE PASS?
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  head = [1,2,3,4,5], n = 2
 *   Output: [1,2,3,5]
 *   Explanation: Counting from the end, 5 is 1st and 4 is 2nd, so remove 4.
 *
 *       1 -> 2 -> 3 -> 4 -> 5          1 -> 2 -> 3 ------> 5
 *                       ^ remove                    (3.next = 5)
 *
 * Example 2:
 *   Input:  head = [1], n = 1
 *   Output: []
 *
 * Example 3:
 *   Input:  head = [1,2], n = 1
 *   Output: [1]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Is n 1-indexed from the end (n = 1 is the last node)?
 *      -> Yes.
 *
 *  Q2. Is n always valid (1 <= n <= length)?
 *      -> Yes, guaranteed. (If not, I'd return head unchanged — F5.)
 *
 *  Q3. What if n equals the length, so I remove the HEAD?
 *      -> Then the returned head changes. A dummy node in front of head gives
 *         the head a predecessor too, so it needs no special case.
 *
 *  Q4. Can the list become empty?
 *      -> Yes: [1] with n = 1 returns null.
 *
 *  Q5. To delete a node in a singly linked list, which node do I need?
 *      -> Its PREDECESSOR: prev.next = prev.next.next. So the real target is
 *         the (n+1)-th node from the end. That's why the gap below is n + 1.
 *
 *  Q6. One pass required?
 *      -> It's the follow-up. Two passes (count, then walk) is the obvious
 *         start; two pointers with a fixed gap does it in one.
 *
 * ============================================================================
 */

public class RemoveNthNodeFromEndOfList {

    // Standard LeetCode singly linked list node.
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE: TWO PASSES (count the length first)
     * ------------------------------------------------------------------------
     * Idea:
     *   The n-th node from the end is the (len - n + 1)-th from the start, so
     *   its predecessor is (len - n) steps after a dummy node.
     *     Pass 1: count len.
     *     Pass 2: walk len - n steps from the dummy, then unlink the next node.
     *
     *   Already O(n) time and O(1) space; it just reads the list twice, which
     *   is what the follow-up asks to avoid.
     *
     * Time  : O(L)  -- two passes over L nodes
     * Space : O(1)
     */
    public ListNode removeNthFromEndTwoPass(ListNode head, int n) {
        int len = 0;
        for (ListNode c = head; c != null; c = c.next) len++;

        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prev = dummy;
        for (int i = 0; i < len - n; i++) prev = prev.next;    // predecessor of the target

        prev.next = prev.next.next;
        return dummy.next;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: ONE PASS, TWO POINTERS with a gap  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Start fast and slow at a dummy node. Move fast n + 1 steps ahead. Now
     *   move both together until fast falls off the end (null). Because the
     *   gap stays n + 1, slow stops exactly at the node BEFORE the n-th from
     *   the end, which is the predecessor we need (Q5).
     *
     *   Trace on [1,2,3,4,5], n = 2:
     *     fast moves 3 steps: dummy -> 1 -> 2 -> 3      (fast = 3, slow = dummy)
     *     together:  fast 4, 5, null   slow 1, 2, 3     (slow = 3)
     *     3.next = 3.next.next  -> removes 4  => [1,2,3,5]
     *
     *   The dummy makes removing the head work too: for n = length, slow never
     *   moves off the dummy, and dummy.next = head.next.
     *
     * Time  : O(L)  -- one pass
     * Space : O(1)
     */
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode fast = dummy;
        ListNode slow = dummy;

        for (int i = 0; i <= n; i++) fast = fast.next;         // n + 1 steps ahead

        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }
        slow.next = slow.next.next;                            // unlink the n-th from the end
        return dummy.next;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — RECURSIVE: count positions on the way back up
     * ------------------------------------------------------------------------
     * Idea:
     *   Recurse to the end of the list. On the way back, each call returns its
     *   position counted from the end (null = 0, last node = 1, ...). The node
     *   whose position is n + 1 is the predecessor: it unlinks its next.
     *   Starting the recursion at a dummy handles removing the head, since
     *   the dummy's position is len + 1.
     *
     *   Also a single pass, but O(L) stack. A nice way to show you understand
     *   "counting from the end", not the one to ship.
     *
     * Time  : O(L)
     * Space : O(L)  -- recursion depth
     */
    public ListNode removeNthFromEndRecursive(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        positionFromEnd(dummy, n);
        return dummy.next;
    }

    private int positionFromEnd(ListNode node, int n) {
        if (node == null) return 0;
        int pos = positionFromEnd(node.next, n) + 1;
        if (pos == n + 1) node.next = node.next.next;          // node is the predecessor
        return pos;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why move fast n + 1 steps and not n?"
     *      -> With a gap of n, slow would stop ON the target node, but deleting
     *         needs the node before it. One extra step leaves slow on the
     *         predecessor. (Alternative: gap n, loop while fast.next != null.)
     *
     *  F2. "Why the dummy node?"
     *      -> When n equals the length, the head itself is removed and has no
     *         predecessor. The dummy gives it one, and dummy.next is always the
     *         correct new head.
     *
     *  F3. "Is the two-pointer version really faster than two passes?"
     *      -> Both are O(L) and touch about the same number of nodes; the
     *         one-pass version just never needs the length. It matters when
     *         the list is a stream you can only read once.
     *
     *  F4. "Find (not remove) the n-th node from the end, or the middle node?"
     *      -> n-th from end: gap of n, return slow when fast hits null. Middle
     *         (LeetCode 876): slow 1 step, fast 2 steps; slow is the middle when
     *         fast reaches the end.
     *
     *  F5. "What if n could be larger than the length?"
     *      -> While moving fast ahead, if it becomes null too early (before
     *         finishing n steps from the head), n is invalid: return head
     *         unchanged.
     *
     *  F6. "In Java, does the removed node need cleanup?"
     *      -> No: once nothing references it, the garbage collector frees it.
     *         In C/C++ you'd keep a pointer to it and free/delete it.
     */

    // ------------------------------------------------------------------------
    // Driver. Every approach unlinks a node, so each one gets a freshly built
    // list.
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

    private static void report(String label, int[] vals, int n, RemoveNthNodeFromEndOfList sol) {
        System.out.println(label + " head=" + show(build(vals)) + ", n=" + n
                + " -> twoPointers=" + show(sol.removeNthFromEnd(build(vals), n))
                + ", recursive=" + show(sol.removeNthFromEndRecursive(build(vals), n))
                + ", twoPass=" + show(sol.removeNthFromEndTwoPass(build(vals), n)));
    }

    public static void main(String[] args) {
        RemoveNthNodeFromEndOfList sol = new RemoveNthNodeFromEndOfList();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 4, 5}, 2, sol);    // [1,2,3,5]
        report("Example 2 ->", new int[]{1}, 1, sol);                // []
        report("Example 3 ->", new int[]{1, 2}, 1, sol);             // [1]

        // Edge cases
        report("Remove head (n=len)->", new int[]{1, 2}, 2, sol);           // [2]
        report("Remove head, longer->", new int[]{1, 2, 3, 4, 5}, 5, sol);  // [2,3,4,5]
        report("Remove tail        ->", new int[]{1, 2, 3, 4, 5}, 1, sol);  // [1,2,3,4]
        report("Remove middle      ->", new int[]{1, 2, 3, 4, 5}, 3, sol);  // [1,2,4,5]
        report("Duplicate values   ->", new int[]{7, 7, 7}, 2, sol);        // [7,7]
    }
}
