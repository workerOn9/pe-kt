/**
 * Project Euler 076 — 暴力解（教学对比用）
 *
 * 思路：按定义直接递归枚举 100 的每一种分拆（每部分不超过剩余值），数到多少种就是多少种。
 * 递归时用 maxPart 保证生成的各部分单调不增，从而天然去重。
 * 要求「至少两部分」，故从 maxPart = 99 起算，等价于把所有分拆中的 {100} 排除。
 * 复杂度：O(p(n)) 量级（实际访问的递归节点数略多于分拆数），p(100) ≈ 1.9×10^8。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    var count = 0L

    fun rec(remaining: Int, maxPart: Int) {
        if (remaining == 0) {
            count++
            return
        }
        var part = minOf(remaining, maxPart)
        while (part >= 1) {
            rec(remaining - part, part)
            part--
        }
    }

    rec(100, 99)
    return count
}

fun main() {
    println(solveBruteForce())
}
