/**
 * Project Euler 132 — Large Repunit Factors（大循环单位数的素因子）
 *
 * 思路：R(k) = (10^k − 1)/9。对素数 p ≠ 3，9 在模 p 下可逆，两边乘 9 得
 * 9·R(k) = 10^k − 1 ≡ 0 (mod p)，于是
 *     p | R(k)  ⟺  10^k ≡ 1 (mod p)  ⟺  ord_p(10) | k，
 * 其中 ord_p(10) 是 10 模 p 的乘法阶（数值上也就是小数 1/p 的循环节长度）。
 * 小素数要单独交代：p = 2, 5 时 10^k ≡ 0 ≠ 1，自动出局；p = 3 是唯一例外——上面
 * 「对 9 求逆」这一步失效，此时 R(k) 的数字和恰为 k，故 3 | R(k) ⟺ 3 | k。
 * k = 10⁹ ≡ 1 (mod 3)，所以 3 不是因子。（对照：10^10 ≡ 1 (mod 3)，但题面给出的
 * R(10) = 11·41·271·9091 里并没有 3。）
 *
 * 求阶不必分解 p − 1：由费马小定理 ord_p(10) | p − 1，而条件要的是 ord | k，两者合并
 *     ord_p(10) | k  ⟺  ord_p(10) | gcd(k, p − 1)  ⟺  10^gcd(k, p−1) ≡ 1 (mod p)。
 * 指数从 k = 10⁹ 降到 k 的约数 gcd(k, p−1) = 2^a·5^b（a, b ≤ 9）：实测 7…160001 这
 * 14681 个素数上平均指数只有 63，模乘次数从 63.1 万降到 7.3 万（约 8.7 倍）。
 * 素数按升序筛出逐个判定，凑满 40 个即停；上界取 200000（第 40 个因子是 160001）。
 *
 * 复杂度：筛法 O(B log log B)；每个素数一次 gcd + 一次快速幂，模幂位数 ≤ log k，
 * 总计 O(B log log B + π(B)·log k)，空间 O(B)。base² < p² < 2⁶³，Long 足够不溢出。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

/** 最大公约数（欧几里得辗转相除）。 */
fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

/** 快速幂：base^exp mod m。base < m ≤ 2×10⁵，平方后仍远小于 2⁶³。 */
fun powMod(base: Long, exp: Long, m: Long): Long {
    var result = 1L
    var b = base % m
    var e = exp
    while (e > 0) {
        if (e and 1L == 1L) result = result * b % m
        b = b * b % m
        e = e shr 1
    }
    return result
}

/** 埃氏筛：返回 ≤ limit 的全部素数（升序）。下标乘法先转 Long 防溢出。 */
fun sievePrimes(limit: Int): IntArray {
    val isComposite = BooleanArray(limit + 1)
    val primes = ArrayList<Int>(limit / 10)
    for (i in 2..limit) {
        if (!isComposite[i]) {
            primes.add(i)
            var j = i.toLong() * i
            while (j <= limit) { isComposite[j.toInt()] = true; j += i }
        }
    }
    return primes.toIntArray()
}

/** 素数 p 是否整除 R(n)：p = 3 用数位和规则，其余用 10^gcd(n, p−1) ≡ 1 (mod p)。 */
fun dividesRepunit(p: Int, n: Long): Boolean = when {
    p == 3 -> n % 3L == 0L                       // R(n) 的数字和恰为 n
    p == 2 || p == 5 -> false                    // gcd(10, p) ≠ 1，10^n ≡ 0 (mod p)
    else -> powMod(10L, gcd(n, p - 1L), p.toLong()) == 1L
}

/** 升序取前 count 个整除 R(n) 的素数求和。上界不足直接报错，绝不返回偏小的和。 */
fun solve(n: Long = 1_000_000_000L, count: Int = 40, limit: Int = 200_000): Long {
    var sum = 0L
    var found = 0
    for (p in sievePrimes(limit)) {
        if (dividesRepunit(p, n)) {
            sum += p
            if (++found == count) return sum
        }
    }
    throw IllegalStateException("上界 $limit 内只找到 $found 个素因子，需提高 limit")
}

/** 对 R(n) 直接试除法分解（BigInteger），完全绕开阶与 gcd 判据，用来锚定题目样例。 */
fun repunitPrimeFactors(n: Int): List<Int> {
    var rest = (BigInteger.TEN.pow(n) - BigInteger.ONE) / BigInteger.valueOf(9)
    val factors = ArrayList<Int>()
    var d = BigInteger.TWO
    while (d * d <= rest) {
        while (rest % d == BigInteger.ZERO) { factors.add(d.toInt()); rest /= d }
        d += BigInteger.ONE
    }
    if (rest > BigInteger.ONE) factors.add(rest.toInt())
    return factors
}

fun verifySample() {
    // 题面样例：R(10) = 11 × 41 × 271 × 9091，素因子和 9414
    val factors = repunitPrimeFactors(10)
    check(factors == listOf(11, 41, 271, 9091)) { "R(10) 的素因子应为 11, 41, 271, 9091，实得 $factors" }
    check(factors.sumOf { it.toLong() } == 9414L) { "R(10) 的素因子和应为 9414" }
    check(11L * 41 * 271 * 9091 == 1_111_111_111L)
    check(solve(10L, 4, 20_000) == 9414L) { "solve(R(10)) 应为 9414" }
    // 另一条独立锚点：BigInteger 分解出 R(20) = 11·41·101·271·3541·9091·27961，前四个和 424
    check(repunitPrimeFactors(20) == listOf(11, 41, 101, 271, 3541, 9091, 27961))
    check(solve(20L, 4, 20_000) == 424L) { "R(20) 最小四个素因子之和应为 424" }
    // 3 为什么必须特判：朴素判据 10^n ≡ 1 (mod 3) 恒真，但 3 | R(n) ⟺ 3 | n
    check(powMod(10L, 10L, 3L) == 1L && !factors.contains(3))
    check(!dividesRepunit(3, 1_000_000_000L)) { "10⁹ 的数位和 10⁹ ≡ 1 (mod 3)，3 不是 R(10⁹) 的因子" }
    check(1_000_000_000L % 3L == 1L)
    // gcd 降幂与原始指数等价：20000 以内每个素数上两条判据必须给出同一结论
    check(sievePrimes(20_000).all { p ->
        (powMod(10L, 1_000_000_000L, p.toLong()) == 1L) ==
            (powMod(10L, gcd(1_000_000_000L, p - 1L), p.toLong()) == 1L)
    }) { "gcd 降幂改变了判据结果" }
    // 最小因子是 11（ord₁₁(10) = 2 | 10⁹），前两个因子之和 11 + 17 = 28
    check(solve(1_000_000_000L, 1, 20_000) == 11L)
    check(solve(1_000_000_000L, 2, 20_000) == 28L)
}

fun main() {
    verifySample()
    repeat(5) { solve() }                            // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
