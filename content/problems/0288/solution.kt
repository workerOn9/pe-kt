#!/usr/bin/env kotlin
/**
 * Project Euler 288 — An Enormous Factorial（巨大的阶乘）
 *
 * 题目：给定素数 p，用平方型递推随机数发生器
 *     S₀ = 290797，S_{n+1} = S_n² mod 50515093，T_n = S_n mod p
 * 定义 N(p, q) = Σ_{n=0}^{q} T_n · pⁿ，Nfac(p, q) = N(p, q)!，
 * 令 NF(p, q) 为 Nfac(p, q) 中因子 p 的个数。
 * 已知 NF(3, 10000) mod 3^20 = 624955285，求 NF(61, 10^7) mod 61^10。
 *
 * 思路
 * ────
 * 1) 勒让德公式把「数因子个数」变成「求和」：N! 中素数 p 的指数为
 *        NF(p, q) = Σ_{k≥1} ⌊N / pᵏ⌋ = (N − s_p(N)) / (p − 1)，
 *    其中 s_p(N) 是 N 的 p 进制数位和（第二个等号即数位和恒等式）。
 *    本题的 N 在 p 进制下恰好写成 T_q T_{q−1} … T_0：每个 T_n = S_n mod p 都落在 [0, p)，
 *    各位无进位，所以 s_p(N) = Σ_{n=0}^{q} T_n，一遍 O(q) 的随机数递推就能数完。
 * 2) 要算的是 N mod p^10、s_p(N) mod p^10，再乘 (p − 1)⁻¹ mod p^10：
 *        NF(p, q) ≡ (N − s_p(N)) · (p − 1)⁻¹   (mod p^10)。
 *    p − 1 = 60 与 61^10 互素，逆元存在，而且有闭式
 *        (p − 1)⁻¹ ≡ −(1 + p + ⋯ + p^{k−1})   (mod p^k)，
 *    因为 p − 1 = −(1 − p)，而 (1 − p)(1 + p + ⋯ + p^{k−1}) = 1 − p^k ≡ 1。
 * 3) N 有 10^7 + 1 位 p 进制数字，不能整存整算；用流式 Horner 从高位到低位
 *        N ← N · p + T_n   (mod p^10)
 *    只保留一个模余状态。注意 61^10 ≈ 7.13×10^17 < 2^60，但再乘 61 就 ≈ 4.35×10^19，
 *    超出 Long 上限 9.22×10^18：这一步乘法必须走 128 位——把 a·p 写成 hi·2^64 + lo
 *    （Math.multiplyHigh 取高位、普通 Long 乘法取低 64 位），而 2^64 mod m 是常数，
 *    于是 (a·p) mod m = (hi·(2^64 mod m) + lo) mod m 全程不溢出。
 * 4) 数字必须先由低位生成、再按高位到低位喂给 Horner，所以把 10^7 + 1 个数字整体存进
 *    ByteArray（T_n < 61 < 128，一字节一项，约 10 MB），同一遍里不做别的；
 *    数位和与模余各自只需要再扫一遍这个数组。
 *
 * 独立复核（不同数学结构的路径，check() 与 brute-force.kt 各覆盖一部分）
 * ────────────────────────────────────────────────────────────────
 *   路径 B（低位数取模，只看 10 位数字）：m = p^10 恰是 p 的幂，n ≥ 10 的项 T_n·p^n 整块被
 *      模数整除，所以 N mod p^10 = Σ_{n=0}^{9} T_n·pⁿ 只由最低 10 位决定；这个和在 Long 内
 *      精确（< p^10），不涉及任何模乘技巧。它与路径 A 的 10^7 步 Horner 必须逐位一致——
 *      同时也就校验了 128 位乘模的实现。
 *   路径 C（定义级，BigInteger）：小 q 上精确算出 N，再按定义用 Σ⌊N/pᵏ⌋ 逐次除法数出因子
 *      个数，取模后与 A/B 对拍（solution.kt 内 q = 100/500/2000，brute-force.kt 里扩到
 *      q = 20000 以及 p = 3 的题面校验规模），并在同一个 N 上核对数位和恒等式。
 *
 * 复杂度
 * ──────
 * 时间 O(q)：一遍 O(q) 随机数递推 + 一遍 O(q) 乘模 Horner；额外空间 O(q) 字节（约 10 MB 的
 * 数字缓存；也可以改成两遍递推把额外空间压到 O(1)，代价是多跑一遍随机数发生器）。q = 10^7、
 * p = 61 时本机实测约 0.17 s（3 轮最优 165.9 ms，其中递推 + 数位和约 55 ms、Horner 约 110 ms）。
 *
 * 答案：NF(61, 10^7) mod 61^10 = 605857431263981935（本机实跑输出，见 main 末尾打印）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0288/solution.kt -d /tmp/kc-0288
 * java -Xmx4g -cp /tmp/kc-0288:<kotlin-stdlib>:<kotlinx-coroutines-core-jvm> SolutionKt
 */

import java.math.BigInteger

// ────────────────────────────────── 生成器 ──────────────────────────────────

private const val SEED = 290_797L
private const val RNG_MOD = 50_515_093L

/** 生成 T₀…T_q（T_n = S_n mod p < 128，一字节一项）。 */
private fun digitsOf(p: Int, q: Int): ByteArray {
    val digits = ByteArray(q + 1)
    var s = SEED
    for (n in 0..q) {
        digits[n] = (s % p).toByte()
        s = s * s % RNG_MOD
    }
    return digits
}

/** 数位和 s_p(N) = Σ T_n（每个 T_n < p，各位无进位）。 */
private fun digitSumOf(digits: ByteArray): Long {
    var sum = 0L
    for (d in digits) sum += d.toInt()
    return sum
}

private fun powLong(base: Long, exp: Int): Long {
    var r = 1L
    repeat(exp) { r *= base }
    return r
}

// ─────────────────────────────── 溢出安全的模运算 ───────────────────────────────

/** (a + b) mod m，要求 a、b ∈ [0, m) 且 2m 不溢出。 */
private fun addMod(a: Long, b: Long, m: Long): Long = if (a >= m - b) a - (m - b) else a + b

/** 2^64 mod m（m > 2^32 时逐次翻倍路径安全；m 为 61^10、3^20 均满足）。 */
private fun twoPow64Mod(m: Long): Long {
    var x = 1L
    repeat(64) { x = addMod(x, x, m) }
    return x
}

/**
 * (a · p) mod m，允许 a·p 超出 Long 范围（要求 a ∈ [0, m)、p ≤ 61、m < 2^60）：
 * a·p < 61^11 ≈ 4.35×10^19 超 Long，拆成 a·p = hi·2^64 + lo（Math.multiplyHigh 取 hi、
 * 普通 Long 乘法取低 64 位），而 a·p ≡ hi·(2^64 mod m) + lo (mod m)；
 * 此时 hi ≤ 2，hi 项只需条件减法，热路径里只剩一个除法。
 */
private fun mulModByP(a: Long, p: Long, m: Long, twoPow64Mod: Long): Long {
    val hi = Math.multiplyHigh(a, p)
    val lo = a * p
    var lowMod = lo % m
    if (lowMod < 0) lowMod += m                        // lo 的带符号余数归一到 [0, m)
    if (lo < 0) lowMod = addMod(lowMod, twoPow64Mod, m) // 按无符号看，lo + 2^64
    if (hi == 0L) return lowMod
    var hiMod = hi * twoPow64Mod                       // hi ≤ 2 ⇒ hiMod < 3m，两次条件减法足够
    if (hiMod >= m) hiMod -= m
    if (hiMod >= m) hiMod -= m
    return addMod(lowMod, hiMod, m)
}

/** 通用 (a · b) mod m：二进制倍增，只在收尾乘逆元时调用一次（b 可达 m 量级）。 */
private fun mulModBinary(a: Long, b: Long, m: Long): Long {
    var result = 0L
    var base = a % m
    var k = b
    while (k > 0L) {
        if (k and 1L == 1L) result = addMod(result, base, m)
        base = addMod(base, base, m)
        k = k shr 1
    }
    return result
}

/**
 * (p − 1)⁻¹ mod p^k 的闭式：p − 1 = −(1 − p)，而 (1 − p)(1 + p + ⋯ + p^{k−1}) = 1 − p^k ≡ 1
 * (mod p^k)，故 (p − 1)⁻¹ ≡ −(1 + p + ⋯ + p^{k−1})。几何和约为 p^k/(p − 1)，Long 内精确。
 */
private fun inverseOfPm1(p: Long, mod: Long): Long {
    var geom = 1L                     // 1 + p + p² + ⋯ + p^{k−1}
    var w = 1L
    while (w * p < mod) {
        w *= p
        geom += w
    }
    return mod - geom
}

// ─────────────────────────────── 三条独立路径 ───────────────────────────────

/** 路径 A 的模运算核心：由数字表从最高位流式 Horner 求 N mod m。 */
private fun nModStreaming(digits: ByteArray, p: Int, mod: Long): Long {
    val twoPow64 = twoPow64Mod(mod)
    var acc = 0L
    for (n in digits.size - 1 downTo 0) {
        acc = addMod(mulModByP(acc, p.toLong(), mod, twoPow64), digits[n].toLong(), mod)
    }
    return acc
}

/** 路径 B 的模运算核心：N mod p^k = Σ_{n<k} T_n·pⁿ（n ≥ k 的项被 p^k 整除），Long 内精确。 */
private fun nModLowDigits(digits: ByteArray, p: Int, k: Int): Long {
    var acc = 0L
    var w = 1L
    for (n in 0 until k) {
        acc += digits[n].toLong() * w
        w *= p
    }
    return acc
}

/** 路径 A：流式 Horner（128 位乘模）算 N mod p^k，再乘 (p−1)⁻¹。 */
private fun nfModStreaming(p: Int, q: Int, mod: Long): Long {
    val digits = digitsOf(p, q)
    val diff = Math.floorMod(nModStreaming(digits, p, mod) - digitSumOf(digits) % mod, mod)
    return mulModBinary(diff, inverseOfPm1(p.toLong(), mod), mod)
}

/** 路径 B：只取最低 k 位数字直接精确求和（要求 mod = p^k）。 */
private fun nfModLowDigits(p: Int, q: Int, mod: Long, k: Int): Long {
    require(powLong(p.toLong(), k) == mod) { "路径 B 要求 mod = p^k" }
    val digits = digitsOf(p, q)
    val diff = Math.floorMod(nModLowDigits(digits, p, k) - digitSumOf(digits) % mod, mod)
    return mulModBinary(diff, inverseOfPm1(p.toLong(), mod), mod)
}

/** 路径 C：定义级精确对照（小 q）。BigInteger 精确算 N，再按定义逐次除法数因子个数。 */
private fun nfModExact(p: Int, q: Int, mod: Long): Long {
    val digits = digitsOf(p, q)
    val bp = BigInteger.valueOf(p.toLong())
    var n = BigInteger.ZERO
    for (i in q downTo 0) n = n.multiply(bp).add(BigInteger.valueOf(digits[i].toLong()))
    var v = BigInteger.ZERO                    // v = Σ_{k≥1} ⌊N / pᵏ⌋：逐次除以 p，不经过数位和
    var x = n
    while (x.signum() > 0) {
        x = x.divide(bp)
        v = v.add(x)
    }
    val num = n.subtract(BigInteger.valueOf(digitSumOf(digits)))
    val pm1 = bp.subtract(BigInteger.ONE)
    check(num.mod(pm1).signum() == 0) { "N − s_p(N) 不被 p − 1 整除" }
    check(v == num.divide(pm1)) { "勒让德逐次除法与数位和恒等式不一致" }
    return v.mod(BigInteger.valueOf(mod)).toLong()
}

// ─────────────────────────────── 自检与计时 ───────────────────────────────

/** 中小规模对照表：与 brute-force.kt 的定义级输出逐行对拍。 */
private fun smallCaseTable(mod61: Long, mod3: Long) {
    println("中小规模对照表（路径 A / B，brute-force.kt 用路径 C 复现同一批值）：")
    for ((p, q) in listOf(61 to 1000, 61 to 2000, 61 to 5000, 61 to 10000, 61 to 20000,
                          3 to 10000, 3 to 30000)) {
        val mod = if (p == 61) mod61 else mod3
        val k = if (p == 61) 10 else 20
        val a = nfModStreaming(p, q, mod)
        val b = nfModLowDigits(p, q, mod, k)
        check(a == b) { "($p, $q) 路径 A=$a 与 B=$b 不一致" }
        println("  (p=$p, q=$q): NF mod $p^$k = $a")
    }
}

private fun checkPreliminaries(mod3: Long, mod61: Long) {
    // 0. 逆元闭式与二进制乘模互检
    check(mulModBinary(inverseOfPm1(3L, mod3), 2L, mod3) == 1L)
    check(mulModBinary(inverseOfPm1(61L, mod61), 60L, mod61) == 1L)

    // 1. 题面给定校验：NF(3, 10000) mod 3^20 = 624955285，两条路径都必须复现
    val given = 624_955_285L
    val a3 = nfModStreaming(3, 10_000, mod3)
    val b3 = nfModLowDigits(3, 10_000, mod3, 20)
    check(a3 == given) { "NF(3,10000) mod 3^20 = $a3，与题面给定的 $given 不符" }
    check(b3 == given) { "路径 B 给出 $b3，与题面给定的 $given 不符" }
    val digits3 = digitsOf(3, 10_000)
    println("题面给定校验：NF(3, 10000) mod 3^20 = $a3（路径 A/B 一致，题面给定 $given）")
    println("  (3, 10000) 中间量：s_3(N) = ${digitSumOf(digits3)}，N mod 3^20 = ${nModStreaming(digits3, 3, mod3)}")

    // 2. 小规模定义级对照（BigInteger 精确 N + 逐次除法数因子），三个值必须一致
    for (q in intArrayOf(100, 500, 2000)) {
        val c = nfModExact(61, q, mod61)
        val a = nfModStreaming(61, q, mod61)
        val b = nfModLowDigits(61, q, mod61, 10)
        check(c == a && c == b) { "(61, $q) 定义级 $c ≠ A $a 或 B $b" }
    }
    println("小规模定义级对照（BigInteger 精确 N + Σ⌊N/pᵏ⌋ 数因子）：")
    println("  q = 100/500/2000 时，路径 C = 路径 A = 路径 B")

    println()
    smallCaseTable(mod61, mod3)
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

// ─────────────────────────────────── main ───────────────────────────────────

fun main() {
    val mod3 = powLong(3L, 20)
    val mod61 = powLong(61L, 10)

    checkPreliminaries(mod3, mod61)

    // ---------- 主问题 ----------
    val q = 10_000_000
    println()
    println("主问题：NF(61, 10^7) mod 61^10，61^10 = $mod61，需要 10^7 + 1 位 p 进制数字")
    val digitsMain = digitsOf(61, q)
    val sMain = digitSumOf(digitsMain)
    println("  RNG 一遍：s_61(N) = Σ T_n = $sMain；N mod 61^10 = " +
        "${nModStreaming(digitsMain, 61, mod61)}（= 低 10 位之和 " +
        "${nModLowDigits(digitsMain, 61, 10)}）")

    val lowT0 = System.nanoTime()
    val answer = nfModLowDigits(61, q, mod61, 10)
    val lowMs = (System.nanoTime() - lowT0) / 1e6
    println("路径 B（只看最低 10 位，精确求和）：$answer（${"%.3f".format(lowMs)} ms）")

    val msA = bestOf3("路径 A（流式 Horner + 128 位乘模）", answer) { nfModStreaming(61, q, mod61) }

    // ---------- 输出 ----------
    println()
    println("答案 = $answer")
    println("汇总：路径 A ${"%.3f".format(msA)} ms；路径 B ${"%.3f".format(lowMs)} ms（本机 JIT 预热后实测）")
    println("check() 全部通过")
}
