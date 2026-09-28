#!/usr/bin/env kotlin
/**
 * Project Euler 242 — brute-force：子集 DP 数奇三元组（与 solution.kt 完全不同路）。
 *
 * solution.kt 走的是「闭式 f(n,k) → mod 4 判偶 → Lucas → 按位 DP」这条组合论路线，
 * 中间要证 C(2a,2b) ≡ C(a,b) (mod 4) 之类的同余。本文件一条也不用：
 *
 *   逐个把元素 1, 2, …, n 塞进集合，维护两个数组
 *
 *       even[k] = 已处理的元素里，k 元子集元素和为偶数的个数
 *       odd[k]  = …元素和为奇数的个数
 *
 *   元素 n 是偶数就保持奇偶、是奇数就翻转奇偶，所以转移只是两个加法：
 *
 *       n 偶： even[k+1] += even[k]，odd[k+1] += odd[k]
 *       n 奇： even[k+1] += odd[k]， odd[k+1] += even[k]
 *
 *   f(n,k) 就是 odd[k]。这直接就是 f 的定义，不含任何闭式。
 *   随后按定义逐个 (n,k) 筛出 n 奇、k 奇、f(n,k) 奇的三元组。
 *
 * 规模：子集 DP 是 O(n^2) 次大整数加法、且 n 每加 1 都要把 C(n, n/2) 级别的数搬一遍，
 * 10^12 的上界完全跑不动，所以本文件只在 N = 10 / 100 / 1000 / 3000 / 5000 上跑，
 * 定位是「solution.kt 的判偶刻画对不对」的裁判。
 *
 * 另：timing 取 N = 3000 的这一次完整扫描（JIT 预热后另测 3 次取中位数）。
 */

private fun tripletsSubsetDp(nMax: Int): Long {
    // C(n, n/2) 在 n = 100 时就超过 2^63（≈1.0×10^29），所以必须用 BigInteger
    var even = arrayOf(java.math.BigInteger.ONE) // k = 0 的空集，和为偶
    var odd = arrayOf(java.math.BigInteger.ZERO)
    var total = 0L
    for (n in 1..nMax) {
        val e = even.copyOf(n + 1).also { it[n] = java.math.BigInteger.ZERO }
        val o = odd.copyOf(n + 1).also { it[n] = java.math.BigInteger.ZERO }
        val flip = (n % 2 == 1)
        for (k in n - 1 downTo 0) {
            if (flip) {
                e[k + 1] = e[k + 1]!!.add(odd[k]!!)
                o[k + 1] = o[k + 1]!!.add(even[k]!!)
            } else {
                e[k + 1] = e[k + 1]!!.add(even[k]!!)
                o[k + 1] = o[k + 1]!!.add(odd[k]!!)
            }
        }
        even = e
        odd = o
        if (n % 2 == 1) {
            for (k in 1..n step 2) if (odd[k]!!.testBit(0)) total++
        }
    }
    return total
}

/** solution.kt 的判定式（Lucas：C(m,r) 奇 ⟺ r ⊆ m），这里只用来对拍 */
private fun countByLucas(nMax: Int): Long {
    var total = 0L
    for (n in 1..nMax step 2) {
        val m = (n - 1) / 2
        if (m % 2 != 0) continue
        for (r in 0..m) if ((r and m) == r) total++
    }
    return total
}

private fun main() {
    // 题面样例：n ≤ 10 恰好 5 个
    val t10 = tripletsSubsetDp(10)
    println("subset DP, n <= 10    -> $t10   (题面 5)")
    check(t10 == 5L) { "题面样例不吻合：$t10" }

    // 与 solution.kt 的闭式逐点对拍
    val bounds = listOf(10, 100, 1000, 3000, 5000)
    val expected = listOf(5L, 139L, 5793L, 29829L, 68331L)
    for ((bound, exp) in bounds.zip(expected)) {
        val dp = tripletsSubsetDp(bound)
        val lucas = countByLucas(bound)
        val formula = (0..(bound - 1) / 4).sumOf { 1L shl Integer.bitCount(it) }
        println("n <= ${"%5d".format(bound)}: subsetDP=$dp  lucas=$lucas  formula=$formula")
        check(dp == exp && lucas == exp && formula == exp) { "n <= $bound 三路不一致" }
    }

    // 计时：JIT 预热后跑 3 次 N = 3000，取中位数
    repeat(3) { tripletsSubsetDp(3000) }
    val samples = DoubleArray(3)
    for (i in samples.indices) {
        val t0 = System.nanoTime()
        val r = tripletsSubsetDp(3000)
        samples[i] = (System.nanoTime() - t0) / 1e6
        check(r == 29829L) { "计时中结果漂移" }
    }
    samples.sort()
    println("brute-force: ${"%.3f".format(samples[1])} ms（n <= 3000，3 次取中位数）")
    println("brute-force OK（子集 DP 与闭式在五个上界上逐位一致）")
}
