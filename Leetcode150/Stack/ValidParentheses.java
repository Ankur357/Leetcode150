package Leetcode150.Stack;
/*
 * ============================================================================
 * LeetCode 20. Valid Parentheses                               [Difficulty: Easy]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given a string s containing just the characters '(', ')', '{', '}', '[' and
 * ']', determine if the input string is VALID.
 *
 * An input string is valid if:
 *   1. Open brackets must be closed by the SAME TYPE of brackets.
 *   2. Open brackets must be closed in the CORRECT ORDER.
 *   3. Every close bracket has a corresponding open bracket of the same type.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= s.length <= 10^4
 *   s consists of parentheses only '()[]{}'.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  s = "()"
 *   Output: true
 *
 * Example 2:
 *   Input:  s = "()[]{}"
 *   Output: true
 *
 * Example 3:
 *   Input:  s = "(]"
 *   Output: false
 *   Explanation: '(' is closed by the wrong type.
 *
 * Example 4:
 *   Input:  s = "([])"
 *   Output: true
 *   Explanation: Nested correctly: '[' closes before '('.
 *
 * Example 5:
 *   Input:  s = "([)]"
 *   Output: false
 *   Explanation: Counts match, but ')' arrives while '[' is still open, so the
 *                ORDER is wrong.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Only the six bracket characters, or can other characters appear?
 *      -> Only '()[]{}'. (If letters were allowed, I'd just skip them — F4.)
 *
 *  Q2. Is matching COUNTS enough, or does nesting order matter?
 *      -> Order matters: "([)]" has balanced counts but is invalid. So a simple
 *         counter per type is NOT enough; I need a stack (last opened, first
 *         closed).
 *
 *  Q3. Is the empty string valid?
 *      -> Constraints say length >= 1, but an empty string would be valid
 *         (nothing unclosed). My code returns true for it naturally.
 *
 *  Q4. What about a string that starts with a closer, like ")("?
 *      -> Invalid: a closer with nothing open must fail immediately, so I must
 *         check the stack is non-empty before popping.
 *
 *  Q5. Quick reject?
 *      -> An ODD length can never be valid, since every bracket needs a
 *         partner. Cheap early exit.
 *
 *  Q6. Return type?
 *      -> boolean.
 *
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class ValidParentheses {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (repeatedly delete adjacent pairs)
     * ------------------------------------------------------------------------
     * Idea:
     *   In any valid string, at least one matching pair "()", "[]" or "{}"
     *   appears side by side (the innermost pair). Remove all such adjacent
     *   pairs and repeat until nothing changes. The string is valid iff it ends
     *   up empty.
     *
     * Time  : O(n^2)  -- up to n/2 rounds, each rebuilding an O(n) string
     * Space : O(n)    -- the intermediate strings
     *
     * Easy to explain, but quadratic. The stack does the same "cancel the
     * innermost pair" work in a single pass.
     */
    public boolean isValidBruteForce(String s) {
        String prev;
        do {
            prev = s;
            s = s.replace("()", "").replace("[]", "").replace("{}", "");
        } while (!s.equals(prev));
        return s.isEmpty();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: STACK of open brackets + closer->opener map  <-- answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Scan left to right:
     *     - Opening bracket: push it.
     *     - Closing bracket: the stack top must be its matching opener. If the
     *       stack is empty or the top is a different opener, return false.
     *       Otherwise pop.
     *   At the end the stack must be EMPTY; leftover openers were never closed.
     *
     *   The stack captures "most recently opened must be closed first", which
     *   is exactly the ordering rule that counters can't express.
     *
     * Time  : O(n)
     * Space : O(n)  -- worst case all openers, e.g. "(((((("
     */
    private static final Map<Character, Character> OPENER_FOR =
            Map.of(')', '(', ']', '[', '}', '{');

    public boolean isValid(String s) {
        if (s.length() % 2 != 0) return false;           // odd length can't pair up

        Deque<Character> stack = new ArrayDeque<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (OPENER_FOR.containsKey(c)) {             // closing bracket
                if (stack.isEmpty()) return false;       // nothing open to close
                char top = stack.pop();                  // unbox so != compares chars, not references
                if (top != OPENER_FOR.get(c)) return false;  // wrong type
            } else {                                     // opening bracket
                stack.push(c);
            }
        }
        return stack.isEmpty();                          // anything left was never closed
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — STACK of EXPECTED closers, backed by a char array
     * ------------------------------------------------------------------------
     * Idea:
     *   Same algorithm, two small tweaks:
     *     - On an opener, push the CLOSER we now expect ('(' pushes ')'). Then a
     *       closer only has to equal the top, so no map lookup is needed.
     *     - Use a plain char[] with a top index instead of Deque<Character>,
     *       which avoids boxing every char. That's the usual "make it fast"
     *       version.
     *
     * Time  : O(n)
     * Space : O(n)
     */
    public boolean isValidExpectedCloser(String s) {
        if (s.length() % 2 != 0) return false;

        char[] stack = new char[s.length()];
        int top = 0;                                     // number of items on the stack
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '(' -> stack[top++] = ')';
                case '[' -> stack[top++] = ']';
                case '{' -> stack[top++] = '}';
                default -> {                             // a closer
                    if (top == 0 || stack[--top] != c) return false;
                }
            }
        }
        return top == 0;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why not just count each bracket type?"
     *      -> Counts check balance but not ORDER. "([)]" has one of each opener
     *         and closer yet is invalid. The stack enforces last-opened,
     *         first-closed.
     *
     *  F2. "What if there's only ONE bracket type, like just '(' and ')'?"
     *      -> Then a single integer counter is enough: +1 on '(', -1 on ')',
     *         fail if it ever goes negative, and require 0 at the end. O(1)
     *         space, because with one type there's no wrong type to close with.
     *
     *  F3. "What are the two ways this fails, and where does the code catch them?"
     *      -> (a) A closer with no matching opener on top: caught inside the loop
     *         (empty stack or wrong type). (b) Openers never closed: caught by
     *         the final stack.isEmpty() check. Forgetting (b) makes "((" pass.
     *
     *  F4. "Strings with other characters, like code or math expressions?"
     *      -> Ignore anything that isn't a bracket and run the same logic. In
     *         real code you'd also skip brackets inside string literals or
     *         comments.
     *
     *  F5. "Return the length of the LONGEST valid substring (LeetCode 32)?"
     *      -> Harder. Push INDICES instead of chars, seeded with -1 as a base;
     *         on a match, length = i - stack.peek(). O(n). There's also a
     *         two-pass counter version with O(1) space.
     *
     *  F6. "Fewest insertions/removals to make it valid (LeetCode 921 / 1249)?"
     *      -> Same scan: count closers that found no opener (insert an opener
     *         for each), plus openers left over at the end (insert a closer for
     *         each). For 1249, remember the indices to remove instead.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String s, ValidParentheses sol) {
        System.out.println(label + " s=\"" + s + "\""
                + " -> stack=" + sol.isValid(s)
                + ", expectedCloser=" + sol.isValidExpectedCloser(s)
                + ", brute=" + sol.isValidBruteForce(s));
    }

    public static void main(String[] args) {
        ValidParentheses sol = new ValidParentheses();

        // Examples
        report("Example 1 ->", "()", sol);        // true
        report("Example 2 ->", "()[]{}", sol);    // true
        report("Example 3 ->", "(]", sol);        // false
        report("Example 4 ->", "([])", sol);      // true
        report("Example 5 ->", "([)]", sol);      // false

        // Edge cases
        report("Starts with closer ->", ")(", sol);          // false (empty-stack pop)
        report("Only openers       ->", "((", sol);          // false (leftover on stack)
        report("Only closers       ->", "))", sol);          // false
        report("Odd length         ->", "(()", sol);         // false (early exit)
        report("Deep nesting       ->", "{[()()]}", sol);    // true
        report("Wrong type deep    ->", "{[(])}", sol);      // false
        report("Long sequential    ->", "()()()[]{}{}", sol); // true
    }
}
