package Leetcode150.Stack;
/*
 * ============================================================================
 * LeetCode 150. Evaluate Reverse Polish Notation              [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an array of strings tokens that represents an arithmetic
 * expression in REVERSE POLISH NOTATION (postfix: operators come AFTER their
 * operands, e.g. "3 4 +" means 3 + 4).
 *
 * Evaluate the expression. Return an integer that represents the value of the
 * expression.
 *
 * Note that:
 *   - The valid operators are '+', '-', '*', and '/'.
 *   - Each operand may be an integer or another expression.
 *   - The division between two integers always TRUNCATES TOWARD ZERO.
 *   - There will not be any division by zero.
 *   - The input represents a VALID arithmetic expression in RPN.
 *   - The answer and all intermediate calculations can be represented in a
 *     32-bit integer.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= tokens.length <= 10^4
 *   tokens[i] is either an operator "+", "-", "*", "/", or an integer in the
 *   range [-200, 200].
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  tokens = ["2","1","+","3","*"]
 *   Output: 9
 *   Explanation: ((2 + 1) * 3) = 9
 *
 * Example 2:
 *   Input:  tokens = ["4","13","5","/","+"]
 *   Output: 6
 *   Explanation: (4 + (13 / 5)) = 4 + 2 = 6
 *
 * Example 3:
 *   Input:  tokens = ["10","6","9","3","+","-11","*","/","*","17","+","5","+"]
 *   Output: 22
 *   Explanation:
 *     ((10 * (6 / ((9 + 3) * -11))) + 17) + 5
 *     = ((10 * (6 / (12 * -11))) + 17) + 5
 *     = ((10 * (6 / -132)) + 17) + 5
 *     = ((10 * 0) + 17) + 5          <- 6 / -132 truncates toward zero to 0
 *     = 22
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. For '-' and '/', which popped value is the LEFT operand?
 *      -> The one pushed FIRST. "5 3 -" means 5 - 3, so pop b = 3, then a = 5,
 *         and compute a - b. Swapping them is the most common bug.
 *
 *  Q2. How should division round?
 *      -> Truncate toward zero: -7 / 2 = -3, not -4. Java's int '/' already
 *         does exactly that, so no extra work (Python's '//' would NOT).
 *
 *  Q3. How do I tell the operator "-" from a negative number like "-11"?
 *      -> An operator is exactly ONE character. "-11" has length 3, so it's a
 *         number. Checking startsWith("-") would be wrong.
 *
 *  Q4. Is the input guaranteed valid (no missing operands, no division by 0)?
 *      -> Yes. In production I'd check the stack size before each pop and
 *         that exactly one value remains at the end (F3).
 *
 *  Q5. Can intermediate results overflow?
 *      -> No, they're guaranteed to fit in 32 bits, so int is fine.
 *
 *  Q6. Can the input be a single number?
 *      -> Yes, e.g. ["42"] -> 42. The stack handles it with no special case.
 *
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class EvaluateReversePolishNotation {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (repeatedly collapse the first operator)
     * ------------------------------------------------------------------------
     * Idea:
     *   In valid RPN, the FIRST operator always has two plain numbers right
     *   before it. Replace those three tokens with their result, and repeat
     *   until one token is left.
     *
     *   e.g. [2, 1, +, 3, *] -> [3, 3, *] -> [9]
     *
     * Time  : O(n^2)  -- each step scans for an operator and shifts the list
     * Space : O(n)
     *
     * Shows you understand the evaluation order; the stack does the same thing
     * without rescanning.
     */
    public int evalRPNBruteForce(String[] tokens) {
        List<String> list = new ArrayList<>(Arrays.asList(tokens));
        while (list.size() > 1) {
            int i = 0;
            while (!isOperator(list.get(i))) i++;          // first operator

            int a = Integer.parseInt(list.get(i - 2));     // left operand
            int b = Integer.parseInt(list.get(i - 1));     // right operand
            int result = apply(list.get(i), a, b);

            list.set(i - 2, String.valueOf(result));
            list.remove(i);                                // remove operator first...
            list.remove(i - 1);                            // ...then the right operand
        }
        return Integer.parseInt(list.get(0));
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: OPERAND STACK  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Scan left to right:
     *     - Number:   push it.
     *     - Operator: pop b (right operand), then pop a (left operand), push
     *                 a <op> b.
     *   For valid input, exactly one value remains: the answer.
     *
     *   RPN was designed for this: the operands an operator needs are always
     *   the two most recent results, which is exactly what a stack's top holds.
     *
     * Time  : O(n)
     * Space : O(n)  -- at most (n + 1) / 2 numbers on the stack
     */
    public int evalRPN(String[] tokens) {
        Deque<Integer> stack = new ArrayDeque<>();
        for (String t : tokens) {
            if (isOperator(t)) {
                int b = stack.pop();                       // right operand (pushed last)
                int a = stack.pop();                       // left operand
                stack.push(apply(t, a, b));
            } else {
                stack.push(Integer.parseInt(t));
            }
        }
        return stack.pop();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — ARRAY-BACKED STACK (same algorithm, no boxing)
     * ------------------------------------------------------------------------
     * Idea:
     *   Identical logic, but an int[] with a top index replaces
     *   Deque<Integer>, avoiding an Integer object per push. The array never
     *   needs more than (n + 1) / 2 slots, because a valid RPN expression with
     *   k operators has k + 1 numbers and n = 2k + 1 tokens.
     *
     * Time  : O(n)
     * Space : O(n)
     */
    public int evalRPNArray(String[] tokens) {
        int[] stack = new int[(tokens.length + 1) / 2];
        int top = 0;                                       // number of values on the stack
        for (String t : tokens) {
            if (isOperator(t)) {
                int b = stack[--top];
                int a = stack[--top];
                stack[top++] = apply(t, a, b);
            } else {
                stack[top++] = Integer.parseInt(t);
            }
        }
        return stack[0];
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 4 — RECURSION FROM THE END (the stack is the call stack)
     * ------------------------------------------------------------------------
     * Idea:
     *   Read tokens from the LAST one backward. The last token is the root of
     *   the expression tree:
     *     - Number:   that's the value.
     *     - Operator: evaluate the RIGHT operand first (it's the next token
     *                 when reading backward), then the left operand, and
     *                 combine them.
     *   A shared index walks backward through the array.
     *
     * Time  : O(n)
     * Space : O(n) recursion depth in the worst case, e.g. "1 2 3 4 + + +".
     *         With n up to 10^4 that's about 5000 frames: usually fine in Java,
     *         but the explicit stack (Approach 2) can't overflow the call stack.
     */
    private int idx;

    public int evalRPNRecursive(String[] tokens) {
        idx = tokens.length - 1;
        return evalFromEnd(tokens);
    }

    private int evalFromEnd(String[] tokens) {
        String t = tokens[idx--];
        if (!isOperator(t)) return Integer.parseInt(t);
        int b = evalFromEnd(tokens);                       // right operand comes first backward
        int a = evalFromEnd(tokens);
        return apply(t, a, b);
    }

    // ------------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------------

    // Operators are exactly one character; "-11" (length 3) is a number.
    private static boolean isOperator(String t) {
        if (t.length() != 1) return false;
        char c = t.charAt(0);
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    // a is the LEFT operand, b the RIGHT. Java int division truncates toward zero.
    private static int apply(String op, int a, int b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            default:  return a / b;
        }
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why does RPN need no parentheses or precedence rules?"
     *      -> The order of the tokens already encodes the order of operations:
     *         an operator applies to the two most recent results. That's why
     *         a simple stack evaluates it in one pass.
     *
     *  F2. "Convert a normal (infix) expression like '3 + 4 * 2' to RPN?"
     *      -> Dijkstra's shunting-yard algorithm: output numbers directly, keep
     *         operators on a stack, and pop operators with higher or equal
     *         precedence before pushing a new one; '(' and ')' bracket the
     *         stack. Then evaluate the result with this solution.
     *
     *  F3. "How would you validate the input?"
     *      -> Before each operator, check there are at least 2 values on the
     *         stack; at the end, check exactly 1 value remains. Also guard
     *         division by zero and catch NumberFormatException on bad tokens.
     *
     *  F4. "What if division should FLOOR instead (like Python's //)?"
     *      -> Use Math.floorDiv(a, b): floorDiv(-7, 2) = -4, while -7 / 2 = -3.
     *
     *  F5. "What if results could overflow int?"
     *      -> Use a long stack, or Math.addExact / multiplyExact to throw on
     *         overflow instead of silently wrapping.
     *
     *  F6. "Support more operators, like '^' or unary negation?"
     *      -> Put the operators in a Map<String, IntBinaryOperator> so adding
     *         one is a single entry. Unary operators pop ONE value instead of
     *         two, so the map would also need to record each operator's arity.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all four approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String[] tokens, EvaluateReversePolishNotation sol) {
        System.out.println(label + " tokens=" + Arrays.toString(tokens)
                + " -> stack=" + sol.evalRPN(tokens)
                + ", array=" + sol.evalRPNArray(tokens)
                + ", recursive=" + sol.evalRPNRecursive(tokens)
                + ", brute=" + sol.evalRPNBruteForce(tokens));
    }

    public static void main(String[] args) {
        EvaluateReversePolishNotation sol = new EvaluateReversePolishNotation();

        // Examples
        report("Example 1 ->", new String[]{"2", "1", "+", "3", "*"}, sol);       // 9
        report("Example 2 ->", new String[]{"4", "13", "5", "/", "+"}, sol);      // 6
        report("Example 3 ->", new String[]{"10", "6", "9", "3", "+", "-11", "*",
                "/", "*", "17", "+", "5", "+"}, sol);                              // 22

        // Edge cases
        report("Single number     ->", new String[]{"42"}, sol);                  // 42
        report("Negative literal  ->", new String[]{"-11"}, sol);                 // -11
        report("Subtraction order ->", new String[]{"5", "3", "-"}, sol);         // 2 (not -2)
        report("Division order    ->", new String[]{"12", "4", "/"}, sol);        // 3 (not 0)
        report("Truncate negative ->", new String[]{"-7", "2", "/"}, sol);        // -3 (not -4)
        report("Divide by negative->", new String[]{"7", "-2", "/"}, sol);        // -3
        report("Two subexpressions->", new String[]{"1", "2", "+", "3", "4", "+", "*"}, sol); // 21
        report("Deep right nesting->", new String[]{"1", "2", "3", "4", "+", "+", "+"}, sol); // 10
    }
}
