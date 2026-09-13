/**
 * Project Euler 079 — 暴力解（教学对比用）
 *
 * 思路：口令必须包含 keylog 中出现过的每个数字，故最短长度就是不同数字的个数（8 个）。
 * 于是穷举这 8 个数字的全部 8! = 40320 种排列（按字典序），逐一检查每个登录尝试是否为
 * 该候选口令的子序列，第一个通过全部 50 条约束的就是答案。不做任何偏序剪枝。
 * 复杂度：O(d! × m × 3)，d = 8，m = 50。
 *
 * 需从题目目录运行（读取同目录下的 keylog.txt）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.io.File

fun solveBruteForce(path: String = "keylog.txt"): Long {
    val attempts = File(path).readLines().map { it.trim() }.filter { it.isNotEmpty() }
    val digits = attempts.joinToString("").toSortedSet().map { it - '0' }.toIntArray()

    val candidate = digits.copyOf()
    while (true) {
        if (isSubsequenceOfAll(candidate, attempts)) return candidate.joinToString("").toLong()
        if (!nextPermutation(candidate)) return -1L
    }
}

private fun isSubsequenceOfAll(candidate: IntArray, attempts: List<String>): Boolean {
    for (attempt in attempts) {
        var idx = 0
        for (ch in attempt) {
            val digit = ch - '0'
            while (idx < candidate.size && candidate[idx] != digit) idx++
            if (idx == candidate.size) return false
            idx++
        }
    }
    return true
}

private fun nextPermutation(a: IntArray): Boolean {
    var i = a.size - 2
    while (i >= 0 && a[i] >= a[i + 1]) i--
    if (i < 0) return false
    var j = a.size - 1
    while (a[j] <= a[i]) j--
    val tmp = a[i]; a[i] = a[j]; a[j] = tmp
    var lo = i + 1
    var hi = a.size - 1
    while (lo < hi) {
        val t = a[lo]; a[lo] = a[hi]; a[hi] = t
        lo++; hi--
    }
    return true
}

fun main() {
    println(solveBruteForce())
}
