#!/usr/bin/env kotlin
/**
 * Project Euler 231 — Prime Factorisation of Binomial Coefficients · 暴力对照版
 *
 * 思路：两条与 solution.kt 的优化路径无关的朴素实现，用来对拍。
 *
 *   (1) 真暴力 trialFactorSum：把 C(n,k) = (n-k+1)·…·n / (1·…·k) 的分子、分母各自试除分解，
 *       指数相减后求和，一点 Legendre 公式都不用。代价 O(k√n)，只能在小规模上跑。
 *
 *   (2) 慢速 Legendre 扫描 naiveScanSum：公式与优化解相同，但把「整除计数」由一次除法
 *       ⌊n/q⌋ 换成**逐个倍数地数**（v += q 循环计数），代价从 O(π(N) log N) 变成
 *       O(Σ_p N/(p-1)) = O(N log log N) 的常数倍，用作同公式不同粒度的耗时对比。
 *       ⚠️ 注意：不能把商式的 q 直接换成「q += p 扫所有倍数」——那不是 Legendre 公式
 *       （它只在 q 取素数幂 p, p², p³… 时成立），会算出偏大的错误结果。
 *
 *       顺带一提：真去算 C(2e7, 1.5e7) 是死路——它约有 1.8×10^6 位十进制数，
 *       试除到 √C 属于 10^900000 量级。
 *
 * 复杂度：(1) O(k√n)；(2) O(N log log N) 次整除计数。
 * 构建：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 * 运行：java -jar bf.jar
 */

/** (1) 真暴力：分子分母各自试除分解，指数相减后求和。 */
fun trialFactorSum(n: Int, k: Int): Long {
    val exps = HashMap<Int, Int>()
    fun factor(x0: Int, sign: Int) {
        var x = x0
        var d = 2
        while (d * d <= x) {
            while (x % d == 0) { exps[d] = (exps[d] ?: 0) + sign; x /= d }
            d++
        }
        if (x > 1) exps[x] = (exps[x] ?: 0) + sign
    }
    for (i in n - k + 1..n) factor(i, +1)
    for (i in 1..k) factor(i, -1)
    return exps.entries.sumOf { (p, e) -> if (e > 0) p.toLong() * e else 0L }
}

/** (2) 慢速版：Legendre 公式的素数幂循环不变，但整除计数改成逐倍数扫描。 */
fun naiveScanSum(n: Int, k: Int): Long {
    val m = n - k
    val isP = BooleanArray(n + 1)
    var i = 2
    while (i * i <= n) {
        if (!isP[i]) { var j = i * i; while (j <= n) { isP[j] = true; j += i } }
        i++
    }
    fun countMultiples(limit: Int, q: Int): Int {
        var c = 0
        var v = q
        while (v <= limit) { c++; v += q }
        return c
    }
    var total = 0L
    var p = 2
    while (p <= n) {
        if (!isP[p]) {
            var e = 0
            var q = p
            while (q <= n) {
                e += countMultiples(n, q) - countMultiples(k, q) - countMultiples(m, q)
                if (q > n / p) break   // 防止 q *= p 溢出
                q *= p
            }
            if (e != 0) total += p.toLong() * e
        }
        p++
    }
    return total
}

fun main() {
    println("== 小规模：真暴力（试除分解）vs 慢速扫描 Legendre ==")
    for ((a, b) in listOf(10 to 3, 20 to 10, 100 to 40, 1000 to 500, 5000 to 2500)) {
        val t = trialFactorSum(a, b)
        val f = naiveScanSum(a, b)
        println("C($a,$b): trial=$t slow-legendre=$f  ${if (t == f) "OK" else "MISMATCH"}")
    }
    val t0 = System.nanoTime()
    val bf = trialFactorSum(20000, 15000)
    println("C(20000,15000): trial=$bf  真暴力耗时 ${(System.nanoTime() - t0) / 1_000_000} ms")

    println()
    println("== 慢速扫描的规模曲线 ==")
    for (e in 14..24 step 2) {
        val n = 1 shl e
        val t = System.nanoTime()
        val s = naiveScanSum(n, n / 2)
        println("  n=2^$e ($n) -> $s   ${(System.nanoTime() - t) / 1_000_000} ms")
    }

    println()
    val t1 = System.nanoTime()
    val s = naiveScanSum(20_000_000, 15_000_000)
    val ms = (System.nanoTime() - t1) / 1_000_000
    println("慢速扫描 C(2e7,1.5e7) = $s  ($ms ms)")
    println("（真暴力在此规模不可行：C(2e7,1.5e7) 有约 1.8e6 位十进制数，试除到 √C 无望）")
}
