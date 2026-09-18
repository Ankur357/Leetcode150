/*
 * ============================================================================
 * LeetCode 238. Product of Array Except Self                 [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an integer array nums, return an array answer such that answer[i] is
 * equal to the PRODUCT of all the elements of nums EXCEPT nums[i].
 *
 * The product of any prefix or suffix of nums is guaranteed to fit in a 32-bit
 * integer.
 *
 * You must write an algorithm that runs in O(n) time and WITHOUT using the
 * DIVISION operation.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   2 <= nums.length <= 10^5
 *   -30 <= nums[i] <= 30
 *   The product of any prefix or suffix of nums fits in a 32-bit integer.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  nums = [1,2,3,4]
 *   Output: [24,12,8,6]
 *   Explanation: answer[0]=2*3*4=24, answer[1]=1*3*4=12,
 *                answer[2]=1*2*4=8,  answer[3]=1*2*3=6.
 *
 * Example 2:
 *   Input:  nums = [-1,1,0,-3,3]
 *   Output: [0,0,9,0,0]
 *   Explanation: Only answer[2] is nonzero because every other position's
 *                product includes the single 0 at index 2. answer[2] excludes
 *                the 0, giving (-1)*1*(-3)*3 = 9.
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Division is explicitly BANNED, right?
 *      -> Yes. Even though total_product / nums[i] is tempting, it's disallowed
 *         (and breaks on zeros anyway). We use prefix/suffix products instead.
 *
 *  Q2. Can the array contain ZEROS? How many?
 *      -> Yes. Zero handling is the crux: one zero -> all answers 0 except the
 *         zero's own slot; two+ zeros -> every answer is 0. The prefix/suffix
 *         method handles this automatically with no special-casing.
 *
 *  Q3. Can values be NEGATIVE?
 *      -> Yes (-30..30). Sign just carries through multiplication normally.
 *
 *  Q4. Does the output array count against the O(1) extra-space follow-up?
 *      -> No. The returned answer array is not counted as extra space; the
 *         follow-up asks for O(1) space BEYOND the output.
 *
 *  Q5. Any overflow concerns?
 *      -> The prompt guarantees every prefix/suffix product fits in a 32-bit
 *         int, so plain int arithmetic is safe.
 *
 *  Q6. Minimum length?
 *      -> n >= 2, so "except self" always leaves at least one factor.
 *
 * ============================================================================
 */

import java.util.Arrays;

public class ProductOfArrayExceptSelf {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — BRUTE FORCE (nested loops, no division)
     * ------------------------------------------------------------------------
     * Idea:
     *   For each index i, multiply every OTHER element to form answer[i].
     *   Obeys the no-division rule but is quadratic.
     *
     * Time  : O(n^2)   -- for each i, scan all other n-1 elements
     * Space : O(1) extra (besides output)
     *
     * Correct but violates the required O(n); shown to anchor the definition.
     */
    public int[] productExceptSelfBruteForce(int[] nums) {
        int n = nums.length;
        int[] answer = new int[n];
        for (int i = 0; i < n; i++) {
            int prod = 1;
            for (int j = 0; j < n; j++) {
                if (j != i) prod *= nums[j];
            }
            answer[i] = prod;
        }
        return answer;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — DIVISION (the "cheating" baseline — NOT allowed)
     * ------------------------------------------------------------------------
     * Idea:
     *   Compute the total product, then answer[i] = total / nums[i]. This is
     *   O(n) but BANNED by the problem, AND it breaks on zeros (division by 0,
     *   and can't recover the single-zero case cleanly).
     *
     * We include it only to explain WHY the constraint pushes us to prefix/
     * suffix products. Handles zeros only with awkward special counting:
     *   - 0 zeros: answer[i] = total / nums[i]
     *   - 1 zero:  answer[zeroIdx] = product of the rest; all others 0
     *   - 2+ zeros: all answers 0
     *
     * Time  : O(n)
     * Space : O(1) extra
     */
    public int[] productExceptSelfWithDivision(int[] nums) {
        int n = nums.length;
        int[] answer = new int[n];
        int zeros = 0, prodNonZero = 1, zeroIdx = -1;
        for (int i = 0; i < n; i++) {
            if (nums[i] == 0) { zeros++; zeroIdx = i; }
            else prodNonZero *= nums[i];
        }
        if (zeros >= 2) return answer;             // all zeros
        if (zeros == 1) { answer[zeroIdx] = prodNonZero; return answer; }
        for (int i = 0; i < n; i++) answer[i] = prodNonZero / nums[i];
        return answer;
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 3 — OPTIMAL (prefix * suffix products, O(1) extra space)
     * ------------------------------------------------------------------------
     * Idea:
     *   answer[i] = (product of everything LEFT of i) * (product of everything
     *   RIGHT of i). Neither factor includes nums[i], so no division needed and
     *   zeros are handled automatically (a zero simply makes the relevant
     *   prefix/suffix zero).
     *
     *   Two sweeps, reusing the output array to stay O(1) extra:
     *     Pass 1 (left -> right): answer[i] = product of nums[0..i-1] (prefix).
     *     Pass 2 (right -> left): multiply answer[i] by a running SUFFIX product
     *                             of nums[i+1..n-1].
     *
     *   Walkthrough on [1,2,3,4]:
     *     after prefix pass:  answer = [1, 1, 2, 6]   (empty product = 1 at i=0)
     *     suffix sweep:
     *       i=3: ans=6*1=6,  suffix=4
     *       i=2: ans=2*4=8,  suffix=12
     *       i=1: ans=1*12=12,suffix=24
     *       i=0: ans=1*24=24
     *     result -> [24,12,8,6]  ✓
     *
     * Time  : O(n)   -- two linear passes
     * Space : O(1) extra   -- only a running suffix scalar (output not counted)
     */
    public int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] answer = new int[n];

        // Pass 1: answer[i] = product of all elements to the LEFT of i.
        answer[0] = 1;                             // nothing to the left of index 0
        for (int i = 1; i < n; i++) {
            answer[i] = answer[i - 1] * nums[i - 1];
        }

        // Pass 2: fold in the product of all elements to the RIGHT of i.
        int suffix = 1;                            // running product of the right side
        for (int i = n - 1; i >= 0; i--) {
            answer[i] *= suffix;                   // left * right = except-self
            suffix *= nums[i];                     // extend suffix to include i
        }
        return answer;
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS  (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     *  F1. "Why not just divide by nums[i]?"
     *      -> Division is banned AND undefined when nums[i] == 0. The prefix/
     *         suffix approach sidesteps both issues with pure multiplication.
     *
     *  F2. "How does prefix/suffix handle zeros without special-casing?"
     *      -> A single zero at index z makes every prefix/suffix that spans z
     *         become 0, so all answers except answer[z] are 0; answer[z] gets
     *         prefix(z)*suffix(z), which excludes the zero -> correct nonzero.
     *         Two zeros make every answer's product include a zero -> all 0.
     *
     *  F3. "Prove the O(1) extra space claim."
     *      -> We write prefix products directly into the OUTPUT array (which
     *         doesn't count), then fold suffixes in-place using a single scalar
     *         `suffix`. No auxiliary array of size n.
     *
     *  F4. "What if overflow WEREN'T guaranteed away?"
     *      -> Use long accumulators, or detect overflow. If exact big products
     *         are needed, BigInteger (O(n * digits)). Zeros still handled the
     *         same way.
     *
     *  F5. "Now division IS allowed — simplest solution?"
     *      -> Total product / nums[i], but you must special-case zeros by
     *         counting them (see productExceptSelfWithDivision). Cleaner code
     *         only when there are no zeros.
     *
     *  F6. "Generalize: answer[i] = f over all elements except i, for an
     *       invertible/associative op."
     *      -> Same prefix/suffix trick works for any associative op (sum, xor,
     *         gcd, min via monotonic structures). Prefix-op * suffix-op per i.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against all approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, int[] nums, ProductOfArrayExceptSelf sol) {
        System.out.println(label + " " + Arrays.toString(nums)
                + " -> optimal=" + Arrays.toString(sol.productExceptSelf(nums))
                + ", brute=" + Arrays.toString(sol.productExceptSelfBruteForce(nums))
                + ", division=" + Arrays.toString(sol.productExceptSelfWithDivision(nums)));
    }

    public static void main(String[] args) {
        ProductOfArrayExceptSelf sol = new ProductOfArrayExceptSelf();

        // Examples
        report("Example 1 ->", new int[]{1, 2, 3, 4}, sol);         // [24,12,8,6]
        report("Example 2 ->", new int[]{-1, 1, 0, -3, 3}, sol);    // [0,0,9,0,0]

        // Edge cases
        report("Two elements     ->", new int[]{2, 3}, sol);           // [3,2]
        report("Single zero      ->", new int[]{4, 0, 2}, sol);        // [0,8,0]
        report("Two zeros        ->", new int[]{0, 0, 5}, sol);        // [0,0,0]
        report("Negatives        ->", new int[]{-2, -3, 4}, sol);      // [-12,-8,6]
        report("Contains ones    ->", new int[]{1, 1, 1}, sol);       // [1,1,1]
    }
}
