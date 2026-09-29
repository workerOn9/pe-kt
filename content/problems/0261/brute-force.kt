#!/usr/bin/env kotlin
/**
 * Project Euler 261 — Pivotal Square Sums（关键平方和）· 直接暴力对照
 *
 * 完全按定义实现，不使用推导出的 (m,z)/Pell 结构：
 *   对每个 k（1..kMax）枚举全部 m < k。把等式
 *       (m+1)·k·(k−m) = m·n·(n+m+1)
 *   （由两个平方和用闭式 S₂(x)=x(x+1)(2x+1)/6 相减并两边除以 (k−(k−m−1))=m+1 得到；
 *   这一步只是「把定义式写成整式」）看成关于 n 的二次方程
 *       n² + (m+1)·n − (m+1)k(k−m)/m = 0，
 *   当 (m+1)k(k−m) 能被 m 整除且判别式 (m+1)² + 4(m+1)k(k−m)/m 是完全平方时得到整数 n；
 *   再用 n ≥ k 过滤，最后**逐项相加**两个平方和、直接验证相等（不信任任何闭式展开）。
 *   k 一旦被验证即为 pivot，记下并跳到下一个 k。
 *
 * 复杂度 O(K²)·(一次整数除法 + 一次平方根)，K = 20000 时约 2×10⁸ 次判断；
 * 完整规模 K = 10¹⁰ 不可行（约 5×10¹⁹ 次），只用于小规模对照。
 *
 * 运行：bash scripts/kotlinc-shim.sh content/problems/0261/brute-force.kt
 *       java -cp /tmp/kc-261-brute:… Brute_forceKt
 */

/** 整数平方根（Long，向下取整）。 */
private fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** 按定义求出所有 ≤ kMax 的 pivot，升序。 */
private fun brutePivots(kMax: Int): LongArray {
    val out = ArrayList<Long>()
    for (k in 1..kMax) {
        var m = 1
        while (m < k) {
            val v = (m + 1).toLong() * k * (k - m)
            if (v % m == 0L) {
                val q = v / m
                val disc = (m + 1).toLong() * (m + 1) + 4 * q
                val r = isqrt(disc)
                if (r * r == disc && (r - (m + 1)) % 2 == 0L) {
                    val n = (r - (m + 1)) / 2
                    if (n >= k) {
                        // 直接逐项验证两个平方和（定义即此）
                        var left = 0L
                        for (x in (k - m)..k) left += x.toLong() * x
                        var right = 0L
                        for (x in (n + 1)..(n + m)) right += x * x
                        if (left == right) {
                            out.add(k.toLong())
                            break
                        }
                    }
                }
            }
            m++
        }
    }
    return out.toLongArray()
}

fun main() {
    println("直接暴力（按定义对每个 k 枚举 m 并解二次方程，再逐项验证平方和）")
    for (kMax in intArrayOf(1_000, 5_000, 20_000)) {
        val t0 = System.nanoTime()
        val res = brutePivots(kMax)
        val ms = (System.nanoTime() - t0) / 1e6
        var sum = 0L
        for (k in res) sum += k
        println("K=$kMax：${res.size} 个 pivot，Σ = $sum（${"%.1f".format(ms)} ms）")
    }
    println("前 12 个 pivot：${brutePivots(5_000).take(12)}")
    println("题面锚点：4、21、24、110 都在其中 → " +
        "${listOf(4L, 21L, 24L, 110L).all { it in brutePivots(200).toList() }}")

    // K=20000 的 JIT 预热后 3 轮最优
    brutePivots(2_000)
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val res = brutePivots(20_000)
        val ms = (System.nanoTime() - t0) / 1e6
        check(res.size == 120) { "K=20000 结果漂移：${res.size} 个" }
        if (ms < best) best = ms
        println("K=20000 第 ${round + 1} 轮：${res.size} 个 pivot（${"%.1f".format(ms)} ms）")
    }
    println("K=20000：${"%.1f".format(best)} ms（JIT 预热后 3 轮最优）")

    // K=100000 一次性（O(K²) 的实用上限附近）
    val t0 = System.nanoTime()
    val big = brutePivots(100_000)
    val ms = (System.nanoTime() - t0) / 1e6
    var sum = 0L
    for (k in big) sum += k
    println("K=100000：${big.size} 个 pivot，Σ = $sum（${"%.1f".format(ms)} ms）")
}
