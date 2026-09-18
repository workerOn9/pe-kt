/**
 * Project Euler 154 — Exploring Pascal's Pyramid（探索帕斯卡金字塔）
 *
 * 思路：多项式系数 C(n, i, j, k) = n! / (i! j! k!) 当 i+j+k=n。
 * 数其被 10^12 整除的个数。10^12 = 2^12 · 5^12。
 *
 * 应用 Lucas 定理（推广到三项式）按素因子分别计数，最后用容斥合并。
 *
 * 关键点：设 v_p(n!) = Σ_{i=1}^∞ ⌊n/p^i⌋ 为 p-adic 赋值。
 * 系数 C(n, i, j, k) 的 v_p 赋值为 v_p(n!) - v_p(i!) - v_p(j!) - v_p(k!)。
 *
 * 目标：v_2(...) ≥ 12 且 v_5(...) ≥ 12。
 *
 * 用「数论分块」和卢卡斯定理，可对 n=200000 在毫秒级内完成。
 * 本实现采用通用方法：枚举 i 和 j（k = n - i - j），对每对用 Legendre 公式求 v_2 和 v_5，统计两者都满足阈值的对数。
 */

fun solve(): Long {
    val n = 200000L
    val p2 = 2L
    val p5 = 5L
    val exp2 = 12  // 2^12 因子
    val exp5 = 12  // 5^12 因子
    
    fun v_p(n: Long, p: Long): Long {
        var count = 0L
        var p_pow = p
        while (p_pow <= n) {
            count += n / p_pow
            p_pow *= p
        }
        return count
    }
    
    // 预计算 v_p(k!) 表：factorial_p[k] = v_p(k!)
    val maxK = n.toInt() + 1
    val factP2 = LongArray(maxK)
    val factP5 = LongArray(maxK)
    for (k in 1 until maxK) {
        factP2[k] = factP2[k - 1] + v_p(k.toLong(), p2)
        factP5[k] = factP5[k - 1] + v_p(k.toLong(), p5)
    }
    
    var count = 0L
    for (i in 0..n.toInt()) {
        val jMax = (n - i).toInt()
        val v2_n = factP2[n.toInt()]
        val v5_n = factP5[n.toInt()]
        val v2_i = factP2[i]
        val v5_i = factP5[i]
        for (j in 0..jMax) {
            val k = n.toInt() - i - j
            val v2 = v2_n - v2_i - factP2[j] - factP2[k]
            val v5 = v5_n - v5_i - factP5[j] - factP5[k]
            if (v2 >= exp2 && v5 >= exp5) count++
        }
    }
    
    return count
}

fun main() {
    repeat(3) { solve() }  // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}