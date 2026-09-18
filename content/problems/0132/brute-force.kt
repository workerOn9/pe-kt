/**
 * Project Euler 132 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的思路正好相反。优化解从不真的「走」乘法阶，而是用 gcd 把指数从
 * 10⁹ 压成 gcd(10⁹, p − 1)，再用快速幂一步判定；本解则按定义朴素模拟——对每个素数
 * p 让余数迭代 r ← 10·r (mod p)，把 10¹, 10², 10³, … 模 p 的余数一个个列出来，直到
 * 它第一次回到 1。此时步数 k 就是 ord_p(10)（也就是 1/p 的循环节长度），再看 k 是否
 * 整除 n = 10⁹。没有快速幂、没有 gcd 降幂、也没有对 p − 1 做因子分解，纯靠线性走完
 * 整个循环；费马小定理保证循环长不过 p − 1，所以必定终止。
 *
 * 判据与优化解同源但要自己交代小素数：对 p ≠ 3 有 p | R(n) ⟺ 10^n ≡ 1 (mod p)；
 * p = 2, 5 时 10^n ≡ 0 自动出局；p = 3 因 10^n ≡ 1 (mod 3) 恒成立而必须让位给数位和
 * 规则（3 | R(n) ⟺ 3 | n）。
 *
 * 复杂度：筛法 O(B log log B)；每个素数 O(ord_p(10)) ≤ O(p) 次模乘。实测（跳过 2, 3, 5）
 * 扫过 14681 个素数、共约 6.4×10⁸ 次模乘，而优化解只需约 7.3×10⁴ 次，相差近四个数量级。
 *
 * 题面样例（R(10) = 11 × 41 × 271 × 9091，素因子和 9414）写成运行时断言；另用 JDK 的
 * BigInteger.modPow（指数直接取 10⁹）在小范围内对拍同一条判据。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

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

/** 线性扫描求 ord_m(10)：反复乘 10 取模，数出首次回到 1 的步数。 */
fun orderByScanning(m: Long): Long {
    var r = 10L % m
    var k = 1L
    while (r != 1L) { r = r * 10 % m; k++ }
    return k
}

/** 素数 p 是否整除 R(n)：p = 3 用数位和规则，其余看步数 ord 是否整除 n。 */
fun dividesRepunit(p: Int, n: Long): Boolean = when {
    p == 3 -> n % 3L == 0L
    p == 2 || p == 5 -> false
    else -> n % orderByScanning(p.toLong()) == 0L
}

/** 升序取前 count 个整除 R(n) 的素数求和。上界不足直接报错，绝不返回偏小的和。 */
fun solveBruteForce(n: Long = 1_000_000_000L, count: Int = 40, limit: Int = 200_000): Long {
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

fun verifySample() {
    // 题面样例：R(10) = 11 × 41 × 271 × 9091
    check(orderByScanning(11L) == 2L)
    check(orderByScanning(9091L) == 10L)
    check(solveBruteForce(10L, 4, 20_000) == 9414L) { "R(10) 的素因子和应为 9414" }
    // R(3) = 111 = 3 × 37：3 走数位和规则，验证特判本身是对的
    check(solveBruteForce(3L, 2, 20_000) == 40L) { "R(3) 的两个素因子应为 3 与 37" }
    // R(6) = 111111 = 3 × 7 × 11 × 13 × 37，前五个素因子之和 71
    check(solveBruteForce(6L, 5, 20_000) == 71L) { "R(6) 的前五个素因子之和应为 71" }
    // 3 必须特判的理由：10 ≡ 1 (mod 3) 会让扫描立刻回 1，但 10⁹ ≡ 1 (mod 3)，3 不是因子
    check(10L % 3L == 1L && 1_000_000_000L % 3L == 1L)
    // 小范围内与 JDK 的 BigInteger.modPow（指数直接取 10⁹，不做任何降幂）对拍
    val n = 1_000_000_000L
    val nBig = BigInteger.valueOf(n)
    val byScanning = sievePrimes(5_000).filter { p ->
        p != 2 && p != 3 && p != 5 && n % orderByScanning(p.toLong()) == 0L
    }
    val byBigInteger = sievePrimes(5_000).filter { p ->
        p != 3 && BigInteger.TEN.modPow(nBig, BigInteger.valueOf(p.toLong())) == BigInteger.ONE
    }
    check(byScanning == byBigInteger) { "线性扫描与 BigInteger 判据不一致：$byScanning vs $byBigInteger" }
}

fun main() {
    verifySample()
    repeat(3) { solveBruteForce() }                  // JIT 预热（慢解，预热轮数相应取小）
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
