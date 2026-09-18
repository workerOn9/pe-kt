/**
 * Project Euler 134 — Prime Pair Connection（素数对连接）
 *
 * 思路：设 p1 有 d 位十进制数字，则「末 d 位恰为 p1」的正整数恰好是等差数列
 *   n = p1 + k·10^d,   k = 0, 1, 2, …
 * 题目还要求 p2 | n，代入即
 *   p1 + k·10^d ≡ 0 (mod p2)  ⟺  k ≡ −p1·(10^d)^(−1) (mod p2)。
 * 本题范围内 p1 ≥ 5，故 p2 > p1 ≥ 5，p2 与 10 互素，进而 gcd(10^d, p2) = 1，逆元存在；
 * 由费马小定理 (10^d)^(p2−2) ≡ (10^d)^(−1) (mod p2)，用快速幂求，O(log p2)。
 * 再把 k 归约到最小非负剩余 k0 ∈ [0, p2)：n 关于 k 严格递增，所以 k0 给出的 n 就是
 * 全体候选中的最小者。k0 = 0 不会发生——那要求 p1 ≡ 0 (mod p2)，而 0 < p1 < p2。
 * 题面点名的例外 (p1, p2) = (3, 5) 正落在互素条件之外：10 ≡ 0 (mod 5)，
 * 一切候选 n ≡ 3 (mod 5) 都不被 5 整除，无解。
 *
 * 复杂度：埃氏筛 O(N log log N)（N = 10^6 + 20，为 p1 = 999983 取到下一个素数 1000003）；
 * 每个素数对一次快速幂 O(log p2)，共 π(10^6) − 2 = 78496 对，
 * 总时间 O(N log log N + π(N) log N)，空间 O(N)。
 * 单项上界 n < p2·10^6 + p1 < 1.01×10^12（实测最大 995751997147），总和 1.86×10^16，
 * Long 足够，无需 BigInteger。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 埃氏筛：返回所有 ≤ limit 的素数。 */
fun sievePrimes(limit: Int): IntArray {
    val isPrime = BooleanArray(limit + 1) { true }
    isPrime[0] = false
    isPrime[1] = false
    var p = 2
    while (p.toLong() * p <= limit) {
        if (isPrime[p]) {
            var multiple = p * p
            while (multiple <= limit) {
                isPrime[multiple] = false
                multiple += p
            }
        }
        p++
    }
    var count = 0
    for (v in 2..limit) if (isPrime[v]) count++
    val primes = IntArray(count)
    var index = 0
    for (v in 2..limit) if (isPrime[v]) primes[index++] = v
    return primes
}

/** 10^exp，用乘法累加，不经过字符串。 */
fun pow10(exp: Int): Long {
    var result = 1L
    repeat(exp) { result *= 10 }
    return result
}

/** v 的十进制位数：阈值比较，禁止用 toString() 数位数。 */
fun digitCount(v: Long): Int {
    var digits = 1
    var threshold = 10L
    while (v >= threshold) {
        digits++
        threshold *= 10
    }
    return digits
}

/** 快速幂：base^exp mod modulus。 */
fun modPow(base: Long, exp: Long, modulus: Long): Long {
    var b = base % modulus
    var e = exp
    var result = 1L
    while (e > 0) {
        if (e and 1L == 1L) result = result * b % modulus
        b = b * b % modulus
        e = e shr 1
    }
    return result
}

/** 费马小定理求逆元：a^(m−2) mod m（m 为素数且 gcd(a, m) = 1）。 */
fun modInverseFermat(a: Long, prime: Long): Long = modPow(a, prime - 2, prime)

/** 相邻素数对 (p1, p2) 的最小连接数 S：末 d 位为 p1 且被 p2 整除的最小 n。 */
fun connection(p1: Long, p2: Long): Long {
    val modulus = pow10(digitCount(p1))            // 10^d
    val inv = modInverseFermat(modulus % p2, p2)   // (10^d)^(−1) mod p2
    val k = (p2 - (p1 % p2) * inv % p2) % p2       // 最小非负剩余：−p1·inv mod p2
    return p1 + k * modulus
}

/** 所有 5 ≤ p1 ≤ limit 的相邻素数对的 S 之和。 */
fun solve(limit: Long = 1_000_000L): Long {
    val primes = sievePrimes((limit + 20).toInt())  // 上限之外还要取到 p1 的下一个素数
    var sum = 0L
    for (i in primes.indices) {
        val p1 = primes[i]
        if (p1 < 5) continue
        if (p1 > limit) break
        sum += connection(p1.toLong(), primes[i + 1].toLong())
    }
    return sum
}

/**
 * 用定义直接校验收到的 n 确实是对的：
 * 末 d 位是 p1、被 p2 整除、且等差数列中它之前的那一项不满足整除（即最小）。
 */
fun checkByDefinition(p1: Int, p2: Int, n: Long) {
    val modulus = pow10(digitCount(p1.toLong()))
    check(n % modulus == p1 % modulus) { "($p1, $p2)：$n 末位不是 $p1" }
    check(n % p2 == 0L) { "($p1, $p2)：$n 不被 $p2 整除" }
    check(n < p1 + modulus || (n - modulus) % p2 != 0L) { "($p1, $p2)：$n 之前还有更小的候选" }
}

fun verifySample() {
    // 题面样例：p1 = 19, p2 = 23 时最小者是 1219
    check(connection(19, 23) == 1219L) { "connection(19, 23) = ${connection(19, 23)}，应为 1219" }
    checkByDefinition(19, 23, 1219L)

    // 前几对相邻素数按定义手算/试除得到的最小值
    val expected = mapOf(
        5 to 7 to 35L,      // 末位 5、被 7 整除：35
        7 to 11 to 77L,     // 末位 7、被 11 整除：77
        11 to 13 to 611L,   // 末两位 11、被 13 整除：611
        13 to 17 to 1513L,  // 末两位 13、被 17 整除：1513
        17 to 19 to 817L,   // 末两位 17、被 19 整除：817
        19 to 23 to 1219L,
        23 to 29 to 2523L,  // 末两位 23、被 29 整除：2523 = 29 × 87
    )
    for ((key, value) in expected) {
        val (p1, p2) = key
        check(connection(p1.toLong(), p2.toLong()) == value) { "($p1, $p2) 应为 $value" }
        checkByDefinition(p1, p2, value)
    }

    // solve(limit) 累加 5 ≤ p1 ≤ limit 的对：端点本身要计入，故每次加一对
    check(solve(13) == 35L + 77L + 611L + 1513L)                          // 4 对
    check(solve(17) == 35L + 77L + 611L + 1513L + 817L)                   // 多出 (17, 19)
    check(solve(19) == 35L + 77L + 611L + 1513L + 817L + 1219L)           // 多出 (19, 23)
    check(solve(23) == 35L + 77L + 611L + 1513L + 817L + 1219L + 2523L)   // 多出 (23, 29)

    // 题面点名的例外 (3, 5)：候选 n = 3 + 10k 恒 ≡ 3 (mod 5)，无解
    for (k in 0L until 5L) check((3L + 10L * k) % 5L != 0L)
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
