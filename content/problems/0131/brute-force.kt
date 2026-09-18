/**
 * Project Euler 131 — 素数立方伙伴 · 暴力对照解（教学对比用）
 *
 * 与 solution.kt 的枚举方向相反：
 *   · solution.kt：从 a 出发正向生成候选 p = 3a² + 3a + 1，再到素数表里查一次，只做 576 次生成；
 *   · 本解：从素数表出发反向检验——对小于 10⁶ 的每个素数 p 单独判定它是否是某个 3a² + 3a + 1。
 *     由 3a² + 3a + 1 = p（a ≥ 1）配方得 3(2a + 1)² = 4p − 1，即
 *     (2a + 1)² = (4p − 1)/3，于是只需验证 4p − 1 被 3 整除、且 (4p − 1)/3 是完全平方数、
 *     开出来的根是 ≥ 3 的奇数。共 π(10⁶) = 78498 次检验，这是两者耗时差距的主要来源。
 *
 * 反向判定只用了一次配方，所以还要防「配方推错导致的假阳性」：对每个判定命中的素数，把还原出的
 * n = a³ 代回定义，直接核验 n³ + n²p 是完全立方数——用整数立方根回验，不复用 m = a²(a + 1)
 * 这个闭式结论（n³ 可达 7×10²⁴，用 BigInteger）。verifySample 里另有一段「纯按定义枚举」的
 * 小规模对照（p < 1000、n ≤ 2×10⁵）：枚举方程 n³ + n²p = m³ 的 (n, m) 两端，不做任何配方，
 * 用来独立确认「合法素数 ⟺ 3a² + 3a + 1」这条约化本身没错。
 *
 * 复杂度：筛法 O(N log log N) + O(π(N)) 次整数开方，总计 O(N log log N)，空间 O(N)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 小于 limit 的全部素数（升序）。 */
fun primesBelow(limit: Int): IntArray {
    val isPrime = BooleanArray(limit) { true }
    if (limit > 0) isPrime[0] = false
    if (limit > 1) isPrime[1] = false
    var i = 2
    while (i.toLong() * i < limit) {
        if (isPrime[i]) {
            var j = i * i
            while (j < limit) {
                isPrime[j] = false
                j += i
            }
        }
        i++
    }
    var count = 0
    for (x in 2 until limit) if (isPrime[x]) count++
    val primes = IntArray(count)
    var k = 0
    for (x in 2 until limit) if (isPrime[x]) primes[k++] = x
    return primes
}

/**
 * n 的整数平方根（向下取整）。用 double 估计后向真值收敛，
 * 避免 Math.sqrt 在 2^53 附近的舍入把平方数判错。
 */
fun isqrt(n: Long): Long {
    if (n < 0) return -1
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/**
 * v（v ≥ 0）是否完全立方数，且返回其立方根（否则返回 −1）。
 * 立方根用 double 估计后用 BigInteger 回验，避免精度问题；v 最大约 7×10²⁴。
 */
fun perfectCubeRoot(v: BigInteger): Long {
    val est = Math.cbrt(v.toDouble()).toLong()
    for (m in (est - 2)..(est + 2)) {
        if (m < 0) continue
        if (BigInteger.valueOf(m).pow(3) == v) return m
    }
    return -1L
}

/**
 * 反向判定：素数 p 是否能写成 3a² + 3a + 1（a ≥ 1）。
 * 为真时顺带按定义核验还原出的 n = a³ 确实满足 n³ + n²p 为完全立方数。
 */
fun isPrimeCubePartner(p: Int): Boolean {
    val num = 4L * p - 1
    if (num % 3L != 0L) return false                  // 3(2a+1)² = 4p − 1
    val sq = num / 3L
    val r = isqrt(sq)                                 // r 应为 2a + 1
    if (r * r != sq) return false
    if (r % 2L == 0L || r < 3L) return false           // 2a + 1 是 ≥ 3 的奇数
    val a = (r - 1L) / 2L
    val n = BigInteger.valueOf(a).pow(3)               // n = a³
    val v = n.pow(3).add(n.pow(2).multiply(BigInteger.valueOf(p.toLong())))
    check(perfectCubeRoot(v) > 0L) { "p = $p 还原出的 n = ${n}·… 不满足定义" }
    return true
}

/** 暴力解：逐个素数反向检验。 */
fun solveBruteForce(limit: Int = 1_000_000): Long =
    primesBelow(limit).count { isPrimeCubePartner(it) }.toLong()

/**
 * 纯定义枚举（小规模对照）：既不经过配方，也不假设 n 是立方数，而是直接枚举方程
 * n³ + n²p = m³ 的两端——对 n = 1…nMax，把使 n²p ≤ n²(limit − 1) 的每个 m > n 都试一遍；
 * 只要 m³ − n³ 被 n² 整除、商 p 小于 limit 且为素数，就说明该素数满足定义。
 * limit = 1000 时解只出现在 n ≤ 17³ = 4913，这里取 nMax = 200000（40 倍余量），
 * 用来确认更大范围内不会冒出新的解。枚举次数约 8×10⁵，代价可忽略。
 */
fun definitionalPartners(limit: Int, nMax: Int): List<Int> {
    val isPrime = BooleanArray(limit) { true }
    if (limit > 0) isPrime[0] = false
    if (limit > 1) isPrime[1] = false
    var i = 2
    while (i.toLong() * i < limit) {
        if (isPrime[i]) {
            var j = i * i
            while (j < limit) {
                isPrime[j] = false
                j += i
            }
        }
        i++
    }
    val found = HashSet<Int>()
    val pMax = (limit - 1).toLong()
    for (n in 1..nMax) {
        val n2 = n.toLong() * n                                  // n² ≤ 4×10¹⁰
        val n3 = n2 * n                                          // n³ ≤ 8×10¹⁵
        val hi = n3 + n2 * pMax                                  // m³ 的截断来自 p ≤ limit − 1
        var m = n + 1L
        var m3 = m * m * m
        while (m3 <= hi) {
            val diff = m3 - n3
            if (diff % n2 == 0L) {
                val p = diff / n2
                if (p < limit && isPrime[p.toInt()]) found.add(p.toInt())
            }
            m++
            m3 = m * m * m
        }
    }
    return found.sorted()
}

fun main() {
    // 题面：一百以内恰有四个这样的素数
    val below100 = primesBelow(100).filter { isPrimeCubePartner(it) }
    check(below100 == listOf(7, 19, 37, 61)) { "一百以内应为 7, 19, 37, 61，实得 $below100" }
    // 题面例子：p = 19 时 n = 8，8³ + 8² × 19 = 512 + 1216 = 1728 = 12³
    check(8L * 8 * 8 + 8L * 8 * 19 == 12L * 12 * 12)

    // 不经过配方的纯定义枚举对照（小规模），并确认「同一素数解唯一」
    val defSmall = definitionalPartners(1000, 200_000)
    check(defSmall == listOf(7, 19, 37, 61, 127, 271, 331, 397, 547, 631, 919)) { "定义枚举结果异常：$defSmall" }
    check(solveBruteForce(1000) == defSmall.size.toLong()) { "1000 以内反向检验与定义枚举不一致" }

    repeat(5) { solveBruteForce() }                  // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
