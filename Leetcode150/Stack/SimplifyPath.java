package Leetcode150.Stack;
/*
 * ============================================================================
 * LeetCode 71. Simplify Path                                  [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * You are given an ABSOLUTE path for a Unix-style file system, which always
 * begins with a slash '/'. Transform it into its SIMPLIFIED CANONICAL path.
 *
 * Rules of a Unix-style file system:
 *   - A single period '.'  means the CURRENT directory.
 *   - A double period '..' means the PARENT directory (go up one level).
 *   - Multiple consecutive slashes like '//' or '///' are treated as ONE '/'.
 *   - Any other sequence of periods, such as '...' or '....', is a valid
 *     directory or file NAME.
 *
 * The simplified canonical path must:
 *   - Start with a single slash '/'.
 *   - Separate directories with exactly one slash '/'.
 *   - NOT end with a slash '/', unless it is the root directory.
 *   - NOT contain any '.' or '..' used as current/parent directory.
 *
 * Return the simplified canonical path.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= path.length <= 3000
 *   path consists of English letters, digits, period '.', slash '/' or '_'.
 *   path is a valid absolute Unix path.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  path = "/home/"
 *   Output: "/home"
 *   Explanation: The trailing slash is removed.
 *
 * Example 2:
 *   Input:  path = "/home//foo/"
 *   Output: "/home/foo"
 *   Explanation: Multiple slashes collapse into one.
 *
 * Example 3:
 *   Input:  path = "/home/user/Documents/../Pictures"
 *   Output: "/home/user/Pictures"
 *   Explanation: '..' goes up from Documents back to user.
 *
 * Example 4:
 *   Input:  path = "/../"
 *   Output: "/"
 *   Explanation: Going up from the root is not possible; you stay at root.
 *
 * Example 5:
 *   Input:  path = "/.../a/../b/c/../d/./"
 *   Output: "/.../b/d"
 *   Explanation: '...' is a normal directory name, not a special token.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. What does '..' at the ROOT do?
 *      -> Nothing: you stay at '/'. So popping an empty stack is a no-op, not
 *         an error.
 *
 *  Q2. Are names like '...' or '..hidden' special?
 *      -> No. Only a segment that is EXACTLY '.' or EXACTLY '..' is special.
 *         Everything else, including '...', is a normal name. This is why
 *         regex/string-replace tricks on '..' are a trap.
 *
 *  Q3. Is the input always absolute (starts with '/')?
 *      -> Yes. (A relative path would need different handling of leading
 *         '..' — see F3.)
 *
 *  Q4. What about empty segments from '//' or a trailing '/'?
 *      -> Splitting on '/' produces empty strings for those; just skip them.
 *
 *  Q5. Should I resolve symlinks or check the directories exist?
 *      -> No, it's purely textual. (Real filesystems differ when '..' follows
 *         a symlink — F5.)
 *
 *  Q6. Output for a path that reduces to nothing?
 *      -> "/" (the root), never an empty string.
 *
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SimplifyPath {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (repeatedly cancel the first '..')
     * ------------------------------------------------------------------------
     * Idea:
     *   Split on '/' and drop empty segments and '.'. Now only real names and
     *   '..' remain. Find the FIRST '..': everything before it is a real name,
     *   so remove it together with the name just before it (if any; at the
     *   root there's nothing to remove). Repeat until no '..' is left, then
     *   join with '/'.
     *
     * Time  : O(k^2)  -- k segments; each removal scans and shifts the list
     * Space : O(n)
     *
     * Correct, but each '..' triggers a fresh scan. A stack cancels each '..'
     * the moment we read it.
     */
    public String simplifyPathBruteForce(String path) {
        List<String> tokens = new ArrayList<>();
        for (String part : path.split("/")) {
            if (!part.isEmpty() && !part.equals(".")) tokens.add(part);
        }

        while (true) {
            int idx = tokens.indexOf("..");
            if (idx == -1) break;
            tokens.remove(idx);                  // remove the '..' itself
            if (idx > 0) tokens.remove(idx - 1); // and the directory it climbs out of
        }
        return "/" + String.join("/", tokens);   // empty list -> "/"
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: SPLIT + STACK of directory names  <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     *   Split on '/' and process each segment left to right:
     *     - ""  or "."  -> skip (extra slash, or "stay here").
     *     - ".."        -> pop the last directory, if there is one.
     *     - anything else -> push it as a directory name.
     *   The stack now holds the canonical path from root to leaf. Join it with
     *   '/' and prefix '/' (or return "/" if the stack is empty).
     *
     *   A Deque used at its tail works as a stack, and iterating it from the
     *   head gives root-to-leaf order for free.
     *
     * Time  : O(n)
     * Space : O(n)
     */
    public String simplifyPath(String path) {
        Deque<String> stack = new ArrayDeque<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) {
                continue;                          // '//' or current directory
            }
            if (part.equals("..")) {
                stack.pollLast();                  // go up; no-op at root (returns null)
            } else {
                stack.offerLast(part);             // a real directory name, incl. '...'
            }
        }

        if (stack.isEmpty()) return "/";
        StringBuilder sb = new StringBuilder();
        for (String dir : stack) {                 // head -> tail = root -> leaf
            sb.append('/').append(dir);
        }
        return sb.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — SINGLE PASS, no split (build the answer in place)
     * ------------------------------------------------------------------------
     * Idea:
     *   Scan characters manually, skipping slashes and reading one segment at a
     *   time. Append each real name to a StringBuilder as "/name", and push the
     *   builder's length from BEFORE that append onto a stack of ints. On '..',
     *   pop that length and truncate the builder back to it with setLength,
     *   undoing the last directory in O(1).
     *
     *   This avoids the String[] from split and the per-segment Strings for
     *   names that get popped anyway; useful if paths are huge.
     *
     * Time  : O(n)
     * Space : O(n)  -- the builder plus one int per open directory
     */
    public String simplifyPathSinglePass(String path) {
        StringBuilder sb = new StringBuilder();
        Deque<Integer> marks = new ArrayDeque<>();   // sb length before each directory
        int n = path.length();
        int i = 0;

        while (i < n) {
            // Skip any run of slashes.
            while (i < n && path.charAt(i) == '/') i++;
            if (i == n) break;

            // Read one segment [start, i).
            int start = i;
            while (i < n && path.charAt(i) != '/') i++;
            int len = i - start;

            boolean isDot = len == 1 && path.charAt(start) == '.';
            boolean isDotDot = len == 2 && path.charAt(start) == '.' && path.charAt(start + 1) == '.';

            if (isDot) {
                continue;
            } else if (isDotDot) {
                if (!marks.isEmpty()) sb.setLength(marks.pop());   // undo last directory
            } else {
                marks.push(sb.length());
                sb.append('/').append(path, start, i);
            }
        }
        return sb.length() == 0 ? "/" : sb.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why is a stack the right structure?"
     *      -> '..' always undoes the MOST RECENT directory that's still in the
     *         path. Last in, first out is exactly a stack.
     *
     *  F2. "Why not just use string replace or regex on '/..'?"
     *      -> It's easy to get wrong: '/...' and '/..hidden' contain '..' but are
     *         normal names; '..' at the root must be ignored; replacements can
     *         create new patterns that need another pass. Tokenizing by '/' and
     *         comparing whole segments avoids all of that.
     *
     *  F3. "What changes for RELATIVE paths, like 'a/../../b'?"
     *      -> A '..' with nothing left to pop can't be dropped any more; it has
     *         to stay in the output ('../b'). Push '..' when the stack is empty
     *         or its top is already '..', and don't add the leading '/'.
     *
     *  F4. "Implement 'cd': resolve a path against a current directory?"
     *      -> If the target starts with '/', simplify it alone. Otherwise
     *         simplify cwd + "/" + target. Same stack logic.
     *
     *  F5. "Does this match what a real filesystem does?"
     *      -> Not with symbolic links: if 'a' is a link to '/x/y', then 'a/..'
     *         really means '/x', not the current directory. Textual
     *         simplification (like Java's Path.normalize()) ignores that; to get
     *         the real answer you have to resolve links (Path.toRealPath()).
     *
     *  F6. "Approach 2 vs 3 — which would you ship?"
     *      -> Approach 2: clearer and fast enough for 3000 chars. Approach 3
     *         only matters if you're processing very long paths in bulk and the
     *         extra allocations show up in a profile.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all three approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String path, SimplifyPath sol) {
        System.out.println(label + " path=\"" + path + "\""
                + " -> stack=\"" + sol.simplifyPath(path) + "\""
                + ", singlePass=\"" + sol.simplifyPathSinglePass(path) + "\""
                + ", brute=\"" + sol.simplifyPathBruteForce(path) + "\"");
    }

    public static void main(String[] args) {
        SimplifyPath sol = new SimplifyPath();

        // Examples
        report("Example 1 ->", "/home/", sol);                              // "/home"
        report("Example 2 ->", "/home//foo/", sol);                         // "/home/foo"
        report("Example 3 ->", "/home/user/Documents/../Pictures", sol);    // "/home/user/Pictures"
        report("Example 4 ->", "/../", sol);                                // "/"
        report("Example 5 ->", "/.../a/../b/c/../d/./", sol);               // "/.../b/d"

        // Edge cases
        report("Root only          ->", "/", sol);                          // "/"
        report("Climb past root    ->", "/home/../../..", sol);             // "/"
        report("Dots in a name     ->", "/..hidden/x.", sol);               // "/..hidden/x."
        report("Mixed noise        ->", "/a//b////c/d//././/..", sol);      // "/a/b/c"
        report("Up then down       ->", "/a/./b/../../c/", sol);            // "/c"
        report("Four dots is a name->", "/..../a", sol);                    // "/..../a"
    }
}
