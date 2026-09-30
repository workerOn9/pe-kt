#!/usr/bin/env kotlin
/**
 * Project Euler 282 — The Ackermann Function（阿克曼函数）：暴力 / 独立对照
 *
 * 独立实现，与 solution.kt 不共享核心代码：主解用「Long 取模 + 广义欧拉修正项（+φ）的 φ 链递归」，
 * 这里换成「BigInteger 字面指数阶梯」——凡能物化的指数都物化成真整数，用 BigInteger.modPow 精确
 * 计算，只在指数大到无法物化时才做一次欧拉降幂。两者仅在「阿克曼 = 高德纳箭头塔」这一层结构上共通。
 *
 * 为什么不能全程暴力
 * ────────────────
 * A(5,5) = 2↑↑↑8 − 3、A(6,6) = 2↑↑↑↑9 − 3 的十进制位数超过 10^65000 位，直接递归展开在物理上不可行
 * （题面给的三例只到 A(3,4) = 125）。因此暴力分两段：
 *   ① 定义级穷举对拍（小参数）：定义直译递归 + 记忆化，复现题面全部样例，并与超运算恒等式
 *      A(m,n) = 2 ↑^{m−2} (n+3) − 3 在 m ∈ {2,3,4} 的可算范围内逐点对拍；
 *   ② 字面指数阶梯（全尺寸）：把 2↑↑5 = 2^65536 精确物化（19729 位），h = 6 时直接用这个真指数
 *      做 modPow（不降幂！）；h ≥ 7 才用一次欧拉降幂 / 偶模数按「2 部分 = 0」拆分。塔高进入稳定段
 *      后（实测 h ≥ 10）数值不再变化，而 A(5,5)、A(6,6) 的塔高 ≥ 2↑↑65536，远超稳定段——这就是
 *      大高度项的「外推」依据，程序里把 h = 1..60 的整段与 h = 10^6、10^6+1 全部打印出来核对。
 *
 * 与 solution.kt 的对照结论
 * ──────────────────────
 *   A(4,4) mod 14^8：本文件 915627005 = solution.kt 主路径 915627005
 *   A(5,5) / A(6,6) mod 14^8：829575165（两者稳定后同值）
 *   总和 mod 14^8：1098988351（= 1 + 3 + 7 + 61 + 915627005 + 2·829575165 mod 14^8）
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0282/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

import java.math.BigInteger

private val MOD = 1475789056L                   // 14^8
private const val BIG_HEIGHT = 1_000_000        // 「足够大的塔高」代表值

// ─────────────────────────── 小工具 ───────────────────────────

private fun phiOf(m0: Long): Long {
    var x = m0
    var res = m0
    var p = 2L
    while (p * p <= x) {
        if (x % p == 0L) {
            while (x % p == 0L) x /= p
            res -= res / p
        }
        p++
    }
    if (x > 1L) res -= res / x
    return res
}

private fun modInverse(a: Long, m: Long): Long {
    var oldR = m
    var r = Math.floorMod(a, m)
    var oldS = 0L
    var s = 1L
    while (r != 0L) {
        val q = oldR / r
        val t1 = oldR - q * r; oldR = r; r = t1
        val t2 = oldS - q * s; oldS = s; s = t2
    }
    check(oldR == 1L) { "逆元不存在" }
    return Math.floorMod(oldS, m)
}

private fun crt(a: Long, p: Long, b: Long, q: Long): Long {
    if (p == 1L) return Math.floorMod(b, q)
    if (q == 1L) return Math.floorMod(a, p)
    val t = Math.floorMod(b - a, q) * modInverse(p % q, q) % q
    return a + p * t
}

// ─────────────────────── ① 定义级穷举对拍 ───────────────────────

private fun <T> withBigStack(block: () -> T): T {
    var result: T? = null
    var failure: Throwable? = null
    val th = Thread(null, {
        try {
            result = block()
        } catch (e: Throwable) {
            failure = e
        }
    }, "big-stack", 256L * 1024 * 1024)
    th.start()
    th.join()
    failure?.let { throw it }
    @Suppress("UNCHECKED_CAST")
    return result as T
}

/** 定义直译（不带任何闭式），递归深度可达数万层，故放在大栈线程里跑。 */
private fun ackermann(m: Int, n: Long): BigInteger = withBigStack {
    val memo = HashMap<Long, BigInteger>()
    fun go(am: Int, an: Long): BigInteger {
        val k = am * 1_000_000L + an
        memo[k]?.let { return it }
        val v = if (am == 0) {
            BigInteger.valueOf(an + 1L)
        } else if (an == 0L) {
            go(am - 1, 1L)
        } else {
            go(am - 1, go(am, an - 1L).toLong())
        }
        memo[k] = v
        return v
    }
    go(m, n)
}

/** 恒等式右侧 2 ↑^{m−2} (n+3) − 3 在小参数下的精确值（m = 2,3 用闭式；m = 4 只有 n ≤ 1 可直接递归）。 */
private fun hyperSmall(m: Int, n: Long): BigInteger = when (m) {
    2 -> BigInteger.valueOf(2L * (n + 3L) - 3L)
    3 -> BigInteger.TWO.pow((n + 3L).toInt()).subtract(BigInteger.valueOf(3L))
    4 -> when (n) {                                   // 2↑↑(n+3) − 3，n+3 ≤ 4 时可物化
        0L -> BigInteger.valueOf(13L)                 // 2↑↑3 = 16
        1L -> BigInteger.valueOf(65533L)              // 2↑↑4 = 65536
        else -> error("m = 4, n = $n 的塔高 ≥ 5，需 BigInteger 塔（见 ② 段）")
    }
    else -> error("m = $m 不在此函数覆盖范围")
}

// ─────────────────────── ② 字面指数阶梯 ───────────────────────

/**
 * 2↑↑h mod m，指数尽量字面物化：
 *   h ≤ 5：2↑↑h 的指数是 1、2、4、16、65536，全部是 Int 级真数；
 *   h = 6：指数 = 2↑↑5 = 2^65536（19729 位 BigInteger），直接 modPow，**不做任何降幂**；
 *   h ≥ 7：指数 2↑↑(h−1) 无法物化。m 奇时用一次欧拉定理（gcd(2,m) = 1，无修正项），
 *          m 偶时拆 m = 2^s·q：2 部分 = 0（指数 ≥ 2^65536 ≥ s），奇数部分同奇数情形，CRT 合并。
 */
private fun towerBig(h: Int, m: Long): Long {
    if (m == 1L) return 0L
    if (h <= 6) {
        val e: BigInteger = when (h) {
            1 -> BigInteger.ONE
            2 -> BigInteger.TWO
            3 -> BigInteger.valueOf(4L)
            4 -> BigInteger.valueOf(16L)
            5 -> BigInteger.valueOf(65536L)
            else -> BigInteger.TWO.pow(65536)
        }
        return BigInteger.TWO.modPow(e, BigInteger.valueOf(m)).toLong()
    }
    var s = 0
    var q = m
    while (q and 1L == 0L) { q = q shr 1; s++ }
    val partQ = if (q == 1L) 0L else {
        val e = BigInteger.valueOf(towerBig(h - 1, phiOf(q)))
        BigInteger.TWO.modPow(e, BigInteger.valueOf(q)).toLong()
    }
    return crt(0L, 1L shl s, partQ, q)
}

/** A(n,n) mod m（超运算恒等式）：n ≤ 3 查表；n = 4 塔高 7；n ≥ 5 塔高 ≥ 2↑↑65536，用 BIG_HEIGHT 代表。 */
private fun termMod(n: Int, m: Long): Long = when (n) {
    0 -> 1L % m
    1 -> 3L % m
    2 -> 7L % m
    3 -> 61L % m
    4 -> Math.floorMod(towerBig(7, m) - 3L, m)
    else -> Math.floorMod(towerBig(BIG_HEIGHT, m) - 3L, m)
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.4f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    println("== ① 定义级穷举对拍（题面样例 + 小参数恒等式）==")
    val want = listOf(Triple(1, 0L, BigInteger.valueOf(2L)), Triple(2, 2L, BigInteger.valueOf(7L)),
        Triple(3, 4L, BigInteger.valueOf(125L)))
    for ((m, n, w) in want) {
        val got = ackermann(m, n)
        check(got == w) { "题面样例 A($m,$n) 应为 $w，实算 $got" }
        println("A($m, $n) = $got（题面 ${w}）")
    }
    for (m in 0..4) {
        for (n in 0L..5L) {
            if (m == 4 && n > 1L) continue                       // A(4,2) 需要 32765 层递归，另测
            if (m >= 2) {
                val viaDef = ackermann(m, n)
                val viaHyper = hyperSmall(m, n)
                check(viaDef == viaHyper) { "A($m,$n) 定义 $viaDef ≠ 恒等式 $viaHyper" }
            }
        }
    }
    println("m ∈ {2,3}、n = 0..5 与 m = 4、n = 0..1：定义直译 ≡ 2 ↑^{m−2}(n+3) − 3 ✓")
    // A(4,2) = A(3, A(4,1))：定义链只到 A(4,1)，外层用「A(3,x) = 2^{x+3} − 3」的已对拍闭式
    val a41 = ackermann(4, 1)
    check(a41 == BigInteger.valueOf(65533L)) { "A(4,1) 应为 65533" }
    val a42 = BigInteger.TWO.pow(65536).subtract(BigInteger.valueOf(3L))
    check(BigInteger.TWO.pow(65536).bitLength() == 65537) { "2^65536 位长应为 65537" }
    println("A(4,1) = $a41；A(4,2) = A(3, A(4,1)) = 2^65536 − 3 = 2↑↑5 − 3 ✓（位长 ${a42.bitLength() + 1}）")

    println()
    println("== ② 字面指数阶梯：h = 1..60 与 10^6（mod 14^8）==")
    val seq = LongArray(60)
    for (h in 1..60) seq[h - 1] = towerBig(h, MOD)
    var h0 = 60
    while (h0 > 1 && seq[h0 - 2] == seq[h0 - 1]) h0--
    for (k in h0..60) check(seq[k - 1] == seq[h0 - 1]) { "h = $k 处不稳定" }
    check(towerBig(BIG_HEIGHT, MOD) == seq[h0 - 1] && towerBig(BIG_HEIGHT + 1, MOD) == seq[h0 - 1]) {
        "10^6 高度与稳定段不符"
    }
    println("h = 1..60 的 2↑↑h mod 14^8 序列（前 12 项）：${seq.take(12).joinToString(", ")}")
    println("h ≥ $h0 后恒为 ${seq[h0 - 1]}；h = 10^6 与 10^6+1 亦为该值 ⇒ 2↑↑↑8、2↑↑↑↑9 的塔高远超稳定阈 ✓")
    check(seq[6] == 915627008L) { "2↑↑7 mod 14^8 应为 915627008（= A(4,4) + 3）" }

    println()
    println("== 全尺寸求值：Σ_{n=0..6} A(n,n) mod 14^8（字面阶梯路线）==")
    val terms = (0..6).map { termMod(it, MOD) }
    println("各项：${terms.joinToString(", ")}")
    var sum = 0L
    for (t in terms) sum = (sum + t) % MOD
    println("Σ mod 14^8 = $sum")
    check(terms[4] == 915627005L && terms[5] == 829575165L && terms[6] == 829575165L) { "各项与主解不一致" }

    println()
    println("== 计时（与 solution.kt 同口径：JIT 预热后 3 轮最优）==")
    val msBrute = bestOf3("字面 BigInteger 阶梯（全部 7 项 + 稳定段外推）", sum) {
        var s = 0L
        for (n in 0..6) s = (s + termMod(n, MOD)) % MOD
        s
    }
    println()
    println("暴力路径答案 = $sum（与 solution.kt 主路径 / CRT 路径一致）")
    println("暴力全量计时 ${"%.4f".format(msBrute)} ms")
}
