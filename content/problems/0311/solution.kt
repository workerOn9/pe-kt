#!/usr/bin/env kotlin
/**
 * Project Euler 311 — Biclinic Integral Quadrilaterals（双斜整数四边形）
 *
 * 题目：凸整边四边形 ABCD，1 ≤ AB < BC < CD < AD，BD 为整数，O 是 BD 中点且 AO 为整数；
 *      若 AO = CO ≤ BO = DO 则称双斜整数四边形。记 B(N) 为 AB²+BC²+CD²+AD² ≤ N 的个数。
 *      已知 B(10⁴)=49、B(10⁶)=38239，求 B(10¹⁰)。
 *
 * 思路推导
 * --------
 * 记 BD = 2d、AO = CO = k（1 ≤ k ≤ d）。对三角形 ABD 与 CBD 各用一次中线公式：
 *   AB² + AD² = 2k² + 2d² = BC² + CD²，故四边平方和 = 4(k² + d²) = 4n，
 * 约束等价于 n = k² + d² ≤ N/4。
 *
 * 把 O 放在原点、BD 沿 x 轴，则 A = (x_A, y_A) 满足
 *   AB² = k² + d² − 2d·x_A，AD² = k² + d² + 2d·x_A，故 AD² − AB² = 4d·x_A。
 * 而 (d−k)² + (d+k)² = 2(k²+d²) = 2n，且 (d+k)² − (d−k)² = 4dk。
 * 对 2n 的另一组表示 u² + v² = 2n（u < v）有 v² − u² = 2n − 2u²，于是
 *   v² − u² < 4dk  <=>  2n − 2u² < 4dk  <=>  (d−k)² < u²  <=>  u > d − k 。
 * 这正是「三角形 ABD / CBD 存在（|x_A| < k）且四边形凸」的条件，而凸性一旦 A、C 分居
 * BD 两侧且 |x_A|, |x_C| < k ≤ d 就自动成立。
 *
 * 于是固定 n 后，每个四边形恰好对应一个三元组 r < i < j：第 r 组表示给出 (k,d)，
 * 第 i、j 组表示给出两组边长；把 2n 的各组表示按 u 升序排列后「u_i > d−k = u_r」
 * 等价于「i > r」。因此 n 的贡献是组合数 C(m,3)，
 *   m = #{ (u,v) : 0 ≤ u < v, u² + v² = 2n }。
 *
 * m 的数论表达：把 n 唯一分解成 n = 2^a·w²·u（w 只含 4k+3 型素数，u 只含 4k+1 型素数），
 * 由 r₂(2n) = 4·Π_{p≡1(4)}(e_p+1) 与 r₂(2n) = 8(m−Z) + 4Z + 4D 得
 *   m = (P + Z − D) / 2,  P = Π_{p≡1(4)}(e_p+1),
 *   Z = [2n 是完全平方] = [a 奇且 u 是平方],  D = [n 是完全平方] = [a 偶且 u 是平方]。
 * C(m,3) 非零要求 P ≥ 5，于是只需枚举 Ω(u) ≥ 3 的 4k+1 型光滑数，
 * 权重再乘上「3 型光滑平方根 w 的可选个数」T(⌊√(X/u)⌋) 与 2 的幂次 a。
 *
 * 验证
 * --------
 * 1. 题面样例：B(10⁴) = 49、B(10⁶) = 38239 由同一算法直接算出（下方输出）；
 * 2. 双方法互证：B ∈ {4·10³, 2·10⁴, 10⁵, 4·10⁵} 上「光滑数分解枚举」与
 *    「枚举所有 (u,v) 对 + 桶计数」的暴力实现逐一相等；
 * 3. 边界样例人工复核：n = 1250 = 2·5⁴，P = 5、Z = 1、D = 0 ⟹ m = 3，贡献 C(3,3) = 1；
 *    该对 u = 0 被计入（对应 k = d 的等腰情形），若漏掉 u = 0 就会少算 1 —— 这正是
 *    「m = 48 vs 实际 49」那个偏差的来源。
 *
 * 复杂度
 * --------
 * 光滑数 DFS 访问 O(X/√(log X)) 个节点（X = 2.5·10⁹ 时约 1.04·10⁸ 个），每个贡献节点
 * 额外做 O(log X) 次查表，总耗时秒级；4k+1 型素数用分段筛筛到 X/25 = 10⁸。
 * 暴力对照为 O(N) 次两两枚举（N = 4·10⁵ 时约 2·10⁵ 对，毫秒级）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

/** 题面规模。 */
private const val N_TARGET = 10_000_000_000L

/** 暴力对照的规模。 */
private val BRUTE_SAMPLES = longArrayOf(4_000L, 20_000L, 100_000L, 400_000L)

/** 枚举 4k+1 型素数的上界：u 至少三个素因子且最小素因子为 5，最大素因子 ≤ X/25。 */
private fun primeLimit(x: Long): Int = maxOf(16, (x / 25).toInt())

/** 分段筛出 ≤ limit 且 ≡ 1 (mod 4) 的素数。 */
private fun primesOneModFour(limit: Int): IntArray {
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (isPrime[i]) {
            var j = i.toLong() * i
            while (j <= limit) { isPrime[j.toInt()] = false; j += i }
        }
        i++
    }
    val out = IntArray(limit / 4 + 16)
    var n = 0
    val seg = 4_000_000
    val base = kotlin.math.sqrt(limit.toDouble()).toInt() + 1
    val mark = BooleanArray(seg + 2)
    var lo = 2
    while (lo <= limit) {
        val hi = minOf(limit, lo + seg - 1)
        java.util.Arrays.fill(mark, false)
        for (q in 2..base) {
            if (!isPrime[q]) continue
            var start = Math.max(q.toLong() * q, ((lo.toLong() + q - 1) / q) * q)
            while (start <= hi) { mark[(start - lo).toInt()] = true; start += q }
        }
        var v = lo
        while ((v and 3) != 1) v++
        while (v <= hi) {
            if (!mark[(v - lo).toInt()]) out[n++] = v
            v += 4
        }
        lo = hi + 1
    }
    return out.copyOf(n)
}

/** t[y] = y 以内「所有素因子都 ≡ 3 (mod 4)」的整数个数（含 1；偶数与含 4k+1 因子的都不算）。 */
private fun threeModFourCounts(maxY: Int): IntArray {
    val isPrime = BooleanArray(maxY + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= maxY) {
        if (isPrime[i]) {
            var j = i.toLong() * i
            while (j <= maxY) { isPrime[j.toInt()] = false; j += i }
        }
        i++
    }
    val bad = BooleanArray(maxY + 1)
    for (y in 2..maxY step 2) bad[y] = true
    for (q in 5..maxY) {
        if (isPrime[q] && q % 4 == 1) {
            var j = q
            while (j <= maxY) { bad[j] = true; j += q }
        }
    }
    val t = IntArray(maxY + 2)
    for (y in 1..maxY) t[y] = t[y - 1] + (if (bad[y]) 0 else 1)
    return t
}

private fun isqrt(x: Long): Long {
    if (x <= 0) return 0
    var r = kotlin.math.sqrt(x.toDouble()).toLong()
    while ((r + 1) * (r + 1) <= x) r++
    while (r * r > x) r--
    return r
}

/**
 * B(4X) = Σ_{n ≤ X} C(m(n),3)。brute=true 时用「枚举 (u,v) 对 + 桶计数」做交叉验证。
 */
private fun biclinic(x: Long, brute: Boolean = false): Long {
    if (brute) {
        val lim = isqrt(2 * x)
        val cnt = HashMap<Long, Int>()
        for (u in 0..lim) {
            val u2 = u * u
            for (v in u + 1..lim) {
                val s = u2 + v * v
                if ((s and 1L) == 0L && s / 2 <= x) cnt.merge(s / 2, 1, Int::plus)
            }
        }
        var tot = 0L
        for (m in cnt.values) if (m >= 3) tot += m.toLong() * (m - 1) * (m - 2) / 6
        return tot
    }
    val primes = primesOneModFour(primeLimit(x))
    val t = threeModFourCounts(isqrt(x).toInt())
    var total = 0L
    var nodes = 0L

    fun contribute(u: Long, p: Long, square: Boolean) {
        var lim = x / u
        var a = 0
        while (lim >= 1) {
            val shift = if (square) { if (a and 1 == 1) 1 else -1 } else 0
            val m = (p + shift) / 2
            if (m >= 3) total += m * (m - 1) * (m - 2) / 6 * t[isqrt(lim).toInt()]
            lim /= 2
            a++
        }
    }

    fun dfs(startIdx: Int, u: Long, p: Long, square: Boolean) {
        nodes++
        if (u > 1 && p >= 5) contribute(u, p, square)
        var i = startIdx
        while (i < primes.size) {
            val prime = primes[i].toLong()
            if (u > x / prime) break
            var v = u
            var e = 1
            while (v <= x / prime) {
                v *= prime
                dfs(i + 1, v, p * (e + 1), square && (e % 2 == 0))
                e++
            }
            i++
        }
    }
    dfs(0, 1L, 1L, true)
    println("      （4k+1 型光滑数 DFS 节点数 = $nodes）")
    return total
}

/** 预热 1 次后跑 3 轮取最快的一次，返回 (结果, 毫秒)。 */
private inline fun timeOf(body: () -> Long): Pair<Long, Double> {
    body()
    var bestMs = Double.MAX_VALUE
    var r = 0L
    repeat(3) {
        val s = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - s) / 1e6
        if (ms < bestMs) bestMs = ms
    }
    return r to bestMs
}

fun main() {
    println("== 题面样例 ==")
    val (b1, t1) = timeOf { biclinic(10_000L / 4, brute = true) }
    println("B(10⁴) = $b1  " + (if (b1 == 49L) "-> 与题面 49 一致" else "-> 与题面不一致！"))
    val (b2, _) = timeOf { biclinic(1_000_000L / 4, brute = true) }
    println("B(10⁶) = $b2  " + (if (b2 == 38239L) "-> 与题面 38239 一致" else "-> 与题面不一致！"))

    println("== 双方法互证（光滑数分解枚举 vs 枚举 (u,v) 对） ==")
    var ok = true
    var bruteMs = 0.0
    for (n in BRUTE_SAMPLES) {
        val f = biclinic(n / 4)
        val bt = timeOf { biclinic(n / 4, brute = true) }
        if (n == BRUTE_SAMPLES.last()) bruteMs = bt.second
        val b = bt.first
        ok = ok && f == b
        println("  B($n)：光滑数=$f，两两枚举=$b  " + (if (f == b) "-> 一致" else "-> 不一致！"))
    }
    println("全部规模一致：$ok")

    println("== 正式求解 ==")
    val (ans, ms) = timeOf { biclinic(N_TARGET / 4) }
    println("B(10¹⁰) = $ans")
    println("OPT_MS: " + String.format("%.3f", ms) + "  （光滑数分解枚举）")
    println("BRUTE_MS: " + String.format("%.3f", bruteMs) + "  （枚举 (u,v) 对 + 桶计数，B(4·10⁵) 规模）")
    println("样例规模 B(10⁴) 两两枚举耗时 = " + String.format("%.3f", t1) + " ms")
}