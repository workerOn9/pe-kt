/**
 * Project Euler 136 — Singleton Difference（唯一的差）
 *
 * 思路：x² − y² − z² = n > 0 已排除公差为正与公差为零的情形，故可设
 * x = a + d、y = a、z = a − d（a ≥ 1、d ≥ 1，z > 0 即 a ≥ d + 1），于是
 *     n = (a + d)² − a² − (a − d)² = 4ad − a² = a(4d − a)。
 * 令 u = a、v = 4d − a，则解与「有序对 (u, v)」一一对应：
 *     uv = n，u + v = 4d ≡ 0 (mod 4)，且 a ≥ d + 1 ⟺ 3u − v ≥ 4。
 *
 * 记 F(n) = #{(u,v) : uv = n, u + v ≡ 0 (mod 4)}。写 n = 2^s·m（m 为奇数），u = 2^iρ、v = 2^jσ
 * （i + j = s、ρσ = m）：
 *   · s = 0：u、v 皆奇，u + v ≡ 0 (mod 4) ⟺ 一对因子一个 ≡ 1、一个 ≡ 3 (mod 4)。由 uv ≡ 3 知
 *     n ≡ 3 (mod 4) 时全部因子对合格（F = τ(n)），n ≡ 1 (mod 4) 时一对也不合格（F = 0）。
 *   · s = 1：u、v 一奇一偶，u + v 为奇数，F = 0。
 *   · s ≥ 2：i ≠ j（设 i < j）时 u + v = 2^i(ρ + 2^{j−i}σ)，括号内为奇数，故合格 ⟺ i ≥ 2；
 *     i = j = s/2 时 u + v = 2^{s/2}(ρ + σ)，由 ρ + σ 为偶数知恒合格。合格的指数分配数为
 *     g(2) = 1、g(3) = 0、g(s) = s − 3（s ≥ 4，其中已含 i = j 那一种），每个分配可配 τ(m) 组
 *     奇因子对，故 F(n) = τ(m)·g(s)（奇 n 用第一条）。
 *
 * 记 C(n) 为合格的无序因子对数（完全平方的中点只算一次），则
 *     C(n) = (F(n) + [n 是完全平方数且平方根为偶数]) / 2。
 * 对每个无序对 {w, w'}（w < w'），方向 (w', w) 恒有 3u − v ≥ 2w' ≥ 2√n ≥ 4（n ≥ 3）；方向
 * (w, w') 合格当且仅当 3w − w' ≥ 4 ⟺ 3w² − 4w ≥ n，把只数后者的个数记为 D(n)。于是
 *     R(n) = C(n) + D(n)，且 R(n) = 1 ⟺ C(n) = 1 且 D(n) = 0。
 *
 * 枚举 τ(m)·g(s) ≤ 2 的全部情形，C(n) = 1 只可能是：n 为 ≡ 3 (mod 4) 的素数（τ = 2）；n = 4、16
 * （m = 1）；n = 4p、16p（p 为奇素数，s = 2、4）；以及 n = 32（s = 5、m = 1）。逐个核对 D(n)：
 *   · 素数的唯一因子对是 {n, 1}，1 太小（3·1 − 4 < n）；
 *   · 4、16 的因子对是中点 (2,2)、(4,4)，不构成第二个方向；
 *   · {2, 2p}、{4, 4p} 的平衡性 3w² − 4w ≥ n 分别要求 p ≤ 1、p ≤ 2，奇素数都不满足；
 *   · 只有 {4, 8} 满足 3·16 − 16 = 32 ≥ 32，即 R(32) = 2，要扣掉。
 * 最终：恰有一个解的 n 恰是 {≡ 3 (mod 4) 的素数} ∪ {4, 16} ∪ {4p, 16p : p 为奇素数}。
 * 一百以内为 13 + 1 + 8 + 1 + 2 = 25 个，与题面的二十五个一致。
 *
 * 实现：只需知道「小于 5×10⁷ 的素数」与两个阈值以内的素数个数，故做一次分段埃氏筛——段内用
 * 位图缓冲（65536 个奇数 = 8 KB，常驻 L1），大素数跨段标记时不会每步都撞缓存不命中；
 * 数素数时用 Long.bitCount 整字统计，k 为奇数即 2k + 1 ≡ 3 (mod 4)，无需取模。
 *
 * 复杂度：时间 O(N log log N)（N = 5×10⁷），空间 O(√N + 8 KB)，与「枚举全部解」的 Θ(N log N)
 * 相比把 1.1×10⁸ 次解枚举换成了约 5×10⁷ 次筛选标记。全部量 < 5×10⁷，Int 足够。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 整数平方根（向下取整）。 */
fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** 朴素奇数筛：isPrime[k] = (2k + 1 是素数)，覆盖 2k + 1 < limit。只用于小范围。 */
fun oddSieve(limit: Int): BooleanArray {
    val isPrime = BooleanArray(limit / 2) { true }
    isPrime[0] = false                                   // 1 不是素数
    var p = 3
    while (p.toLong() * p < limit) {
        if (isPrime[p / 2]) {
            var j = p * p
            while (j < limit) {
                isPrime[j / 2] = false
                j += 2 * p
            }
        }
        p += 2
    }
    return isPrime
}

/** 清除位图第 b 位的掩码。 */
private val CLEAR_BIT = LongArray(64) { (1L shl it).inv() }

/**
 * 分段埃氏筛（只处理奇数，位图缓冲 8 KB）。
 * 返回 [c3, c1, c2]：c3 = 小于 limit 且 ≡ 3 (mod 4) 的素数个数；
 * c1、c2 = 不超过 t1、t2 的奇素数个数（调用方保证 t1 ≤ t2 < limit）。
 */
fun segmentedStats(limit: Int, t1: Int, t2: Int): LongArray {
    val segBits = 1 shl 16                               // 每段 65536 个奇数
    val segWords = segBits ushr 6
    val bits = LongArray(segWords)

    val root = isqrt(limit.toLong()).toInt() + 1
    val small = oddSieve(root)
    val primes = ArrayList<Int>()
    for (k in 1 until small.size) if (small[k]) primes.add(2 * k + 1)

    var c3 = 0L
    var c1 = 0L
    var c2 = 0L
    val kEnd = limit / 2                                 // 奇数 2k + 1 < limit ⟺ k < kEnd
    var kLo = 0
    while (kLo < kEnd) {
        var kHi = kLo + segBits
        if (kHi > kEnd) kHi = kEnd
        val n = kHi - kLo

        java.util.Arrays.fill(bits, -1L)                 // 先当作全是素数
        val rem = n and 63
        if (rem != 0) bits[n ushr 6] = (1L shl rem) - 1L // 尾字只保留有效的低位
        if (kLo == 0) bits[0] = bits[0] and CLEAR_BIT[0] // 1 不是素数
        val lastNum = 2 * kHi - 1                        // 本段最大的奇数

        for (p in primes) {
            if (p.toLong() * p > lastNum) break          // p² 已越出本段，无需标记
            val pl = p.toLong()
            var q = (2L * kLo + 1 + pl - 1) / pl         // ⌈(2kLo + 1) / p⌉：本段第一个 p 的倍数
            if (q < pl) q = pl                           // 从 p² 起标，别把 p 自己划掉
            if ((q and 1L) == 0L) q++                    // 只标奇倍数
            var i = ((pl * q - 1) / 2 - kLo).toInt()     // 对应的段内奇数下标
            while (i < n) {
                bits[i ushr 6] = bits[i ushr 6] and CLEAR_BIT[i and 63]
                i += p
            }
        }

        val words = (n + 63) ushr 6
        var total = 0
        for (w in 0 until words) total += java.lang.Long.bitCount(bits[w])
        // k 为奇数 ⟺ 2k + 1 ≡ 3 (mod 4)；kLo 为偶数时奇数下标记在奇位
        val mask = if ((kLo and 1) == 0) -0x5555555555555556L else 0x5555555555555555L
        var t3 = 0
        for (w in 0 until words) t3 += java.lang.Long.bitCount(bits[w] and mask)

        val segMin = 2 * kLo + 1
        c3 += t3
        if (lastNum <= t1) c1 += total
        else if (segMin <= t1) {
            var k = kLo
            while (2 * k + 1 <= t1) {
                val i = k - kLo
                if ((bits[i ushr 6] ushr (i and 63)) and 1L == 1L) c1++
                k++
            }
        }
        if (lastNum <= t2) c2 += total
        else if (segMin <= t2) {
            var k = kLo
            while (2 * k + 1 <= t2) {
                val i = k - kLo
                if ((bits[i ushr 6] ushr (i and 63)) and 1L == 1L) c2++
                k++
            }
        }
        kLo = kHi
    }
    return longArrayOf(c3, c1, c2)
}

/**
 * 小于 limit 且恰好只有一个解的 n 的个数：
 * {≡ 3 (mod 4) 的素数} ∪ {4, 16} ∪ {4p, 16p : p 为奇素数}。
 */
fun solve(limit: Int = 50_000_000): Long {
    val t4 = (limit - 1) / 4                             // 4p < limit ⟺ p ≤ (limit − 1)/4
    val t16 = (limit - 1) / 16
    val stats = segmentedStats(limit, t4, t16)
    var count = stats[0]
    if (limit > 4) count += 1 + stats[1]
    if (limit > 16) count += 1 + stats[2]
    return count
}

/**
 * 按定义枚举：解 (x, y, z) = (a + d, a, a − d)，d ≥ 1、d + 1 ≤ a ≤ 4d − 1，
 * 把 n = a(4d − a) < limit 逐个计数，返回解数恰为 1 的 n（升序）。只用于小规模自查。
 */
fun uniqueByDefinition(limit: Int): List<Int> {
    val cnt = IntArray(limit)
    var d = 1
    while (4 * d - 1 < limit) {
        var a = d + 1
        while (a < 4 * d) {
            val n = a * (4 * d - a)
            if (n < limit && cnt[n] < 2) cnt[n]++
            a++
        }
        d++
    }
    return (1 until limit).filter { cnt[it] == 1 }
}

/** 上面那个集合形式，只用于小规模自查时与按定义枚举的结果对比。 */
fun uniqueByCharacterization(limit: Int): List<Int> {
    val isPrime = oddSieve(limit)
    val out = sortedSetOf<Int>()
    for (k in 1 until isPrime.size) {
        if (isPrime[k] && (2 * k + 1) % 4 == 3) out.add(2 * k + 1)
    }
    if (4 < limit) out.add(4)
    if (16 < limit) out.add(16)
    var p = 3
    while (4L * p < limit) {
        if (isPrime[p / 2]) out.add(4 * p)
        p += 2
    }
    p = 3
    while (16L * p < limit) {
        if (isPrime[p / 2]) out.add(16 * p)
        p += 2
    }
    return out.toList()
}

/** 朴素地统计同样的三个量，用于核对分段筛。 */
fun naiveStats(limit: Int, t1: Int, t2: Int): LongArray {
    val isPrime = oddSieve(limit)
    var c3 = 0L
    var c1 = 0L
    var c2 = 0L
    for (k in 1 until isPrime.size) {
        if (!isPrime[k]) continue
        val p = 2 * k + 1
        if (p % 4 == 3) c3++
        if (p <= t1) c1++
        if (p <= t2) c2++
    }
    return longArrayOf(c3, c1, c2)
}

fun verifySample() {
    // 题面样例：n = 20 恰有一个解，就是 13² − 10² − 7²（a = 10、d = 3）
    check(13L * 13 - 10L * 10 - 7L * 7 == 20L)
    check(10 * (4 * 3 - 10) == 20)
    check(uniqueByDefinition(21).filter { it == 20 } == listOf(20)) { "n = 20 应恰有一个解" }

    // 题面：一百以内恰有二十五个 n 有唯一解
    check(solve(100) == 25L) { "一百以内应为 25 个，实得 ${solve(100)}" }
    check(uniqueByDefinition(100).size == 25)
    check(solve(1000) == 158L) { "一千以内应为 158 个，实得 ${solve(1000)}" }

    // 边界：小于 4 时只有 n = 3（4² − 3² − 2²），小于 5 时正是 n = 3 与 n = 4
    check(solve(3) == 0L && solve(4) == 1L && solve(5) == 2L)
    check(4L * 4 - 3L * 3 - 2L * 2 == 3L && 3L * 3 - 2L * 2 - 1L * 1 == 4L)
    // n = 32 是唯一被扣掉的反例：因子对 {4, 8} 两个方向都合法，解数为 2
    check(!uniqueByDefinition(33).contains(32)) { "n = 32 解数为 2，不应出现在唯一解集合里" }

    // 刻画与按定义枚举在中小规模上必须给出完全相同的集合
    for (limit in intArrayOf(100, 1000, 10_000)) {
        check(uniqueByCharacterization(limit) == uniqueByDefinition(limit)) {
            "limit = $limit 处刻画与定义枚举不一致"
        }
    }
    // 分段位筛在多个规模上与朴素筛逐项一致
    for (limit in intArrayOf(100, 1000, 10_000, 100_000, 1_000_000)) {
        val t1 = (limit - 1) / 4
        val t2 = (limit - 1) / 16
        check(segmentedStats(limit, t1, t2).contentEquals(naiveStats(limit, t1, t2))) {
            "limit = $limit 处分段筛与朴素筛不一致：${segmentedStats(limit, t1, t2).toList()} vs ${naiveStats(limit, t1, t2).toList()}"
        }
    }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                                // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
