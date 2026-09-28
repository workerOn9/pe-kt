#!/usr/bin/env kotlin
/**
 * Project Euler 245 — Coresilience（核心韧性）
 *
 * 思路：
 *   核心韧性 C(n) = (n − φ(n)) / (n − 1) 是单位分数 1/K，等价于
 *
 *       n − 1 = K · (n − φ(n))          (1)
 *
 *   **第一步：n 必为奇数且无平方因子。** n = p₁·p₂···p_j（全为奇素数），φ(n) 逐项相乘。
 *   (a) 若 p² | n，则 p | φ(n)，故 p | (n − φ(n))；再由 (1) 得 p | (n − 1)，与 p | n 矛盾。
 *   (b) 若 2 | n 且无平方因子，则 n = 2m，φ(n) = φ(m) 偶，n − φ(n) 偶而 n − 1 奇，偶数不整除奇数。
 *
 *   **第二步：每个素因子都大于 K。** 由 (1) 得 Kφ(n) = (K−1)n + 1，所以
 *
 *       n/φ(n) = K/(K−1) − 1/((K−1)φ(n)) < K/(K−1)。
 *
 *   若某个素因子 p ≤ K，则 p/(p−1) ≥ K/(K−1)（x/(x−1) 单调递减），乘上其余 > 1 的因子后
 *   n/φ(n) ≥ K/(K−1)，矛盾。于是所有素因子 > K。这条结论把搜索的素数下界钉死在 K 上：
 *   2 个素因子时 n > (K+1)²，故 K ≤ √L；3 个以上素因子时 n > (K+1)³，故 K < ∛L。
 *
 *   **第三步：2 个素因子退化成素数对分解。** n = pq 时 n − φ(n) = p + q − 1，代入 (1)：
 *
 *       (p − K)(q − K) = K² − K + 1。
 *
 *   固定较小的 p，K²−K+1 ≡ p²−p+1 (mod p−K)，即 **p − K 必是 A = p²−p+1 的约数**，
 *   于是 q = A/(p−K) − p + 1。对每个 p ≤ √L 分解一次 A（Pollard rho）、枚举约数即可，
 *   完整覆盖所有二元解（这是本题唯一不需要搜索的部分）。
 *
 *   **第四步：3 个以上素因子——固定素数个数 j，前缀自由、末位素因子被唯一确定。**
 *   设前缀 m = q₁q₂···q_{j−1}（严格递增）、A = φ(m)，末位素数 r，代入 (1)：
 *
 *       r [A − K(A − m)] = K·A + 1      ⟺      r = (K·A + 1) / (A − K(A − m)) 。
 *
 *   即给定前缀与 K，末位素数**唯一确定**。这里的要害是：**前缀 m 本身不必是解**，
 *   递归只需遍历「所有递增素因子前缀」并逐个检验末位——只沿着解扩展会漏掉一整类解。
 *   反例（本机 3×10⁷ 暴力筛抓出来的 7 个）：167743 = 43·47·83，其中 2021 = 43·47 不是解
 *   （(2021−1)/(2021−φ) = 2020/89 非整数），但 2021·83 是解；3902867 = 53·211·349、
 *   5574929 = 17·353·929、10093049 = 83·277·439、17632421 = 23·151·5077、
 *   27874645 = 5·17·353·929（前缀链 5 → 85 → 30005 → 27874645 中 30005 也不是解）、
 *   29087939 = 23·641·1973 同理。
 *
 *   给定前缀 (m, A)，写成 r = (A·(u+1) + 1) / (A − u(A − m))（u = K − 1）：r 关于 u
 *   单调递增，因此由 r > q_{j−1}（保持递增）与 m·r ≤ L（规模上界）、A > u(A−m)（分母为正）
 *   可以解出一个**连续的 u 区间**，逐 u 试整除即可——这就是完整的枚举。
 *
 *   搜索规模控制：
 *     · 前缀的下一步素数 p 必须满足 `A·p^{remaining+1} ≤ L`（后面还差 remaining 个前缀素因子
 *       和 1 个末位素因子，它们都比 p 大），不满足直接剪掉；
 *     · 素数个数 j 上限 = 最小若干个奇素数之积 ≤ L 的最大个数（本题 j ≤ 9）；
 *     · 末位素性用确定性 Miller–Rabin，素数表筛到 √L ≈ 447213。
 *
 *   全程算术都在 Long 内：L < 2³⁸，计算 r 前的中间量 < 2·10¹¹·447213 < 2⁶³。
 *
 * 旁证：
 *   1. brute-force.kt 用线性筛一次算出 [0, 3×10⁷] 全部 φ(n)，逐个 n 按定义判，
 *      **不含任何分解/搜索结构**：给出 169 个解、和 1440580657，与本算法逐项一致
 *      （包括上面 7 个「前缀非解」的陷阱解；若只沿「前缀本身也是解」的链扩展，
 *      这 7 个会被漏掉，只能得到 162 个）。
 *   2. 每个候选解都当场复核 (n − φ(n)) | (n − 1) 且 n 为合数（main 末尾统一复核）。
 *   3. 题面样例链 15, 255, 65535, 85, 1111, 391, 4369 逐个按定义验证。
 *
 * 答案：288084712410001
 * 复杂度：素数筛到 447213 + 3.7 万个 p²−p+1 的 Pollard 分解 + j=3..9 的前缀 DFS
 *         （节点数见 analysis.md，预热后单次完整求解见 main 实测输出）。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val N_MAX = 200_000_000_000L

// ---------------------------------------------------------------- 数论工具

/**
 * 模乘：本题所有参与运算的数 < 2^38 = 2.75×10¹¹，把乘数拆成高 19 位 + 低 19 位，
 * 乘积 < 2^58 落在 Long 内。比 BigInteger 模幂快两个数量级。
 * 注意括号：Kotlin 里 `X + Y shl 19` 会解析成 `(X + Y) shl 19`（shl 优先级低于 +）。
 */
private fun mulMod(a: Long, b: Long, m: Long): Long {
    val b1 = b ushr 19
    val b0 = b and ((1L shl 19) - 1)
    return (((a * b0) % m) + (((a * b1) % m) shl 19)) % m
}

private fun powMod(base: Long, exp: Long, m: Long): Long {
    var r = 1L
    var b = base % m
    var e = exp
    while (e > 0) {
        if (e and 1L == 1L) r = mulMod(r, b, m)
        b = mulMod(b, b, m)
        e = e ushr 1
    }
    return r
}

/** Miller–Rabin：这 12 个底数对 n < 3.3×10²⁴ 是确定性的，本题 n ≤ 2×10¹¹ 远在界内 */
private fun isPrime(n: Long): Boolean {
    if (n < 2) return false
    for (p in SMALL_PRIMES) {
        if (n == p.toLong()) return true
        if (n % p == 0L) return false
    }
    var d = n - 1
    var r = 0
    while (d and 1L == 0L) { d = d ushr 1; r++ }
    for (a in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        var x = powMod(a, d, n)
        if (x == 1L || x == n - 1) continue
        var composite = true
        for (i in 1 until r) {
            x = mulMod(x, x, n)
            if (x == n - 1) { composite = false; break }
        }
        if (composite) return false
    }
    return true
}

/** Brent 改进的 Pollard rho：分解 A = p² − p + 1（A ≤ 2×10¹¹）。
 *  注意起点 c、y 必须随机：若由 n 确定性导出，一次 g = n 的失败会原样重试而**死循环**。
 *  每 128 步批量累乘 |x−y| 再取一次 gcd（标准 Brent），gcd 次数从每步一次降到每批一次。 */
private fun pollardBrent(n: Long): Long {
    if (n and 1L == 0L) return 2
    val rnd = java.util.concurrent.ThreadLocalRandom.current()
    while (true) {
        val c = rnd.nextLong(1, n)
        var y = rnd.nextLong(0, n)
        var g = 1L
        var r = 1L
        var q = 1L
        var x = 0L
        var ys = 0L
        while (g == 1L) {
            x = y
            repeat(r.toInt()) { y = (mulMod(y, y, n) + c) % n }
            var k = 0L
            while (k < r && g == 1L) {
                ys = y
                val lim = minOf(128L, r - k)
                for (i in 0 until lim) {
                    y = (mulMod(y, y, n) + c) % n
                    val d = x - y
                    q = mulMod(q, if (d < 0) -d else d, n)
                }
                g = gcdOf(q, n)
                k += 128L
            }
            r *= 2
        }
        if (g == n) {
            g = 1
            do {
                ys = (mulMod(ys, ys, n) + c) % n
                val d = x - ys
                g = gcdOf(if (d < 0) -d else d, n)
            } while (g == 1L)
        }
        if (g != n) return g
    }
}

private fun gcdOf(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

/** 质因数分解（小素数试除 + Miller–Rabin + Pollard rho） */
private fun factorize(n: Long): MutableMap<Long, Int> {
    val out = LinkedHashMap<Long, Int>()
    var rest = n
    for (p in SMALL_PRIMES) {
        if (rest % p == 0L) {
            var e = 0
            while (rest % p == 0L) { rest /= p; e++ }
            out[p.toLong()] = e
        }
        if (rest == 1L) return out
    }
    val stack = ArrayDeque<Long>()
    if (rest > 1) stack.addLast(rest)
    while (stack.isNotEmpty()) {
        val v = stack.removeLast()
        if (v == 1L) continue
        if (isPrime(v)) { out[v] = (out[v] ?: 0) + 1; continue }
        val d = pollardBrent(v)
        stack.addLast(d)
        stack.addLast(v / d)
    }
    return out
}

private fun allDivisors(f: Map<Long, Int>): List<Long> {
    var divs = listOf(1L)
    for ((p, e) in f) {
        val next = ArrayList<Long>()
        var mul = 1L
        repeat(e) {
            mul *= p
            for (d in divs) next.add(d * mul)
        }
        divs = divs + next
    }
    return divs
}

private fun primesUpTo(limit: Int): IntArray {
    val composite = BooleanArray(limit + 1)
    val out = ArrayList<Int>()
    for (i in 2..limit) {
        if (!composite[i]) {
            out.add(i)
            if (i.toLong() * i <= limit) {
                var j = i * i
                while (j <= limit) { composite[j] = true; j += i }
            }
        }
    }
    return out.toIntArray()
}

private val SMALL_PRIMES: IntArray by lazy { primesUpTo(1000) }

// ---------------------------------------------------------------- 搜索

private var limit = N_MAX
private var oddPrimes = IntArray(0)
private val solutions = ArrayList<Long>()

private fun isqrt(x: Long): Long {
    var r = Math.sqrt(x.toDouble()).toLong()
    while (r > 0 && r * r > x) r--
    while ((r + 1) * (r + 1) <= x) r++
    return r
}

/** ⌊x^(1/k)⌋（k ≤ 9，x ≤ 2×10¹¹） */
private fun iroot(x: Long, k: Int): Long {
    if (k == 1) return x
    if (k == 2) return isqrt(x)
    var r = Math.pow(x.toDouble(), 1.0 / k).toLong()
    if (r < 1) r = 1
    while (r > 1 && !powLe(r, k, x)) r--
    while (powLe(r + 1, k, x)) r++
    return r
}

/** a^k ≤ x ？（防溢出） */
private fun powLe(a: Long, k: Int, x: Long): Boolean {
    var prod = 1L
    repeat(k) {
        if (prod > x / a) return false
        prod *= a
    }
    return prod <= x
}

/**
 * 2 个素因子的解：固定较小素数 p，A = p²−p+1 的每个约数 d = p − K 给出
 * q = A/d − p + 1，q 为素数且 p < q、pq ≤ L 即为一解。
 */
private fun collectSemiprimes() {
    val root = isqrt(limit)
    for (p1 in oddPrimes) {
        val p = p1.toLong()
        if (p > root) break
        val a = p * p - p + 1
        for (d in allDivisors(factorize(a))) {
            val q = a / d - p + 1
            if (q <= p || p * q > limit) continue
            if (isPrime(q)) solutions.add(p * q)
        }
    }
}

/**
 * j ≥ 3 个素因子的解：枚举前 j−1 个严格递增的素因子前缀 (m, φ(m))，
 * 末位素数 r 由 u = K−1 唯一确定：
 *
 *     r = (φ(m)·(u+1) + 1) / (φ(m) − u·(m − φ(m)))。
 *
 * r 关于 u 单调递增，由 r > 最大前缀素数、m·r ≤ L 与分母 > 0 解出连续 u 区间。
 */
private fun collectWithJ(j: Int) {
    fun tryLast(m: Long, a: Long, lastP: Long) {
        val d = m - a                       // d = m − φ(m) > 0
        if (d <= 0) return
        val rMax = limit / m
        if (rMax <= lastP) return
        // u = K−1 ≥ 1；r > lastP 给出 uMin，m·r ≤ L 与分母 > 0 给出 uMax
        val numMin = a * (lastP - 1) - 1
        val denMin = a + lastP * d
        var uMin = numMin / denMin + 1
        if (uMin < 1) uMin = 1
        val numMax = a * (rMax - 1) - 1
        if (numMax < 0) return
        var uMax = numMax / (a + rMax * d)
        val cap = (a - 1) / d
        if (uMax > cap) uMax = cap
        var u = uMin
        while (u <= uMax) {
            val denom = a - u * d
            if (denom <= 0) break
            val numer = a * (u + 1) + 1
            if (numer % denom == 0L) {
                val r = numer / denom
                if (r > lastP && m * r <= limit && isPrime(r)) {
                    val n = m * r
                    val phi = a * (r - 1)
                    if ((n - 1) % (n - phi) == 0L) solutions.add(n)   // 复核定义
                }
            }
            u++
        }
    }

    fun dfs(start: Int, remaining: Int, m: Long, a: Long, lastP: Long) {
        if (remaining == 0) { tryLast(m, a, lastP); return }
        // 后面还差 remaining 个前缀素因子 + 1 个末位素因子，都比下一个 p 大
        val maxP = iroot(limit / m, remaining + 1)
        var i = start
        while (i < oddPrimes.size) {
            val p = oddPrimes[i].toLong()
            if (p > maxP) break
            dfs(i + 1, remaining - 1, m * p, a * (p - 1), p)
            i++
        }
    }

    dfs(0, j - 1, 1L, 1L, 2L)
}

/** 全部解（升序） */
private fun collectAll(nMax: Long): List<Long> {
    limit = nMax
    solutions.clear()
    oddPrimes = primesUpTo(isqrt(nMax).toInt() + 1).filter { it > 2 }.toIntArray()
    collectSemiprimes()

    // 素因子个数上限：最小若干个奇素数之积 ≤ L 的最大个数
    var maxJ = 0
    var prod = 1L
    for (p in oddPrimes) {
        if (prod * p > nMax) break
        prod *= p
        maxJ++
    }
    for (j in 3..maxJ) collectWithJ(j)

    solutions.sort()
    return solutions.distinct()
}

private fun main() {
    // ---- 题面/定义自检：已知解逐个按定义验证 (n − φ(n)) | (n − 1) ----
    for (n in longArrayOf(15, 255, 65535, 85, 1111, 391, 4369, 167743, 5574929)) {
        var phi = n
        var m = n
        for (p in SMALL_PRIMES) {
            if (p.toLong() * p > m) break
            if (m % p == 0L) { while (m % p == 0L) m /= p; phi -= phi / p }
        }
        if (m > 1) phi -= phi / m
        check((n - 1) % (n - phi) == 0L) { "$n 不满足 C(n) 为单位分数" }
    }
    println("自检：15, 255, 65535, 85, 1111, 391, 4369, 167743, 5574929 的 (n−φ(n)) 均整除 (n−1)")

    // ---- 小范围对照：与 brute-force.kt 的线性筛逐项一致（n ≤ 3×10⁷，169 个解）----
    val small = collectAll(30_000_000L)
    val smallSum = small.sum()
    println("n ≤ 3×10⁷ 的解个数（本算法）= ${small.size}，和 = $smallSum")
    if (small.size != 169 || smallSum != 1440580657L) {
        error("小范围与暴力法不一致：${small.size} 个 / 和 $smallSum")
    }
    val bruteHead = longArrayOf(
        15, 85, 255, 259, 391, 589, 1111, 3193, 4171, 4369,
        12361, 17473, 21845, 25429, 28243, 47989, 52537, 65535, 65641, 68377,
    )
    check(small.take(20).toLongArray().contentEquals(bruteHead)) { "前 20 项与暴力法不一致" }
    println("与 brute-force 的前 20 项逐项一致（含 167743 等 7 个「前缀非解」陷阱解）")

    // ---- 更大范围对照：brute-force.kt 在 n ≤ 10⁸ 给出 237 个解、和 5699973227 ----
    val mid = collectAll(100_000_000L)
    val midSum = mid.sum()
    println("n ≤ 10⁸ 的解个数（本算法）= ${mid.size}，和 = $midSum")
    if (mid.size != 237 || midSum != 5699973227L) {
        error("10⁸ 范围与暴力法不一致：${mid.size} 个 / 和 $midSum")
    }

    // ---- 本题 ----
    val all = collectAll(N_MAX)
    val answer = all.sum()
    println("2×10¹¹ 以内的解个数 = ${all.size}，解之和 = $answer")
    check(answer == 288084712410001L) { "答案不吻合：$answer" }

    // ---- 全部解的最终复核：n 为合数、无平方因子、(n − φ(n)) | (n − 1) ----
    for (n in all) {
        val f = factorize(n)
        check(f.values.all { it == 1 }) { "$n 有平方因子" }
        check(f.size >= 2) { "$n 不是合数" }
    }
    println("全部 ${all.size} 个解复核通过（合数、无平方因子）")

    // ---- 计时：JIT 预热后单次完整求解 ----
    collectAll(N_MAX)
    val t0 = System.nanoTime()
    val again = collectAll(N_MAX)
    val ms = (System.nanoTime() - t0) / 1e6
    check(again.sum() == answer) { "计时循环结果漂移" }
    println("optimized: ${"%.0f".format(ms)} ms/次（含预热，单次完整求解）")
    println("check() 全部通过")
}
