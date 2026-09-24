#!/usr/bin/env kotlin
// PE 216 — The Primality of 2n² − 1（t(n) = 2n² - 1 的素性）暴力参照
// 思路：不做二次剩余筛，对每个 n 直接用试除法判断 t(n) = 2n² - 1 的素性。
//       t(n) ≤ 5×10⁹ 时试除到 √t(n) ≈ 7.07×10⁴ 即可，故只适合 n ≤ 10⁵ 的量级；
//       用来复现题面锚点「n ≤ 10000 有 2202 个素数」，并与 solution.kt 的
//       二次剩余分段筛在同一区间上对照（n ≤ 50000 应为 9175）。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

private fun isPrimeTrial(v: Long): Boolean {
    if (v < 2L) return false
    if (v % 2L == 0L) return v == 2L
    if (v % 3L == 0L) return v == 3L
    var i = 5L
    while (i * i <= v) {
        if (v % i == 0L) return false
        if (v % (i + 2) == 0L) return false
        i += 6
    }
    return true
}

/** 直接试除计数：t(n) 为素数的 n 个数，n ∈ [2, nMax]。 */
private fun countByTrial(nMax: Long): Long {
    var count = 0L
    for (n in 2..nMax) {
        if (isPrimeTrial(2 * n * n - 1)) count++
    }
    return count
}

fun main() {
    val t0 = System.nanoTime()
    println("n ≤ 10000 ：${countByTrial(10_000)}（题面锚点 2202）")
    println("n ≤ 50000 ：${countByTrial(50_000)}")
    val ms = (System.nanoTime() - t0) / 1_000_000
    System.err.println("试除暴力 n ≤ 50000 wall = $ms ms")
}
