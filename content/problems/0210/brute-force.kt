#!/usr/bin/env kotlin
// PE 210 暴力参照：直接枚举 S(r) 中每个 B，用三个点积判钝角（与 O、C 共线则跳过）。
// 只对小 r 可行（O(r²)），用于验证闭式与圆盘点数的正确性：N(4)=24、N(8)=100。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

fun main() {
    for (r in intArrayOf(4, 8, 16, 24, 32, 40, 60, 1000, 2000)) {
        val t0 = System.nanoTime()
        val n = brute(r)
        val ms = (System.nanoTime() - t0) / 1_000_000
        println("N($r) = $n  (${ms} ms)")
    }
}

private fun brute(r: Int): Long {
    val c = r / 4L
    var tot = 0L
    for (x in -r..r) {
        for (y in -r..r) {
            if (Math.abs(x) + Math.abs(y) > r) continue
            if (x.toLong() == y.toLong()) continue              // 与 O、C 共线 → 三角形退化
            val dO = x * c + y * c                              // (B−O)·(C−O)
            val dB = x.toLong() * x + y.toLong() * y - c * (x + y)   // (O−B)·(C−B)
            val dC = c * (2 * c - x - y)                         // (O−C)·(B−C)
            if (dO < 0 || dB < 0 || dC < 0) tot++
        }
    }
    return tot
}
