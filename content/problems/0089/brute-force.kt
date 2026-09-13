/**
 * Project Euler 089 — Roman Numerals（暴力解）
 *
 * 暴力思路：不用「贪心取 13 个规范记号」的推理，而是按罗马数字的位值文法穷举全部合法写法：
 * 千位取 ""…"MMMM"，百位、十位、个位各取 10 种规范组合，拼出约 5000 个候选串，
 * 逐个解码并对每个数值记录最短串长。答案 = Σ(原串长 − 最短串长)。
 *
 * 教训：若把「最短」写成无约束的加法背包 dp[v] = dp[v − val(sym)] + len(sym)，会凑出
 * XIVIV(=18) 这类非法串——它比合法的 XVIII 更短——结果就会比真正的最短写法偏小，
 * 多省出两百多个字符；必须带位值文法约束。
 *
 * 复杂度：O(5000 × 串长)，实测 &lt; 1 ms。
 * 需从题目目录运行（读取同目录 roman.txt）；
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 */

import java.io.File

private val b089Thousands = listOf("", "M", "MM", "MMM", "MMMM")
private val b089Hundreds = listOf("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM")
private val b089Tens = listOf("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC")
private val b089Units = listOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")

private val b089Value = mapOf(
    'I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000,
)

private fun b089Decode(r: String): Int {
    var total = 0
    for (i in r.indices) {
        val v = b089Value.getValue(r[i])
        total += if (i + 1 < r.length && v < b089Value.getValue(r[i + 1])) -v else v
    }
    return total
}

fun solveBruteForce(path: String = "roman.txt"): Long {
    val best = HashMap<Int, Int>()
    for (a in b089Thousands) for (b in b089Hundreds) for (c in b089Tens) for (d in b089Units) {
        val s = a + b + c + d
        if (s.isEmpty()) continue
        val v = b089Decode(s)
        val prev = best[v]
        if (prev == null || s.length < prev) best[v] = s.length
    }
    var saved = 0L
    for (line in File(path).readLines()) {
        val r = line.trim()
        if (r.isEmpty()) continue
        saved += r.length - best.getValue(b089Decode(r))
    }
    return saved
}

fun main() {
    println(solveBruteForce())
}
