package Leetcode150.LinkedList;
/*
 * ============================================================================
 * LeetCode 61. Rotate List                                    [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given the head of a linked list, rotate the list to the RIGHT by k places.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   The number of nodes in the list is in the range [0, 500].
 *   -100 <= Node.val <= 100
 *   0 <= k <= 2 * 10^9
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  head = [1,2,3,4,5], k = 2
 *   Output: [4,5,1,2,3]
 *   Explanation:
 *     rotate 1 step:  [5,1,2,3,4]
 *     rotate 2 steps: [4,5,1,2,3]
 *
 *     Equivalently: the LAST k nodes move to the front, in order.
 *       1 -> 2 -> 3 | 4 -> 5      =>      4 -> 5 -> 1 -> 2 -> 3
 *
 * Example 2:
 *   Input:  head = [0,1,2], k = 4
 *   Output: [2,0,1]
 *   Explanation: Rotating 3 steps gives back [0,1,2], so 4 steps is the same
 *                as 4 % 3 = 1 step: [2,0,1].
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Rotate RIGHT means the tail moves to the front?
 *      -> Yes. Each step takes the last node and makes it the new head.
 *
 *  Q2. Can k be larger than the length?
 *      -> Yes, up to 2 * 10^9. Rotating by the length gives the same list,
 *         so only k % len matters. This is the key observation: without it,
 *         simulating 2 * 10^9 steps is hopeless.
 *
 *  Q3. Empty list, single node, or k == 0?
 *      -> All allowed, and all return head unchanged. Also k % len == 0
 *         (e.g. k equal to the length) returns head unchanged.
 *
 *  Q4. Relink nodes or build a new list?
 *      -> Relink, O(1) extra space. Values must stay in their nodes.
 *
 *  Q5. Does k fit in an int?
 *      -> Yes, 2 * 10^9 < 2^31 - 1 (about 2.147 * 10^9). And k % len is
 *         taken before any arithmetic, so nothing overflows.
 *
 *  Q6. Which node becomes the new tail?
 *      -> After k %= len, the node at position len - k (1-indexed) from the
 *         start. Its next is the new head, and the old tail links to the old
 *         head.
 *
 * ============================================================================
 */

public class RotateList {

    // Standard LeetCode singly linked list node.
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE: rotate one step at a time
     * ------------------------------------------------------------------------
     * Idea:
     *   A single right rotation: walk to the last node (and its predecessor),
     *   detach the last node and put it in front of head. Repeat k times.
     *
     *   Even this needs k %= len first. Without it, k = 2 * 10^9 means two
     *   billion O(n) walks. With it, at most n - 1 rotations remain.
     *
     * Time  : O(n * (k % n))  -- up to O(n^2)
     * Space : O(1)
     */
    public ListNode rotateRightBruteForce(ListNode head, int k) {
        if (head == null || head.next == null) return head;

        int len = 0;
        for (ListNode c = head; c != null; c = c.next) len++;
        k %= len;

        for (int r = 0; r < k; r++) {
            ListNode prev = null;
            ListNode curr = head;
            while (curr.next != null) {                        // find last node and its predecessor
                prev = curr;
                curr = curr.next;
            }
            prev.next = null;                                  // detach the last node
            curr.next = head;                                  // put it in front
            head = curr;
        }
        return head;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: CLOSE INTO A RING, then cut  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   1. Walk to the tail, counting len.
     *   2. k %= len. If k == 0 the list doesn't change (Q3).
     *   3. Link tail.next = head, making the list a ring.
     *   4. The new tail is (len - k - 1) steps after head. Its next is the new
     *      head. Cut the ring there: newTail.next = null.
     *
     *   Trace on [1,2,3,4,5], k = 2:
     *     len = 5, k = 2, ring: 1->2->3->4->5->1...
     *     new tail = 5 - 2 - 1 = 2 steps after head = node 3
     *     new head = 4, cut after 3  => [4,5,1,2,3]
     *
     *   Closing the ring means the old tail -> old head link is created
     *   automatically; we only have to choose where to break it.
     *
     * Time  : O(n)  -- one pass to count, at most one more to find the cut
     * Space : O(1)
     */
    public ListNode rotateRight(ListNode head, int k) {
        if (head == null || head.next == null || k == 0) return head;

        int len = 1;
        ListNode tail = head;
        while (tail.next != null) {
            tail = tail.next;
            len++;
        }

        k %= len;
        if (k == 0) return head;                               // full rotations: unchanged

        tail.next = head;                                      // close the ring
        ListNode newTail = head;
        for (int i = 0; i < len - k - 1; i++) newTail = newTail.next;

        ListNode newHead = newTail.next;
        newTail.next = null;                                   // break the ring
        return newHead;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — TWO POINTERS with a gap of k (after k %= len)
     * ------------------------------------------------------------------------
     * Idea:
     *   Same "last k nodes move to the front" view, found with the gap trick
     *   from LeetCode 19:
     *     - Count len and reduce k %= len (still needs one counting pass, since
     *       k can be huge).
     *     - Move fast k nodes ahead of slow, then move both until fast is the
     *       LAST node. slow is now the new tail.
     *     - newHead = slow.next, slow.next = null, fast.next = old head.
     *
     *   Same complexity as Approach 2; just a different way to find the cut.
     *
     * Time  : O(n)
     * Space : O(1)
     */
    public ListNode rotateRightTwoPointers(ListNode head, int k) {
        if (head == null || head.next == null) return head;

        int len = 0;
        for (ListNode c = head; c != null; c = c.next) len++;
        k %= len;
        if (k == 0) return head;

        ListNode fast = head;
        for (int i = 0; i < k; i++) fast = fast.next;          // fast is k ahead
        ListNode slow = head;
        while (fast.next != null) {                            // stop when fast is the tail
            fast = fast.next;
            slow = slow.next;
        }

        ListNode newHead = slow.next;
        slow.next = null;                                      // slow is the new tail
        fast.next = head;                                      // old tail -> old head
        return newHead;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why k % len?"
     *      -> Rotating by len puts every node back where it started, so only
     *         the remainder matters. It turns an impossible 2 * 10^9 steps into
     *         at most len - 1.
     *
     *  F2. "Why len - k - 1 steps to the new tail?"
     *      -> The new head is the (len - k + 1)-th node; the new tail is the one
     *         before it, the (len - k)-th, which is len - k - 1 steps from head.
     *
     *  F3. "Rotate LEFT by k instead?"
     *      -> Rotating left by k is rotating right by len - (k % len). Or
     *         directly: the new tail is (k % len) - 1 steps from head.
     *
     *  F4. "Same problem on an array (LeetCode 189)?"
     *      -> Arrays can't be relinked, so use the triple reversal: reverse
     *         all, reverse the first k, reverse the rest. O(n), O(1).
     *
     *  F5. "What if the list is doubly linked or already circular?"
     *      -> A circular list just needs a new head pointer: move it
     *         len - (k % len) steps forward. Nothing to cut or relink.
     *
     *  F6. "Can you do it without counting the length?"
     *      -> Not safely: k can be far larger than len, and you can't reduce
     *         k % len without knowing len. Counting is one O(n) pass anyway.
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

    private static void report(String label, int[] vals, int k, RotateList sol) {
        System.out.println(label + " head=" + show(build(vals)) + ", k=" + k
                + " -> ring=" + show(sol.rotateRight(build(vals), k))
                + ", twoPointers=" + show(sol.rotateRightTwoPointers(build(vals), k))
                + ", brute=" + show(sol.rotateRightBruteForce(build(vals), k)));
    }

    public static void main(String[] args) {
        RotateList sol = new RotateList();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 4, 5}, 2, sol);           // [4,5,1,2,3]
        report("Example 2 ->", new int[]{0, 1, 2}, 4, sol);                 // [2,0,1]

        // Edge cases
        report("Empty list        ->", new int[]{}, 3, sol);                    // []
        report("Single node       ->", new int[]{1}, 99, sol);                  // [1]
        report("k = 0             ->", new int[]{1, 2, 3}, 0, sol);             // [1,2,3]
        report("k = len           ->", new int[]{1, 2, 3}, 3, sol);             // [1,2,3]
        report("k = len - 1       ->", new int[]{1, 2, 3}, 2, sol);             // [2,3,1]
        report("Two nodes, k = 1  ->", new int[]{1, 2}, 1, sol);                // [2,1]
        report("Huge k (2e9)      ->", new int[]{1, 2, 3, 4, 5}, 2_000_000_000, sol); // 2e9 % 5 = 0 -> [1,2,3,4,5]
        report("Huge k, remainder ->", new int[]{1, 2, 3, 4, 5, 6, 7}, 1_999_999_999, sol); // % 7 = 4 -> [4,5,6,7,1,2,3]
    }
}
