#!/usr/bin/env kotlin
/**
 * Project Euler 282 — The Ackermann Function（阿克曼函数）
 *
 * 题目：A(m, n) 由三重递归定义
 *     A(0, n) = n + 1；  A(m, 0) = A(m−1, 1)（m > 0）；  A(m, n) = A(m−1, A(m, n−1))（m, n > 0）。
 * 求 Σ_{n=0..6} A(n, n) mod 14^8，其中 14^8 = 2^8 · 7^8 = 1475789056。
 *
 * 关键恒等式（超运算 / 高德纳箭头）
 * ────────────────────────────────
 * 记 a ↑^k b 为高德纳箭头（↑^0 = 乘法、↑^1 = 幂、↑^2 = 四则塔 tetration、↑^3 = pentation …），
 * 则对 m ≥ 2 有
 *     A(m, n) = 2 ↑^{m−2} (n + 3) − 3 .
 * 逐级验证：m=2 时 A(2,n) = 2n+3 = 2·(n+3) − 3；m=3 时 A(3,n) = 2^{n+3} − 3（题面 A(3,4)=125
 * 即 2^7 − 3）；一般地把「外层多套一层 2 的幂塔」逐层展开即得。于是本题只需要三类巨大的塔：
 *     A(4,4) = 2↑↑7 − 3，   A(5,5) = 2↑↑↑8 − 3，   A(6,6) = 2↑↑↑↑9 − 3，
 * 前四个值 A(0,0)=1、A(1,1)=3、A(2,2)=7、A(3,3)=61 直接由定义递归算出。
 *
 * 主路径 A：φ 链 + 广义欧拉定理（直接模 14^8）
 * ──────────────────────────────────────
 * 目标是 2↑↑h mod m。当指数 E 精确已知（h ≤ 5 时 E = 2↑↑(h−1) ∈ {1, 2, 4, 16, 65536}）直接快速幂；
 * 否则 E = 2↑↑(h−1) ≥ 2↑↑5 = 2^65536 ≫ log2 m，可以用广义欧拉定理
 *     2^E ≡ 2^{E mod φ(m) + φ(m)}  (mod m)
 * （条件是 E ≥ log2 m，与 2、m 是否互素无关），于是递归到 φ(m) 上：tower(h, m) 依赖
 * tower(h−1, φ(m))，模数沿 φ 链下降（长度 ≤ 30 左右），递归深度由模数而不是 h 控制。
 *
 * 主路径 B：显式拆 2 部分 + CRT（复核）
 * ──────────────────────────────────
 * 把 m 写成 2^s · q（q 奇）。h ≥ 6 时 2↑↑h = 2^{2↑↑(h−1)} 被 2^65536 整除 ⇒ 在 2^s 上是 0；
 * 奇数部分 gcd(2, q) = 1，欧拉定理直接给 2↑↑h ≡ 2^{2↑↑(h−1) mod φ(q)} (mod q)，最后 CRT 合并。
 * 这条路径完全不用「+φ 修正」，也不再对 2 的幂次做任何近似。
 *
 * 精度 / 边界的三条防线
 * ────────────────────
 * 1. 小高度直接精确：h ≤ 5 时指数精确，无任何取模近似；
 * 2. 「+φ」只在真实指数 ≥ 2^65536 时使用（h ≥ 6），远超任何模数需要的 log2 m（< 31）；
 * 3. 塔高足够后模值稳定：tower(h, m) 对 h ≥ h0(m)（本机实测 h0 ≤ 6）恒定，而 A(5,5)、A(6,6)
 *    对应的塔高是 2↑↑65536 量级甚至更高，故用 BIG_HEIGHT = 10^6 代表「任意够大的高度」——
 *    程序里显式验证 tower(h0..60, m) 与 tower(BIG_HEIGHT, m)、tower(BIG_HEIGHT+1, m) 全部相等。
 *    （稳定性可以沿 φ 链归纳证明：tower(·, m) 的常值段由 tower(·, φ(m)) 的常值段 +1 得到。）
 *
 * 暴力 / 独立对照（见 brute-force.kt）
 * ─────────────────────────────────
 * 1. 定义直译递归（BigInteger + 记忆化）复现题面三例 A(1,0)=2、A(2,2)=7、A(3,4)=125，
 *    并给出 A(4,0)=13、A(4,1)=65533；
 * 2. 字面指数路径：把 2↑↑5 = 2^65536 精确物化成 BigInteger（19729 位），用 BigInteger.modPow
 *    直接算 2↑↑6 mod m = 2^(2^65536) mod m（指数是真 19729 位数，不做任何欧拉降幂）；
 *    2↑↑7 mod 奇模数再用一次欧拉降幂（指数仍是字面 BigInteger）；
 * 3. 塔实现 A / B / 字面 BigInteger 三路在 φ 链所有模数 × h = 1..8 上逐个相等。
 *
 * 复杂度
 * ──────
 * 每次 tower 调用只沿 φ 链下降（≤ ~30 层），每层一次 32 位快速幂（O(log m) 次乘法）+ 一次
 * 试除法 φ（O(√m)）；整体是常数级计算（< 1 ms），远低于引擎 10 s 熔断线。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0282/solution.kt -d /tmp/kc-0282
 * java -cp /tmp/kc-0282:<kotlin-stdlib> SolutionKt
 */

import java.math.BigInteger

private val MOD = 1475789056L               // 14^8 = 2^8 · 7^8
private val MOD_2 = 256L                    // 2^8
private val MOD_7 = 5764801L                // 7^8

/** 「任意足够大的塔高」哨兵：A(5,5)、A(6,6) 的真塔高均 ≥ 2↑↑65536 ≫ 10^6。 */
private const val BIG_HEIGHT = 1_000_000L

// ─────────────────────────── 数论小工具 ───────────────────────────

/** 欧拉函数 φ(m)：试除法，只用在小模数（≤ 14^8）上。 */
private fun eulerPhi(m0: Long): Long {
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

/** 快速幂 base^exp mod mod（模数 ≤ 1.5×10^9，中间乘积 < 2^63，无溢出）。 */
private fun modPowL(base: Long, exp: Long, mod: Long): Long {
    if (mod == 1L) return 0L
    var b = base % mod
    var e = exp
    var r = 1L
    while (e > 0L) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e ushr 1
    }
    return r
}

/** 模逆元（扩展欧几里得），要求 gcd(a, m) = 1。 */
private fun modInverseL(a: Long, m: Long): Long {
    var oldR = m
    var r = Math.floorMod(a, m)
    var oldS = 0L
    var s = 1L
    while (r != 0L) {
        val q = oldR / r
        val t1 = oldR - q * r; oldR = r; r = t1
        val t2 = oldS - q * s; oldS = s; s = t2
    }
    check(oldR == 1L) { "模逆元不存在：gcd($a, $m) = $oldR" }
    return Math.floorMod(oldS, m)
}

/** 中国剩余定理：x ≡ a (mod p)、x ≡ b (mod q)，gcd(p, q) = 1，返回 [0, p·q) 中的解。 */
private fun crtTwo(a: Long, p: Long, b: Long, q: Long): Long {
    if (p == 1L) return Math.floorMod(b, q)
    if (q == 1L) return Math.floorMod(a, p)
    val inv = modInverseL(p % q, q)
    val t = Math.floorMod(b - a, q) * inv % q
    return a + p * t
}

// ─────────────────────────── 2 的幂塔模计算 ───────────────────────────

/** 2↑↑h（h ≤ 5）对应的精确指数 2↑↑(h−1)。 */
private fun smallExponent(h: Long): Long = when (h) {
    1L -> 1L
    2L -> 2L
    3L -> 4L
    4L -> 16L
    else -> 65536L
}

/**
 * 2↑↑h mod m —— 主路径 A：广义欧拉定理 + φ 链。
 *   h ≤ 5：指数精确（2↑↑(h−1) ≤ 65536），快速幂即可；
 *   h ≥ 6：真实指数 2↑↑(h−1) ≥ 2↑↑5 = 2^65536 ≥ log2 m，用
 *          2^E ≡ 2^(E mod φ(m) + φ(m)) (mod m)（对任意底数/模数成立，只需 E ≥ log2 m）。
 */
private fun towerModA(h: Long, m: Long): Long {
    if (m == 1L) return 0L
    if (h <= 5L) return modPowL(2L, smallExponent(h), m)
    val phi = eulerPhi(m)
    val e = towerModA(h - 1L, phi)                 // = 2↑↑(h−1) mod φ(m)
    return modPowL(2L, e + phi, m)
}

/**
 * 2↑↑h mod m —— 主路径 B（复核）：m = 2^s · q 拆开，CRT 合并。
 *   2 部分：h ≥ 6 ⇒ 指数 2↑↑(h−1) ≥ 2^65536 ≥ s，故 2↑↑h ≡ 0 (mod 2^s)；
 *   奇数部分：gcd(2, q) = 1，欧拉定理直接降指数，无修正项。
 */
private fun towerModB(h: Long, m: Long): Long {
    if (m == 1L) return 0L
    if (h <= 5L) return modPowL(2L, smallExponent(h), m)
    var s = 0
    var q = m
    while (q and 1L == 0L) { q = q shr 1; s++ }
    val partQ = if (q == 1L) 0L else modPowL(2L, towerModB(h - 1L, eulerPhi(q)), q)
    val part2 = 0L
    return crtTwo(part2, 1L shl s, partQ, q)
}

/**
 * Σ 的第 n 项 A(n,n) mod m。
 *   n ≤ 3 直接查表（1、3、7、61）；n = 4 用 A(4,4) = 2↑↑7 − 3；
 *   n = 5 ⟹ A(5,5) = 2↑↑↑8 − 3 = 2↑↑(2↑↑↑7) − 3，塔高 ≥ 2↑↑65536；
 *   n = 6 ⟹ A(6,6) = 2↑↑↑↑9 − 3，塔高更高——两者都超过稳定阈，用 BIG_HEIGHT 代表。
 */
private fun ackermannNNMod(n: Int, m: Long, tower: (Long, Long) -> Long): Long = when (n) {
    0 -> 1L % m
    1 -> 3L % m
    2 -> 7L % m
    3 -> 61L % m
    4 -> Math.floorMod(tower(7L, m) - 3L, m)
    5 -> Math.floorMod(tower(BIG_HEIGHT, m) - 3L, m)
    6 -> Math.floorMod(tower(BIG_HEIGHT, m) - 3L, m)
    else -> error("n = $n 不在 0..6 内")
}

/** Σ_{n=0..6} A(n,n) mod m。 */
private fun sumAckermannMod(m: Long, tower: (Long, Long) -> Long): Long {
    var s = 0L
    for (n in 0..6) s = (s + ackermannNNMod(n, m, tower)) % m
    return s
}

// ─────────────────────────── 独立对照：定义直译 / 字面 BigInteger ───────────────────────────

/** 在指定栈大小的线程里跑 block：A(4,1) 这类定义直译递归的嵌套深度可达数万层。 */
private fun <T> withBigStack(block: () -> T): T {
    var result: T? = null
    var failure: Throwable? = null
    val t = Thread(null, {
        try {
            result = block()
        } catch (e: Throwable) {
            failure = e
        }
    }, "big-stack", 256L * 1024 * 1024)
    t.start()
    t.join()
    failure?.let { throw it }
    @Suppress("UNCHECKED_CAST")
    return result as T
}

/** 阿克曼函数定义直译（BigInteger + 记忆化），只用于小参数（递归深、栈需求大）。 */
private fun ackermannNaive(m: Int, n: Long): BigInteger = withBigStack {
    val memo = HashMap<Long, BigInteger>()
    fun go(am: Int, an: Long): BigInteger {
        val key = am * 1_000_000L + an
        memo[key]?.let { return it }
        val v = when {
            am == 0 -> BigInteger.valueOf(an + 1L)
            an == 0L -> go(am - 1, 1L)
            else -> go(am - 1, go(am, an - 1L).toLong())
        }
        memo[key] = v
        return v
    }
    go(m, n)
}

/**
 * 2↑↑h mod m —— 字面指数路径：把能物化的指数做成真 BigInteger 用 BigInteger.modPow 计算
 * （h = 6 的指数是 2↑↑5 = 2^65536，共 19729 位十进制，完全不降幂，是最「硬」的对照）；
 * h ≥ 7 且指数不可物化时才做一次欧拉降幂 / 偶模数按 2 部分 = 0 拆分。
 */
private fun towerLiteral(h: Int, m: Long): Long {
    fun rec(k: Int, mod: Long): Long {
        if (mod == 1L) return 0L
        if (k <= 6) {
            val e: BigInteger = when (k) {
                1 -> BigInteger.ONE
                2 -> BigInteger.TWO
                3 -> BigInteger.valueOf(4L)
                4 -> BigInteger.valueOf(16L)
                5 -> BigInteger.valueOf(65536L)
                else -> BigInteger.TWO.pow(65536)               // 2↑↑5，19729 位
            }
            return BigInteger.TWO.modPow(e, BigInteger.valueOf(mod)).toLong()
        }
        var s = 0
        var q = mod
        while (q and 1L == 0L) { q = q shr 1; s++ }
        val partQ = if (q == 1L) 0L else modPowL(2L, rec(k - 1, eulerPhi(q)), q)
        return crtTwo(0L, 1L shl s, partQ, q)
    }
    return rec(h, m)
}

// ─────────────────────────── 计时与输出 ───────────────────────────

private fun bestOf3(tag: String, expected: Long, reps: Int = 1, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        var out = 0L
        val t0 = System.nanoTime()
        repeat(reps) { out = f() }
        val ms = (System.nanoTime() - t0) / 1e6 / reps
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.6f".format(best)} ms/次（3 轮最优，每轮 $reps 次重复，JIT 预热后）")
    return best
}

fun main() {
    println("== 1. 题面样例（定义直译递归，BigInteger 精确）==")
    val samples = listOf(
        Triple(1, 0L, BigInteger.valueOf(2L)),
        Triple(2, 2L, BigInteger.valueOf(7L)),
        Triple(3, 4L, BigInteger.valueOf(125L)),
    )
    for ((m, n, want) in samples) {
        val got = ackermannNaive(m, n)
        check(got == want) { "题面样例 A($m,$n) = $want，实算 $got" }
        println("A($m, $n) = $got（题面给 $want）")
    }
    val a40 = ackermannNaive(4, 0)
    val a41 = ackermannNaive(4, 1)
    check(a40 == BigInteger.valueOf(13L) && a41 == BigInteger.valueOf(65533L)) { "A(4,0)/A(4,1) 异常" }
    println("A(4, 0) = $a40，A(4, 1) = $a41（= 2^16 − 3 = 2↑↑4 − 3，与恒等式一致）")

    println()
    println("== 2. 恒等式 A(m,n) = 2 ↑^{m−2} (n+3) − 3 的小参数对拍 ==")
    for (m in 2..3) {
        for (n in 0L..4L) {
            val viaDef = ackermannNaive(m, n)
            val viaHyper = when (m) {
                2 -> BigInteger.valueOf(2L * (n + 3L) - 3L)
                else -> BigInteger.TWO.pow((n + 3L).toInt()).subtract(BigInteger.valueOf(3L))
            }
            check(viaDef == viaHyper) { "恒等式不符：A($m,$n) 定义 $viaDef ≠ 恒等式 $viaHyper" }
        }
        println("A($m, n)（n = 0..4）定义直译 ≡ 2 ↑^{${m - 2}} (n+3) − 3 ✓")
    }
    // A(4,2) = A(3, A(4,1)) = 2^{65533+3} − 3 = 2^65536 − 3：链上用已验证的 A(3,·) 闭式
    val tower5 = BigInteger.TWO.pow(65536)                     // 物化的 2↑↑5
    check(tower5.bitLength() == 65537) { "2↑↑5 = 2^65536 的位长应为 65537，实测 ${tower5.bitLength()}" }
    val a42 = tower5.subtract(BigInteger.valueOf(3L))
    check(a42.bitLength() == 65536) { "2^65536 − 3 的位长应为 65536，实测 ${a42.bitLength()}" }
    println("A(4, 2) = A(3, A(4,1)) = 2^65536 − 3 = 2↑↑5 − 3 ✓（2↑↑5 物化后位长 ${tower5.bitLength()}）")
    println("恒等式在 m ∈ {2,3,4} 可算范围内全部成立 ⇒ A(4,4) = 2↑↑7 − 3、A(5,5) = 2↑↑↑8 − 3、A(6,6) = 2↑↑↑↑9 − 3")

    println()
    println("== 3. 稳定性：塔高足够后 2↑↑h mod m 恒定（φ 链归纳 + 实算验证）==")
    val chainModuli = ArrayList<Long>()
    run {
        var x = MOD
        while (x > 1L) { chainModuli.add(x); x = eulerPhi(x) }
    }
    for (m in listOf(MOD, MOD_7, MOD_2, eulerPhi(MOD))) {
        var h = 60L
        while (h > 1L && towerModA(h - 1L, m) == towerModA(h, m)) h--
        val v = towerModA(h, m)
        for (k in h..60L) check(towerModA(k, m) == v) { "m = $m 在 h = $k 处不稳定" }
        check(towerModA(BIG_HEIGHT, m) == v && towerModA(BIG_HEIGHT + 1L, m) == v) { "m = $m 的大高度值偏离" }
        check(towerModB(BIG_HEIGHT, m) == v) { "m = $m 两条塔实现不一致" }
        println("m = $m：h ≥ $h 后 2↑↑h mod m 恒为 $v（h ≤ 60 与 h = 10^6、10^6+1 全部相等）")
    }

    println()
    println("== 4. 三条塔实现互证（h = 1..8，φ 链上的所有模数）==")
    var checked = 0
    for (m in chainModuli) {
        for (h in 1..8) {
            val a = towerModA(h.toLong(), m)
            val b = towerModB(h.toLong(), m)
            val c = towerLiteral(h, m)
            check(a == b && b == c) { "塔实现不一致：m = $m, h = $h → A=$a B=$b 字面=$c" }
            checked++
        }
    }
    println("φ 链上 ${chainModuli.size} 个模数 × h = 1..8 共 $checked 组（A / B / 字面 BigInteger）全部相等 ✓")

    println()
    println("== 5. 主路径 A（直接 mod 14^8）与路径 B（CRT：mod 2^8 + mod 7^8）互证 ==")
    val ansA = sumAckermannMod(MOD) { h, m -> towerModA(h, m) }
    val ansB = crtTwo(
        sumAckermannMod(MOD_2) { h, m -> towerModB(h, m) },
        MOD_2,
        sumAckermannMod(MOD_7) { h, m -> towerModB(h, m) },
        MOD_7,
    )
    check(ansA == ansB) { "两条路径不一致：A = $ansA，CRT = $ansB" }
    val terms = (0..6).map { ackermannNNMod(it, MOD) { h, m -> towerModA(h, m) } }
    println("各项 mod 14^8：n=0..6 → ${terms.joinToString(", ")}")
    println("路径 A = $ansA，路径 B（CRT 重组）= $ansB，一致 ✓")
    check(terms[5] == terms[6]) { "A(5,5) 与 A(6,6) 应对 14^8 同余（塔高都远超稳定阈）" }
    println("A(5,5) ≡ A(6,6) (mod 14^8)：两者塔高（≥ 2↑↑65536）都远超 2↑↑h 的稳定阈")

    println()
    println("== 6. 计时 ==")
    val msA = bestOf3("主路径 A（φ 链 + 广义欧拉，直接 mod 14^8）", ansA, reps = 10_000) {
        sumAckermannMod(MOD) { h, m -> towerModA(h, m) }
    }
    val msB = bestOf3("路径 B（拆 2 部分 + CRT 重组）", ansB, reps = 2_000) {
        crtTwo(
            sumAckermannMod(MOD_2) { h, m -> towerModB(h, m) },
            MOD_2,
            sumAckermannMod(MOD_7) { h, m -> towerModB(h, m) },
            MOD_7,
        )
    }
    val litExpected = towerModA(6L, MOD)
    val msL = bestOf3("字面 BigInteger 路径（2↑↑6 mod 14^8，指数为 19729 位真数）", litExpected, reps = 10) {
        towerLiteral(6, MOD)
    }

    println()
    println("== 7. 结果 ==")
    println("Σ_{n=0..6} A(n,n) mod 14^8 = $ansA")
    println("（14^8 = $MOD）")
    println(
        "check() 全部通过；主路径 A ${"%.6f".format(msA)} ms，路径 B ${"%.6f".format(msB)} ms，" +
            "字面路径 ${"%.6f".format(msL)} ms",
    )
}
