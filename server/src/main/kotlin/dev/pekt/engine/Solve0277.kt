package dev.pekt.engine

/**
 * PE 277 — A Modified Collatz Sequence（修改版 Collatz 序列）：步规则 D/U/d 由当前项 mod 3 决定，
 * 求步串以 "UDDDUdddDDUDDddDdDddDDUDDdUUDd" 开头的最小 a_1 > 10^15。
 *
 * 推导（详见 content/problems/0277/solution.kt 头部与 0277/analysis.md）：
 *   a_n = (A_n·a_1 + B_n)/3^{n−1}，A_{n+1} = c_n A_n，B_{n+1} = c_n B_n + 3^{n−1} d_n
 *   （第 n 步为 D/U/d 时 (c,d) = (1,0)/(4,2)/(2,−1)）。于是「第 n 步是 s_n」等价于
 *   A_n a_1 + B_n ≡ 3^{n−1}·σ(s_n) (mod 3^n)，而满足前 n 个字符的 a_1 恰是一个模 3^n 的剩余类：
 *   三个提升 r, r+3^n, r+2·3^n 给出的 a_{n+1} mod 3 互不相同（每提升一格，a_{n+1} 增加
 *   A_{n+1} ≡ ±1 (mod 3)），恰有一个命中目标字符。
 *   答案 = 该剩余类（模 3^{30} = 205891132094649）中严格大于 10^15 的最小元素。
 *
 * 实现要点：逐字符提升时用**同类代表元**判定（a_1 = 1 会立刻终止序列、不产生字符，规范代表元
 * 可能退化）；最终再在类内逐项模拟，取第一个真正产生目标串的元素。
 * 复杂度 O(k³) 最坏（k = 30），常数极小，实测 < 1 ms。
 * 校验：题面锚点 231 → "DdDddUUdDD"、1004064 = "DdDddUUdDDDdUDUUUdDdUUDDDUdDD"（且是 >10^6
 * 最小解）；求解的剩余类与 BigInteger 仿射同余法（solution.kt 方法 B）在全部 30 个前缀上一致；
 * brute-force.kt 在 [1,10^7] 上穷举验证「前缀匹配 ⟺ 剩余类」。逻辑与 solution.kt 主路径一致。
 */
internal fun solve0277Impl(): Long {
    val target = "UDDDUdddDDUDDddDdDddDDUDDdUUDd"
    val lowerBound = 1_000_000_000_000_000L

    fun beginsWith(a: Long, s: String): Boolean {
        var x = a
        for (i in s.indices) {
            if (x == 1L) return false
            val r = x % 3L
            val c = if (r == 0L) 'D' else if (r == 1L) 'U' else 'd'
            if (c != s[i]) return false
            x = when (c) {
                'D' -> x / 3
                'U' -> (4 * x + 2) / 3
                else -> (2 * x - 1) / 3
            }
        }
        return true
    }

    // 逐字符提升剩余类 r mod 3^i（类内换代表元以避开 a_1 = 1 的退化）
    var r = 0L
    var mod = 1L
    for (i in target.indices) {
        val lower = mod
        mod *= 3
        val want = target.substring(0, i + 1)
        var found = -1L
        for (t in 0..2) {
            val base = r + t * lower
            var hits = false
            for (m in 0..(i + 2)) {
                val cand = base + m * mod
                if (cand >= 1L && beginsWith(cand, want)) { hits = true; break }
            }
            if (hits) {
                check(found < 0L) { "多个提升候选命中" }
                found = base
            }
        }
        check(found >= 0L) { "第 ${i + 1} 位没有提升候选命中" }
        r = found
    }

    // 类内取严格大于 10^15 的最小元素（并确认它真的产生目标串）
    var answer = r
    if (answer <= lowerBound) answer += ((lowerBound - answer) / mod + 1) * mod
    var guard = 0
    while (!beginsWith(answer, target)) {
        answer += mod
        check(++guard <= 64) { "类内退化元素过多" }
    }
    return answer
}
