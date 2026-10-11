package Leetcode150.Stack;
/*
 * ============================================================================
 * LeetCode 155. Min Stack                                     [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Design a stack that supports push, pop, top, and retrieving the MINIMUM
 * element in CONSTANT time.
 *
 * Implement the MinStack class:
 *   - MinStack()           initializes the stack object.
 *   - void push(int val)   pushes the element val onto the stack.
 *   - void pop()           removes the element on the top of the stack.
 *   - int top()            gets the top element of the stack.
 *   - int getMin()         retrieves the minimum element in the stack.
 *
 * You must implement a solution with O(1) time complexity for EACH function.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   -2^31 <= val <= 2^31 - 1
 *   pop, top and getMin will always be called on NON-EMPTY stacks.
 *   At most 3 * 10^4 calls will be made to push, pop, top, and getMin.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:
 *     ["MinStack","push","push","push","getMin","pop","top","getMin"]
 *     [[],[-2],[0],[-3],[],[],[],[]]
 *   Output:
 *     [null,null,null,null,-3,null,0,-2]
 *   Explanation:
 *     MinStack minStack = new MinStack();
 *     minStack.push(-2);
 *     minStack.push(0);
 *     minStack.push(-3);
 *     minStack.getMin(); // -3
 *     minStack.pop();
 *     minStack.top();    // 0
 *     minStack.getMin(); // -2   <- the min must "come back" after -3 is popped
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Do ALL operations need to be O(1), including getMin?
 *      -> Yes, every one. So scanning the stack for the min is not allowed.
 *
 *  Q2. Will pop/top/getMin ever be called on an empty stack?
 *      -> No, guaranteed non-empty. I won't add error handling for that, but
 *         I'd mention throwing an exception in production code.
 *
 *  Q3. Can values repeat, including the minimum?
 *      -> Yes. This matters: if -3 is pushed twice and popped once, the min is
 *         still -3. A min-tracking stack must handle duplicates (push on <=,
 *         not just <).
 *
 *  Q4. Value range?
 *      -> The full int range. Any trick that stores differences like
 *         val - min can OVERFLOW an int, so that version needs long.
 *
 *  Q5. Is the hard part getMin after a POP?
 *      -> Yes. Tracking a single "current min" variable is easy on push, but
 *         once the min is popped you have to know the previous min without
 *         rescanning. That's the whole problem.
 *
 *  Q6. Thread safety?
 *      -> Not required (single-threaded, like java.util.ArrayDeque).
 *
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.Deque;

public class MinStack {

    // Common interface so the driver can run the same script on every version.
    interface MinStackApi {
        void push(int val);
        void pop();
        int top();
        int getMin();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (plain stack, scan for min)
     * ------------------------------------------------------------------------
     * Idea:
     *   A normal stack. getMin walks every element to find the smallest.
     *
     * Time  : push/pop/top O(1), getMin O(n)   -- fails the O(1) requirement
     * Space : O(n)
     *
     * Worth stating first so the interviewer sees you know exactly which
     * operation is the problem.
     */
    static class BruteForceMinStack implements MinStackApi {
        private final Deque<Integer> stack = new ArrayDeque<>();

        public void push(int val) { stack.push(val); }
        public void pop()         { stack.pop(); }
        public int top()          { return stack.peek(); }

        public int getMin() {
            int min = Integer.MAX_VALUE;
            for (int v : stack) min = Math.min(min, v);
            return min;
        }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — STACK OF PAIRS (value, min-so-far)  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   With each value, store the minimum of the stack AT THE MOMENT it was
     *   pushed: min(val, previous top's min). Then the top pair always knows
     *   the current min, and popping it automatically "restores" the min that
     *   was in effect before — it's sitting in the new top pair.
     *
     *   This is the simplest correct O(1) design and handles duplicates with
     *   no special cases.
     *
     * Time  : O(1) for every operation
     * Space : O(n)  -- two ints per element
     */
    static class PairMinStack implements MinStackApi {
        private final Deque<int[]> stack = new ArrayDeque<>();   // {value, minSoFar}

        public void push(int val) {
            int min = stack.isEmpty() ? val : Math.min(val, stack.peek()[1]);
            stack.push(new int[]{val, min});
        }

        public void pop()    { stack.pop(); }
        public int top()     { return stack.peek()[0]; }
        public int getMin()  { return stack.peek()[1]; }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — TWO STACKS (main + min stack, push min only when <=)
     * ------------------------------------------------------------------------
     * Idea:
     *   Keep the main stack plus a second stack of minimums. Push onto the min
     *   stack only when the new value is <= the current min; pop from it only
     *   when the value being popped EQUALS the current min.
     *
     *   Saves space when the min rarely changes (e.g. pushing increasing
     *   values only stores one min). The '<=' is essential: with '<', pushing
     *   -3 twice stores it once, and the first pop would wrongly drop it.
     *
     * Time  : O(1) for every operation
     * Space : O(n) worst case, often much less
     */
    static class TwoStackMinStack implements MinStackApi {
        private final Deque<Integer> stack = new ArrayDeque<>();
        private final Deque<Integer> mins = new ArrayDeque<>();

        public void push(int val) {
            stack.push(val);
            if (mins.isEmpty() || val <= mins.peek()) {   // '<=' keeps duplicate mins
                mins.push(val);
            }
        }

        public void pop() {
            int val = stack.pop();                        // unbox: compare ints, not Integer refs
            if (val == mins.peek()) {
                mins.pop();
            }
        }

        public int top()    { return stack.peek(); }
        public int getMin() { return mins.peek(); }
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 4 — ONE STACK + ONE VARIABLE (store differences from the min)
     * ------------------------------------------------------------------------
     * Idea:
     *   Keep a single `min` variable and store each value as diff = val - min
     *   (computed BEFORE updating min).
     *     - push: if diff < 0, the new value is the new min, so set min = val.
     *       The negative diff now encodes the OLD min: oldMin = val - diff.
     *     - pop:  if the popped diff < 0, the top WAS the min; restore
     *       min = min - diff (that's the old min).
     *     - top:  if diff < 0 the top is the current min, else it's min + diff.
     *   Stores one number per element and no second structure.
     *
     *   Differences of two ints can reach about 2^32, so everything is long.
     *
     * Time  : O(1) for every operation
     * Space : O(n)  -- one long per element
     *
     * Clever, but easy to get wrong in an interview; mention it as the
     * "can you do it with one stack?" follow-up rather than leading with it.
     */
    static class DiffMinStack implements MinStackApi {
        private final Deque<Long> stack = new ArrayDeque<>();
        private long min;

        public void push(int val) {
            if (stack.isEmpty()) {
                stack.push(0L);          // val - min where min == val
                min = val;
            } else {
                long diff = (long) val - min;
                stack.push(diff);
                if (diff < 0) min = val; // new minimum; old one is recoverable from diff
            }
        }

        public void pop() {
            long diff = stack.pop();
            if (diff < 0) min = min - diff;   // popped the min: restore the previous one
        }

        public int top() {
            long diff = stack.peek();
            return (int) (diff < 0 ? min : min + diff);
        }

        public int getMin() { return (int) min; }
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why doesn't a single 'currentMin' variable work?"
     *      -> Push is fine, but when the min is popped you don't know the
     *         previous min without rescanning. Every approach above is a way of
     *         remembering the min history.
     *
     *  F2. "In the two-stack version, why '<=' and not '<'?"
     *      -> Duplicates. Push -3, -3: with '<' the min stack holds one -3.
     *         Popping the first -3 also pops the min stack, so getMin is wrong
     *         while the second -3 is still there. '<=' stores both.
     *
     *  F3. "Any Java gotcha in the two-stack version?"
     *      -> Comparing stack.peek() == mins.peek() compares two Integer
     *         OBJECTS by reference. It works for -128..127 (cached) and then
     *         silently fails for bigger values. Unbox to int or use equals().
     *
     *  F4. "Why long in the difference version?"
     *      -> val - min can be as large as (2^31 - 1) - (-2^31) = 2^32 - 1,
     *         which overflows int and corrupts the stored values.
     *
     *  F5. "Support getMax too? Or a min QUEUE?"
     *      -> getMax: track max-so-far in the pair, or add a max stack. A min
     *         QUEUE: build a queue from two min-stacks (amortized O(1)), or use
     *         a monotonic deque as in Sliding Window Maximum (LC 239).
     *
     *  F6. "Space trade-offs between the approaches?"
     *      -> Pairs: always 2 ints per element, simplest. Two stacks: extra
     *         space only when the min changes. Differences: 1 long per element
     *         and no second structure, but the trickiest to get right.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the same operation script against every implementation.
    // ------------------------------------------------------------------------
    private static void runExample(String label, MinStackApi s) {
        StringBuilder out = new StringBuilder(label + " -> ");
        s.push(-2);
        s.push(0);
        s.push(-3);
        out.append("getMin=").append(s.getMin());          // -3
        s.pop();
        out.append(", top=").append(s.top());              // 0
        out.append(", getMin=").append(s.getMin());        // -2
        System.out.println(out);
    }

    private static void runDuplicates(String label, MinStackApi s) {
        StringBuilder out = new StringBuilder(label + " -> ");
        s.push(5);
        s.push(-3);
        s.push(-3);                                         // duplicate min
        s.pop();
        out.append("getMin after one pop=").append(s.getMin());  // -3 (still one left)
        s.pop();
        out.append(", after two pops=").append(s.getMin());      // 5
        System.out.println(out);
    }

    private static void runExtremes(String label, MinStackApi s) {
        StringBuilder out = new StringBuilder(label + " -> ");
        s.push(Integer.MAX_VALUE);
        s.push(Integer.MIN_VALUE);                          // diff overflows int without long
        out.append("getMin=").append(s.getMin());           // -2147483648
        out.append(", top=").append(s.top());               // -2147483648
        s.pop();
        out.append(", getMin=").append(s.getMin());         // 2147483647
        out.append(", top=").append(s.top());               // 2147483647
        System.out.println(out);
    }

    private static void runLargeDuplicates(String label, MinStackApi s) {
        // 1000 is outside the Integer cache, so a reference comparison would break here.
        StringBuilder out = new StringBuilder(label + " -> ");
        s.push(2000);
        s.push(1000);
        s.push(1000);
        s.pop();
        out.append("getMin=").append(s.getMin());           // 1000
        s.pop();
        out.append(", getMin=").append(s.getMin());         // 2000
        System.out.println(out);
    }

    public static void main(String[] args) {
        System.out.println("Example 1 (expected getMin=-3, top=0, getMin=-2)");
        runExample("  brute    ", new BruteForceMinStack());
        runExample("  pairs    ", new PairMinStack());
        runExample("  twoStacks", new TwoStackMinStack());
        runExample("  diffs    ", new DiffMinStack());

        System.out.println("Duplicate minimums (expected -3, then 5)");
        runDuplicates("  brute    ", new BruteForceMinStack());
        runDuplicates("  pairs    ", new PairMinStack());
        runDuplicates("  twoStacks", new TwoStackMinStack());
        runDuplicates("  diffs    ", new DiffMinStack());

        System.out.println("Int extremes (expected MIN, MIN, then MAX, MAX)");
        runExtremes("  brute    ", new BruteForceMinStack());
        runExtremes("  pairs    ", new PairMinStack());
        runExtremes("  twoStacks", new TwoStackMinStack());
        runExtremes("  diffs    ", new DiffMinStack());

        System.out.println("Large duplicate mins (expected 1000, then 2000)");
        runLargeDuplicates("  brute    ", new BruteForceMinStack());
        runLargeDuplicates("  pairs    ", new PairMinStack());
        runLargeDuplicates("  twoStacks", new TwoStackMinStack());
        runLargeDuplicates("  diffs    ", new DiffMinStack());
    }
}
