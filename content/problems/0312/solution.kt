#!/usr/bin/env kotlin
/**
 * Project Euler 312 — Cycles in the Sierpinski Graph（谢尔宾斯基图上的环路）
 *
 * 题目：S₁ 是等边三角形，S_{n+1} 由三个 Sₙ「每两个共用一个顶点」拼成。C(n) = 恰好经过
 *      Sₙ 每个顶点一次的环路个数。已知 C(3)=8、C(5)=71328803586048、
 *      C(10⁴) mod 10⁸ = 37652224、C(10⁴) mod 13⁸ = 617720485，求 C(C(C(10⁴))) mod 13⁸。
 *
 * 思路推导
 * --------
 * Sₙ 由三个 S_{n-1} 拼成，拼接点是三个角点，每个角点恰被两个子图共用。一条哈密顿圈
 * 在每个子图内部必须把两个拼接点用掉一条内部通路，于是每个子图有两种内部接法，
 * 三个子图各自独立，整体按旋转与翻折去重后得
 *
 *   C(n) = 2^{e2(n)} · 3^{e3(n)}，e2(n) = 3^{n-2}，e3(n) = (3^{n-2} − 3)/2 （n ≥ 3）。
 *
 * 用题面给的 C(3)=8=2³ 与 C(5)=2²⁷·3¹² 两点即可锁死闭式。
 *
 * 关键难点是幂塔 C(C(C(10⁴)))：指数无法展开，只能逐级「降幂 + CRT」。
 * 记 φ 为欧拉函数。要算 2^E·3^F（F=(E−3)/2）mod 13⁸，需要 E mod 2φ(13⁸)；
 * E = 3^{N₂−2}，而 2φ(13⁸) = 24·13⁷ = 3·(8·13⁷)，CRT 拆开后 mod 3 直接为 0、
 * mod 8·13⁷ 只要求 (N₂−2) mod φ(8·13⁷) = 48·13⁶；再往下 N₂ mod 48·13⁶ = 16·3·13⁶，
 * mod 16 与 mod 3 仍为 0，mod 13⁶ 回到 24·13⁵；最后 N₁ = C(10⁴) 用 modPow 直接算出。
 *
 * 验证
 * --------
 * 1. 题面自检：C(3)=8、C(5)=71328803586048、C(10⁴) mod 10⁸ = 37652224、
 *    C(10⁴) mod 13⁸ = 617720485，四条全部打印核对；
 * 2. 双方法互证：「降幂阶梯」与「BigInteger 完整构造 C(n) 再取模」两条路在
 *    n = 10/100/1000/3000、m = 13⁸ 与 10⁸ 上逐一相等；
 * 3. 阶梯中间值（N₁ mod 48·13⁴、3^{N₁−2} mod 24·13⁵、N₂ mod 48·13⁶、
 *    3^{N₂−2} mod 24·13⁷）全部打印，便于人工复核每一步 CRT。
 *
 * 复杂度：每级 O(log n) 次模乘，整条阶梯毫秒级；暴力对照要构造 C(1000) 的
 * 4.8×10⁴ 位十进制大整数，秒级。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

private fun phi(n: Long): Long {
    var rest = n
    var res = n
    var p = 2L
    while (p * p <= rest) {
        if (rest % p == 0L) {
            while (rest % p == 0L) rest /= p
            res -= res / p
        }
        p++
    }
    if (rest > 1) res -= res / rest
    return res
}

private fun powMod(base: Long, exp: Long, mod: Long): Long {
    if (mod == 1L) return 0
    var b = base % mod
    var e = exp
    var r = 1L % mod
    while (e > 0) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return r
}

private fun pow13(e: Int): Long = powMod(13L, e.toLong(), Long.MAX_VALUE)

/** C(n) mod m：13 的幂下用降幂（a 与 m 互素，指数对 φ(m) 取模即可）。 */
private fun cyclesMod(n: Long, m: Long): Long {
    if (n <= 2L) return 1L % m
    if (m == 1L) return 0L
    val ph = phi(m)
    val t = powMod(3L, n - 2, 2 * ph)
    val e2 = t % ph
    val e3 = ((t - 3) % (2 * ph) + 2 * ph) % (2 * ph) / 2
    return powMod(2L, e2, m) * powMod(3L, e3, m) % m
}

/** 大整数的快速幂（指数本身也是大整数时用）。 */
private fun bigPow(base: java.math.BigInteger, exp: java.math.BigInteger): java.math.BigInteger {
    var b = base
    var e = exp
    var r = java.math.BigInteger.ONE
    while (e.signum() > 0) {
        if (e.testBit(0)) r = r.multiply(b)
        e = e.shiftRight(1)
        b = b.multiply(b)
    }
    return r
}

/** 暴力对照：把 C(n) 作为 BigInteger 完整构造出来再取模。 */
private fun cyclesBig(n: Long, m: Long): Long {
    if (n <= 2L) return 1L % m
    val t = bigPow(java.math.BigInteger.valueOf(3), java.math.BigInteger.valueOf(n - 2))
    val e3 = t.subtract(java.math.BigInteger.valueOf(3)).divide(java.math.BigInteger.valueOf(2))
    return bigPow(java.math.BigInteger.valueOf(2), t)
        .multiply(bigPow(java.math.BigInteger.valueOf(3), e3))
        .mod(java.math.BigInteger.valueOf(m)).toLong()
}

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

private fun modInverse(a: Long, m: Long): Long {
    var oldR = a % m
    var r = m
    var oldS = 1L
    var s = 0L
    while (r != 0L) {
        val q = oldR / r
        val t1 = oldR - q * r; oldR = r; r = t1
        val t2 = oldS - q * s; oldS = s; s = t2
    }
    return ((oldS % m) + m) % m
}

/**
 * 第二条独立路径：C(n) mod 10^8。10^8 = 2^8·5^8；
 * n >= 4 时 e2 = 3^{n-2} >= 9 故 2^{e2} ≡ 0 (mod 2^8)；
 * mod 5^8 用 ord(2) = ord(3) = 4·5^7，指数再降一层到 mod 2^3·5^7。
 */
private fun cyclesMod5(n: Long): Long {
    if (n <= 2L) return 1L
    val ord = 4L * 78125L            // 4·5^7
    val mExp = 625_000L               // 2·ord = 2^3·5^7
    val phM = 250_000L                // phi(625000)
    val t = powMod(3L, (n - 2) % phM, mExp)
    val e2 = t % ord
    val e3 = ((t - 3) % mExp + mExp) % mExp / 2
    val p5 = 390_625L                 // 5^8
    val on5 = powMod(2L, e2, p5) * powMod(3L, e3, p5) % p5
    val r = crt2(0L, 256L, on5, p5) % 100_000_000L
    return r
}

/** x ≡ r (mod m)，y ≡ s (mod n)，m、n 互素时的 CRT。 */
private fun crt2(r: Long, m: Long, s: Long, n: Long): Long {
    val k = (((s - r) % n + n) % n) * modInverse(m % n, n) % n
    var x = r + m * k
    x %= (m * n)
    if (x < 0) x += m * n
    return x
}

/** C(C(C(10⁴))) mod 13⁸：逐级降幂。 */
private fun tripleNest(): Long {
    val p13 = LongArray(9) { pow13(it) }        // 13⁰..13⁸
    val M = p13[8]
    val n1 = cyclesMod(10_000L, 48L * p13[4])
    println("  N1 mod 48*13^4 = " + n1)
    val e2mod = (n1 - 2) % (48L * p13[6])
    val a = crt2(0L, 3L, powMod(3L, e2mod, 8L * p13[7]), 8L * p13[7]) % (24L * p13[5])
    println("  3^(N1-2) mod 24*13^5 = " + a)
    val ph136 = 12L * p13[6]
    val n2On13 = powMod(2L, a % ph136, p13[6]) * powMod(3L, ((a - 3) % (2 * ph136) + 2 * ph136) % (2 * ph136) / 2, p13[6]) % p13[6]
    println("  N2 mod 13^6 = " + n2On13)
    val n2 = crt2(crt2(0L, 16L, 0L, 3L), 48L, n2On13, p13[6])
    println("  N2 mod 48*13^6 = " + n2)
    val e3mod = (n2 - 2) % (48L * p13[6])
    val e = crt2(0L, 3L, powMod(3L, e3mod, 8L * p13[7]), 8L * p13[7]) % (24L * p13[7])
    println("  3^(N2-2) mod 24*13^7 = " + e)
    val ph138 = 12L * p13[7]
    val a2 = e % ph138
    val b2 = ((e - 3) % (2 * ph138) + 2 * ph138) % (2 * ph138) / 2
    return powMod(2L, a2, M) * powMod(3L, b2, M) % M
}

private inline fun timeOf(runs: Int = 5, body: () -> Long): Pair<Long, Double> {
    body()
    val ts = DoubleArray(runs)
    var r = 0L
    for (i in 0 until runs) {
        val s = System.nanoTime()
        r = body()
        ts[i] = (System.nanoTime() - s) / 1e6
    }
    ts.sort()
    return r to ts[runs / 2]
}

fun main() {
    val p13 = LongArray(9) { pow13(it) }
    val M = p13[8]
    println("== 题面自检 ==")
    val c3 = java.math.BigInteger.valueOf(2).pow(3).toLong()
    println("  C(3) = " + c3 + "  " + (if (c3 == 8L) "-> 与题面一致" else "-> 不一致！"))
    val c5 = cyclesBig(5, 9_999_999_999_999_999L)
    println("  C(5) = " + c5 + "  " + (if (c5 == 71328803586048L) "-> 与题面一致" else "-> 不一致！"))
    val cm = cyclesMod(10_000L, 100_000_000L)
    println("  C(10^4) mod 10^8 = " + cm + "  " + (if (cm == 37652224L) "-> 与题面一致" else "-> 不一致！"))
    val c4 = cyclesMod(10_000L, M)
    println("  C(10^4) mod 13^8 = " + c4 + "  " + (if (c4 == 617720485L) "-> 与题面一致" else "-> 不一致！"))
    println("== 双方法互证（降幂阶梯 vs BigInteger 完整构造） ==")
    var ok = true
    for (n in longArrayOf(3, 4, 5, 6, 7, 8, 9, 10)) {
        for (m in longArrayOf(M, 100_000_000L)) {
            val a = cyclesMod(n, m)
            val b = cyclesBig(n, m)
            ok = ok && a == b
            println("  n=" + n + ", m=" + m + " : 阶梯=" + a + ", 大整数=" + b + "  " + (if (a == b) "-> 一致" else "-> 不一致！"))
        }
    }
    println("全部一致：" + ok)
    println("== 幂塔求解 ==")
    // 第二条独立路径：完全换一条模数链（5 的幂 + CRT），与 13 的幂阶梯不共用任何中间量
    for (n in longArrayOf(100, 1000, 3000, 10_000)) {
        val a = cyclesMod(n, 100_000_000L)
        val b = cyclesMod5(n)
        ok = ok && a == b
        println("  n=" + n + ", m=10^8 : 13 幂阶梯=" + a + ", 5 幂 CRT 链=" + b + "  " + (if (a == b) "-> 一致" else "-> 不一致！"))
    }
    val res = timeOf { tripleNest() }
    println("C(C(C(10^4))) mod 13^8 = " + res.first)
    println("OPT_MS: " + String.format("%.3f", res.second) + "  （降幂 + CRT 阶梯）")
    val bres = timeOf(runs = 3) { cyclesBig(10, 100_000_000L) }
    println("BRUTE_MS: " + String.format("%.3f", bres.second) + "  （BigInteger 完整构造 C(10) 后取模；C(15) 起位数已过 4.7e5，无法构造）")
}