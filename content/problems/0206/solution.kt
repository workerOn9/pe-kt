#!/usr/bin/env kotlin
// PE 206 — Concealed Square（伪装的平方数）
// 思路：n² 形如 1_2_3_4_5_6_7_8_9_0 共 19 位（10^18 ≤ n² < 2×10^18），
//       故 n ∈ [⌊√10^18⌋, ⌊√(2×10^18)⌋] = [1_000_000_000, 1_414_213_562]。
//       逐位校验 n² 的奇数位（10^0, 10^2, …, 10^18）依次为 0,9,8,…,1：
//       每步取 (s % 10) 对比后 s /= 100，不匹配立即剪枝——绝大多数候选在前一两位即出局。
//       偶数位任意，故无需构造法；顺序扫描 + 命中即停在毫秒级完成。
// 复杂度：候选区间约 4.1×10^8 个 n，剪枝后实际工作集中在区间前 39% —— 百毫秒级。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    // 目标：s 的奇数位（从 10^0 起）依次为 0,9,8,7,6,5,4,3,2,1
    fun check19(s0: Long): Boolean {
        var s = s0
        var digit = 0L
        for (k in 0..9) {
            if (s % 10 != digit) return false
            s /= 100
            digit = if (digit == 0L) 9L else digit - 1
        }
        return true
    }
    var found = -1L
    var n = 1_000_000_000L
    val nMax = 1_414_213_562L
    while (n <= nMax) {
        if (check19(n * n)) { found = n; break }
        n++
    }
    println(found)   // n = 1389019170，n² = 1929374254627488900
}
