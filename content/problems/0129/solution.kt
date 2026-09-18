/**
 * Project Euler 129 — Repunit Divisibility（循环单位数整除性）
 *
 * 思路：循环单位数满足 R(k) = (10^k − 1)/9，于是对 gcd(n, 10) = 1 的 n，
 *     n | R(k) ⟺ 9n | 10^k − 1 ⟺ 10^k ≡ 1 (mod 9n)，
 * 即 A(n) 恰是 10 在模 9n 下的乘法阶。把 n 写成 n = 3^a·m（3 ∤ m），由中国剩余定理
 *     A(n) = lcm(ord_{3^{a+2}}(10), ord_m(10)) = lcm(3^a, ord_m(10))，
 * 其中 ord_{3^j}(10) = 3^{j−2} 由 LTE：v_3(10^k − 1) = v_3(10 − 1) + v_3(k) = 2 + v_3(k)。
 *
 * 搜索起点为什么是 10^6 + 1（纯计数论证，不含阶论）：模 n 的映射 x ↦ (10x + 1) mod n
 * 是双射（gcd(10, n) = 1），故余数序列 R(1), R(2), … mod n 纯周期，整条轨道落在 n 个
 * 剩余类中，于是 A(n) ≤ n。要 A(n) > 10^6 就必须 n > 10^6。
 *
 * ord_m(10) 不靠「一次次乘 10 试」求：先由 m 的质因子分解算出 Carmichael 函数 λ(m)
 * （阶必为 λ(m) 的因子），再对 λ(m) 的每个质因子逐次用模幂判定、把多余的指数除尽。
 *
 * 复杂度：每个候选 n 一次 O(√n) 试除分解 + O(log m) 次模幂；与 10 互素的候选只有 10 个
 * （1000001 … 1000023），总计约 O(候选数 × √n)，空间 O(1)。全部中间量 < 2·10^6，用 Long。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 试除法分解 x ≥ 1，返回「质因子 → 指数」。 */
fun factorize(x: Long): Map<Long, Int> {
    val factors = LinkedHashMap<Long, Int>()
    var rest = x
    var d = 2L
    while (d * d <= rest) {
        if (rest % d == 0L) {
            var e = 0
            while (rest % d == 0L) {
                rest /= d
                e++
            }
            factors[d] = e
        }
        d += if (d == 2L) 1L else 2L
    }
    if (rest > 1L) factors[rest] = 1
    return factors
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

fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b

fun ipow(base: Long, exp: Int): Long {
    var result = 1L
    repeat(exp) { result *= base }
    return result
}

/** Carmichael 函数 λ(m)：使所有 gcd(a, m) = 1 的 a 都满足 a^λ(m) ≡ 1 (mod m) 的最小正指数。 */
fun carmichael(m: Long): Long {
    var lambda = 1L
    for ((p, e) in factorize(m)) {
        val primePart = when {
            p == 2L && e == 1 -> 1L
            p == 2L && e == 2 -> 2L
            p == 2L -> 1L shl (e - 2)          // 2^(e−2)，e ≥ 3
            else -> (p - 1) * ipow(p, e - 1)   // φ(p^e)，p 为奇素数
        }
        lambda = lcm(lambda, primePart)
    }
    return lambda
}

/** 模幂 base^exp mod mod（mod < 10^9 时中间乘积仍在 Long 内）。 */
fun modPow(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var result = 1L % mod
    while (e > 0L) {
        if (e and 1L == 1L) result = result * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return result
}

/** ord_m(10)：10 模 m 的乘法阶（要求 gcd(m, 10) = 1）。 */
fun orderOfTenMod(m: Long): Long {
    if (m == 1L) return 1L
    var order = carmichael(m)
    for (p in factorize(order).keys) {
        while (order % p == 0L && modPow(10L, order / p, m) == 1L) order /= p
    }
    return order
}

/** A(n)：最小的 k 使 n | R(k)（要求 gcd(n, 10) = 1）。 */
fun repunitDivisorLength(n: Long): Long {
    var m = n
    var threePower = 1L
    while (m % 3L == 0L) {
        m /= 3L
        threePower *= 3L
    }
    return lcm(threePower, orderOfTenMod(m))
}

/** 最小的 n，使 A(n) > threshold；由 A(n) ≤ n 知只需从 threshold + 1 起找。 */
fun leastNWithLengthAbove(threshold: Long): Long {
    var n = threshold + 1L
    while (true) {
        if (n % 2L != 0L && n % 5L != 0L && repunitDivisorLength(n) > threshold) return n
        n++
    }
}

/** 按定义递推 R(k) mod n，得到 A(n)（用于与阶公式在小范围上互证）。 */
fun repunitDivisorLengthByDefinition(n: Long): Long {
    var r = 0L
    var k = 0L
    while (true) {
        r = (r * 10L + 1L) % n
        k++
        if (r == 0L) return k
    }
}

fun verifySample() {
    // 题面样例
    check(repunitDivisorLength(7L) == 6L) { "A(7) 应为 6" }
    check(repunitDivisorLength(41L) == 5L) { "A(41) 应为 5" }
    check(41L * 271L == 11111L) { "R(5) = 11111 = 41 × 271" }
    // 题面边界：A(n) 首次超过 10 的最小 n 是 17
    check(leastNWithLengthAbove(10L) == 17L) { "A(n) > 10 的最小 n 应为 17" }
    check(repunitDivisorLength(17L) == 16L) { "A(17) 应为 16" }
    // A(n) ≤ n 的两个取等号情形（n 为 3 的幂），顺带检查 3 的幂分支
    check(repunitDivisorLength(3L) == 3L) { "A(3) 应为 3" }
    check(repunitDivisorLength(9L) == 9L) { "A(9) 应为 9" }
    // 阶公式与定义式逐个数互证：n < 20000 上必须完全一致
    for (n in 1L until 20000L) {
        if (n % 2L == 0L || n % 5L == 0L) continue
        val byFormula = repunitDivisorLength(n)
        val byDefinition = repunitDivisorLengthByDefinition(n)
        check(byFormula == byDefinition) { "n=$n：阶公式 $byFormula ≠ 定义枚举 $byDefinition" }
    }
}

fun solve(): Long = leastNWithLengthAbove(1_000_000L)

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
