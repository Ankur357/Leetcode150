package Leetcode150.Hashmap;
/*
 * ============================================================================
 * LeetCode 49. Group Anagrams                                 [Difficulty: Medium]
 * ============================================================================
 *
 * ----------------------------------------------------------------------------
 * PROBLEM STATEMENT
 * ----------------------------------------------------------------------------
 * Given an array of strings strs, GROUP THE ANAGRAMS together. You can return
 * the answer in ANY ORDER.
 *
 * An anagram is a word or phrase formed by rearranging the letters of a
 * different word, using all the original letters exactly once.
 *
 * ----------------------------------------------------------------------------
 * CONSTRAINTS
 * ----------------------------------------------------------------------------
 *   1 <= strs.length <= 10^4
 *   0 <= strs[i].length <= 100
 *   strs[i] consists of LOWERCASE English letters.
 *
 * ----------------------------------------------------------------------------
 * EXAMPLES
 * ----------------------------------------------------------------------------
 * Example 1:
 *   Input:  strs = ["eat","tea","tan","ate","nat","bat"]
 *   Output: [["bat"],["nat","tan"],["ate","eat","tea"]]
 *   (Any grouping/order is accepted.)
 *
 * Example 2:
 *   Input:  strs = [""]
 *   Output: [[""]]
 *
 * Example 3:
 *   Input:  strs = ["a"]
 *   Output: [["a"]]
 *
 * ----------------------------------------------------------------------------
 * CLARIFYING QUESTIONS  (what a strong candidate asks BEFORE coding)
 * ----------------------------------------------------------------------------
 *  Q1. Two strings belong together iff they're anagrams (same letter multiset)?
 *      -> Yes. I need a canonical SIGNATURE that is identical for anagrams and
 *         distinct otherwise, to use as a hash-map key.
 *
 *  Q2. Does output order (of groups or within a group) matter?
 *      -> No — any order is accepted. So I won't sort the result.
 *
 *  Q3. Character set — lowercase English only?
 *      -> Yes, so a 26-length count signature works and beats sorting each word.
 *
 *  Q4. Can strings be empty?
 *      -> Yes ("" is valid, Example 2). The empty string forms its own anagram
 *         group; handled naturally (its signature is the all-zero count / "").
 *
 *  Q5. Can there be duplicate strings?
 *      -> Possibly; duplicates are anagrams of each other and land in the same
 *         group (kept, not deduped).
 *
 *  Q6. Return type?
 *      -> List<List<String>> — one inner list per anagram group.
 *
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupAnagrams {

    /*
     * ------------------------------------------------------------------------
     * APPROACH 1 — SORTED-STRING SIGNATURE [clean baseline]
     * ------------------------------------------------------------------------
     * Idea:
     * Anagrams share the same sorted character sequence. Use the sorted string
     * as the map key; append each original word to that key's bucket.
     *
     * Let n = number of strings, k = max string length.
     * Time : O(n * k log k) -- sorting each string
     * Space : O(n * k) -- the map and result
     *
     * Simple and readable; the count-key version avoids the per-string sort.
     */
    public List<List<String>> groupAnagramsSort(String[] strs) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String s : strs) {
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(groups.values());
    }

    /*
     * ------------------------------------------------------------------------
     * APPROACH 2 — OPTIMAL: 26-COUNT SIGNATURE key <-- the answer
     * ------------------------------------------------------------------------
     * Idea:
     * Two words are anagrams iff their letter-count vectors (length 26) are
     * identical. Build that count per word and turn it into a canonical string
     * key (counts separated by a delimiter so "1,2" != "12"). Bucket by key.
     *
     * Building the key is O(k) instead of O(k log k), so this beats sorting.
     *
     * Time : O(n * k)
     * Space : O(n * k)
     */
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String s : strs) {
            groups.computeIfAbsent(countKey(s), k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(groups.values());
    }

    // Canonical key from a 26-length letter count, e.g. "#1#0#0...#2".
    // The '#' delimiter prevents ambiguity between multi-digit counts.
    private String countKey(String s) {
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++)
            count[s.charAt(i) - 'a']++;
        StringBuilder sb = new StringBuilder();
        for (int c : count)
            sb.append('#').append(c);
        return sb.toString();
    }

    /*
     * ------------------------------------------------------------------------
     * FOLLOW-UP QUESTIONS (what the interviewer probes AFTER the optimal answer)
     * ------------------------------------------------------------------------
     * F1. "Sorted-key vs. count-key — when does count win?"
     * -> Count key is O(k) per word vs O(k log k) for sorting; it wins when
     * strings are long. Sorted key is simpler and charset-agnostic. For
     * short words the difference is negligible.
     *
     * F2. "Why the '#' delimiter in the count key?"
     * -> Without a separator, counts like [1,12] and [11,2] both stringify
     * to "112" and collide. A delimiter makes the encoding unambiguous.
     *
     * F3. "Unicode instead of lowercase a-z?"
     * -> The fixed int[26] breaks. Either sort the string (Approach 1,
     * charset-agnostic) or build the count key from a HashMap over code
     * points serialized deterministically.
     *
     * F4. "Could you use the int[26] array itself as the key?"
     * -> Not directly — arrays use identity hashCode/equals, so equal
     * contents wouldn't match. Wrap it (Arrays.toString, a String key,
     * or a record/List<Integer>) to get value-based equality.
     *
     * F5. "Memory for very large inputs?"
     * -> Keys duplicate letter info; you can intern keys or hash them to a
     * 64-bit rolling/polynomial hash (with collision handling) to shrink
     * key size, trading a tiny collision risk for space.
     *
     * F6. "Relationship to Valid Anagram (LC 242)?"
     * -> Same signature idea: LC 242 compares two signatures for equality;
     * here we bucket many strings by that signature. The count vector is
     * the shared primitive.
     */

    // ------------------------------------------------------------------------
    // Driver: runs the examples against both approaches.
    // ------------------------------------------------------------------------
    private static void report(String label, String[] strs, GroupAnagrams sol) {
        System.out.println(label + " strs=" + Arrays.toString(strs));
        System.out.println("   countKey -> " + sol.groupAnagrams(strs));
        System.out.println("   sortKey  -> " + sol.groupAnagramsSort(strs));
    }

    public static void main(String[] args) {
        GroupAnagrams sol = new GroupAnagrams();

        // Examples
        report("Example 1 ->", new String[] { "eat", "tea", "tan", "ate", "nat", "bat" }, sol);
        // Groups: [bat], [nat,tan], [ate,eat,tea] (any order)

        report("Example 2 ->", new String[] { "" }, sol); // [[""]]
        report("Example 3 ->", new String[] { "a" }, sol); // [["a"]]

        // Edge cases
        report("All anagrams   ->", new String[] { "abc", "bca", "cab", "bac" }, sol); // one group of 4
        report("No anagrams    ->", new String[] { "abc", "def", "ghi" }, sol); // three singletons
        report("Duplicates     ->", new String[] { "ab", "ab", "ba" }, sol); // one group of 3
        report("Multi-digit cnt->", new String[] { "aaaaaaaaaab", "baaaaaaaaaa" }, sol); // one group (delimiter
                                                                                         // matters)
        report("Empty strings  ->", new String[] { "", "", "a" }, sol); // [["",""],["a"]]
    }
}
