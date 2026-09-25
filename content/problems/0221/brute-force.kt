/**
 * Project Euler 221 — brute-force 对照版（ Alexandrian Integers）
 *
 * 思路：不开根筛，对每个 x 从 1 逐个试除 x^2+1 到 sqrt(x^2+1)，找到全部
 * 小因子 s（配对 t=(x^2+1)/s），直接按 x^3 <= cap 逐步扫。
 * 与 optimized 版比时间慢约 120 倍（数判预计 4 分钟）── 展示打标筛费的性能差。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

import kotlin.math.sqrt

fun main() {
    val cap = 3_000_000_000_000_000L    // 生成上限，同 optimized 版
    val answers = ArrayList<Long>()
    answers.add(6L)                     // x=1, x^2+1=2, s=1: 1·2·3
    var x = 2L
    while (x * x * x <= cap) {
        val xsq1 = x * x + 1
        // 直接试除，慢在小循环里 sqrt(x^2+1) 每次重算
        var d = 2L
        val sq = sqrt(xsq1.toDouble()).toLong()
        while (d <= sq) {
            if (xsq1 % d == 0L) {
                val t = xsq1 / d
                val m1 = x * (x + d)
                if (m1 <= cap / (x + t)) {
                    answers.add(m1 * (x + t))
                }
            }
            d++
        }
        // s=1（配对 t=xsq1）也要算 —— 从 1 起扫
        val m1 = x * (x + 1)
        if (m1 <= cap / (x + xsq1)) answers.add(m1 * (x + xsq1))
        x++
    }
    answers.sort()
    println("answer #150000 = ${answers[149999]}")
    println("total ≤ cap: ${answers.size}")
}
