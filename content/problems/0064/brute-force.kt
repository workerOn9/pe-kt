/**
 * Project Euler 064 — 暴力解（教学对比用）
 *
 * 不套用 (m,d,a) 闭式递推：把余项显式写成 (√N + P)/Q，用线性探测确定 aₖ
 * （a 从小到大试到 (a·Q − P)² > N 为止），并用「余项状态 (P,Q) 回到起点」判定周期闭合——
 * 一种不依赖理论、只看状态重演的朴素周期检测。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isqrtB(n: Int): Int {
    var r = Math.sqrt(n.toDouble()).toInt()
    while (r.toLong() * r > n) r--
    while ((r + 1).toLong() * (r + 1) <= n) r++
    return r
}

/** 线性探测最大的 a 使 a·Q − P ≤ √N（即 ⌊(√N + P)/Q⌋）。 */
fun floorSqrtFrac(n: Int, p: Int, q: Int): Int {
    var a = 1
    while (true) {
        val v = a * q - p
        if (v > 0 && v.toLong() * v > n) break
        a++
    }
    return a - 1
}

fun solveBruteForce(): Long {
    var count = 0L
    for (n in 2..10000) {
        val a0 = isqrtB(n)
        if (a0 * a0 == n) continue
        val p0 = a0
        val q0 = n - a0 * a0
        var p = p0
        var q = q0
        var period = 0
        while (true) {
            val a = floorSqrtFrac(n, p, q)
            val np = a * q - p
            q = ((n - np.toLong() * np) / q).toInt()
            p = np
            period++
            if (p == p0 && q == q0) break
        }
        if (period and 1 == 1) count++
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
