/**
 * Project Euler 133 — Repunit Nonfactors（非循环单位数因子的素数）
 *
 * 思路：repunit R(k) 有 k 个 1，即 R(k) = (10^k − 1) / 9（该除法恒为整数）。于是
 *
 *     p | R(k)  ⟺  9p | 10^k − 1  ⟺  10^k ≡ 1 (mod 9p)          （p 为素数）
 *
 * 这是整数等式的等价变形，不需要 p ∤ 9：p = 3 时模数就是 27，恰好对应
 * 「3 | R(k) ⟺ 3 | k」，而不是「k 任意」。
 * 记 d = ord₉ₚ(10)（10 与 9p 互素时的乘法阶）。则
 *
 *     ∃ n ≥ 1, p | R(10^n)  ⟺  ∃ n ≥ 1, d | 10^n  ⟺  d = 2^a · 5^b
 *
 * 最后一步是关键：10^n 的素因子只有 2 和 5，故 d | 10^n 当且仅当 d 本身是
 * 2^a·5^b 形（此时 n = max(a, b) 即可）。反过来，d 只要含 2、5 以外的素因子 q，
 * 就永远有 q ∤ 10^n，即 p 永远不整除任何 R(10^n)。
 * 若 gcd(10, 9p) > 1（即 p = 2 或 5），10^k ≡ 1 根本无解（左边恒是 g 的倍数、
 * 右边不是），同样属于「永远不是因子」。故判定统一为
 *
 *     p 永不整除 R(10^n)  ⟺  gcd(10, 9p) > 1  或  ord₉ₚ(10) 含 2、5 以外的素因子
 *
 * 阶的计算：d₀ = lcm(6, p − 1) 是阶的倍数（p ≠ 3 时它就是 Carmichael 函数
 * λ(9p) = lcm(λ(9), λ(p))；p = 3 时阶为 ord₂₇(10) = 3，而 3 | 6）。把 d₀ 用试除法分解，
 * 对每个素因子 q 反复试着把 d 除以 q（只要 10^(d/q) ≡ 1 成立），即得真正的阶。
 *
 * 复杂度：素数筛 O(N log log N)，N = 10⁵；每个素数再做 O(√d₀) 的试除法分解与
 * O(log m) 次模幂，总计约 O(N log log N + π(N)·√N)，空间 O(N)。
 * 全部中间量 < 9·10⁵，模乘用 Long；答案虽在 Int 范围内，累计仍用 Long。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

/** 埃氏筛：返回所有小于 limit 的素数。 */
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

/** 模幂 base^exp mod m，无溢出（m < 9·10⁵，乘积 < 10¹² 仍在 Long 内）。 */
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

fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/**
 * 10 模 9p 的乘法阶，要求 gcd(10, 9p) = 1。
 * d₀ = lcm(6, p − 1) 是阶的倍数（见文件头），故从 d₀ 出发逐素因子试降即得阶。
 */
fun orderOf10(p: Int, primes: IntArray): Long {
    val m = 9L * p
    var d = 6L * (p - 1).toLong() / gcd(6L, (p - 1).toLong())
    var rem = d
    for (q in primes) {
        if (q.toLong() * q > rem) break
        if (rem % q == 0L) {
            while (rem % q == 0L) rem /= q
            while (d % q == 0L && powMod(10L, d / q, m) == 1L) d /= q
        }
    }
    if (rem > 1L) {                                    // 试除后剩下的必是素因子
        while (d % rem == 0L && powMod(10L, d / rem, m) == 1L) d /= rem
    }
    return d
}

/** 素数 p 是否永远不可能整除任何 R(10^n)。 */
fun isNonfactor(p: Int, primes: IntArray): Boolean {
    if (gcd(10L, 9L * p) != 1L) return true            // p = 2, 5：R(k) 为奇数且末位恒为 1
    var d = orderOf10(p, primes)
    while (d % 2L == 0L) d /= 2L                       // 剥去 2 的幂
    while (d % 5L == 0L) d /= 5L                       // 剥去 5 的幂
    return d != 1L                                     // 还剩别的素因子 ⇒ 永远不整除
}

/** 小于 limit 的素数中「永远不是 R(10^n) 因子」的那些之和。 */
fun solve(limit: Int = 100_000): Long {
    val primes = primesBelow(limit)
    var sum = 0L
    for (p in primes) if (isNonfactor(p, primes)) sum += p
    return sum
}

/** R(len) = 111…1（len 个 1）。 */
fun repunit(len: Int): BigInteger =
    BigInteger.TEN.pow(len).subtract(BigInteger.ONE).divide(BigInteger.valueOf(9))

fun verifySample() {
    val primes = primesBelow(1000)

    // 题面：一百以内「能成为某个 R(10ⁿ) 的因子」的素数只有 11、17、41、73
    val canBeFactor = primesBelow(100).filterNot { isNonfactor(it, primes) }
    check(canBeFactor == listOf(11, 17, 41, 73)) {
        "一百以内应为 [11, 17, 41, 73]，实得 $canBeFactor"
    }

    // 题面：R(10)、R(100)、R(1000) 都不被 17 整除，而 R(10000) 被 17 整除
    val seventeen = BigInteger.valueOf(17)
    check(repunit(10).mod(seventeen) != BigInteger.ZERO)
    check(repunit(100).mod(seventeen) != BigInteger.ZERO)
    check(repunit(1000).mod(seventeen) != BigInteger.ZERO)
    check(repunit(10_000).mod(seventeen) == BigInteger.ZERO)

    // 这四个可整除者的阶都是 2^a·5^b（2、16、5、8），故存在 n 使 d | 10^n
    check(orderOf10(11, primes) == 2L)
    check(orderOf10(17, primes) == 16L)
    check(orderOf10(41, primes) == 5L)
    check(orderOf10(73, primes) == 8L)

    // 题面：19 永远不是因子。依据 ord₁₉(10) = 18 = 2·3² 含素因子 3
    check(orderOf10(19, primes) == 18L) { "ord₁₉(10) 应为 18" }
    // 再把定义逐点走一遍：n = 1…18 时 10^(10^n) ≢ 1 (mod 9·19 = 171)
    // （指数上限取 10¹⁸ 是因为 10¹⁹ 已超 Long；阶只有 18，18 步足够看出模式）
    var exponent = 10L
    for (n in 1..18) {
        check(powMod(10L, exponent, 171L) != 1L) { "n = $n 时不该整除" }
        exponent *= 10
    }

    // 3：3 | R(k) ⟺ 27 | 10^k − 1 ⟺ 3 | k，而 3 ∤ 10^n；2、5 与所有 repunit 互素
    check(isNonfactor(2, primes) && isNonfactor(3, primes) && isNonfactor(5, primes))
    check(orderOf10(3, primes) == 3L)

    // 最小规模上的可见答案：小于 4 的素数只有 2、3（5 也在内），都是非因子
    check(solve(4) == 5L)
    check(solve(6) == 10L)
    check(solve(11) == 17L)                            // 再加 7（ord₇(10) = 6），11 是因子
}

fun main() {
    verifySample()
    repeat(5) { solve() }                              // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
