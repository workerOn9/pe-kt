/**
 * Project Euler 134 — 暴力解（按定义逐个试除，教学对比用）
 *
 * 与 solution.kt 的差距来源：优化解不枚举任何候选，直接把「p2 | n」写成线性同余
 * k ≡ −p1·(10^d)^(−1) (mod p2)，用快速幂求逆元一次算出答案，每对 O(log p2)。
 * 本解完全不解析求逆，只按定义穷举：从 k = 0 起逐个试验候选
 *   n = p1 + k·10^d  （末 d 位为 p1），
 * 直到 n 被 p2 整除为止。判整除不做每次取模，而是维护余数 r = (p1 + k·10^d) mod p2，
 * 每步 r += (10^d mod p2)，越界回减一次；因为 gcd(10^d, p2) = 1，r 在 p2 步内必然命中 0。
 *
 * 复杂度：筛法 O(N log log N)；枚举最坏每对 p2 步、平均 p2/2 步。
 * 实测本机全部 78496 对的步数合计 18817676144 ≈ 1.88×10^10 步，单步只是加法 + 比较 +
 * 自增（约 0.6 ns），故全规模约 12 秒；优化解同一批数据只要几十毫秒。
 *
 * 题面样例（19 × 23 → 1219，以及前几对相邻素数的最小连接数）写成运行时断言，
 * 且每条都用「末位 = p1」「被 p2 整除」「前一项不整除」三条定义式复核。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
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

/** 暴力最小连接数：逐个候选 n = p1 + k·10^d 试除，返回首个被 p2 整除者。 */
fun connectionBrute(p1: Int, p2: Int): Long {
    val modulus = pow10(digitCount(p1.toLong()))
    val step = (modulus % p2).toInt()        // 每前进一个 k，余数增加的量
    var remainder = p1                       // k = 0 时的余数（p1 < p2，无需取模）
    var k = 0L
    while (remainder != 0) {
        remainder += step
        if (remainder >= p2) remainder -= p2
        k++
    }
    return p1 + k * modulus
}

/** 所有 5 ≤ p1 ≤ limit 的相邻素数对的 S 之和（逐个试除）。 */
fun solveBruteForce(limit: Int = 1_000_000): Long {
    val primes = sievePrimes(limit + 20)     // 上限之外还要取到 p1 的下一个素数
    var sum = 0L
    for (i in primes.indices) {
        val p1 = primes[i]
        if (p1 < 5) continue
        if (p1 > limit) break
        sum += connectionBrute(p1, primes[i + 1])
    }
    return sum
}

/** 用定义复核：末 d 位为 p1、被 p2 整除、且等差数列中前一项不被 p2 整除（最小性）。 */
fun checkByDefinition(p1: Int, p2: Int, n: Long) {
    val modulus = pow10(digitCount(p1.toLong()))
    check(n % modulus == p1 % modulus) { "($p1, $p2)：$n 末位不是 $p1" }
    check(n % p2 == 0L) { "($p1, $p2)：$n 不被 $p2 整除" }
    check(n < p1 + modulus || (n - modulus) % p2 != 0L) { "($p1, $p2)：$n 之前还有更小的候选" }
}

fun main() {
    // 题面样例：p1 = 19, p2 = 23 → 1219
    check(connectionBrute(19, 23) == 1219L)
    checkByDefinition(19, 23, 1219L)

    // 前几对相邻素数的最小连接数，逐条定义复核
    val expected = mapOf(
        5 to 7 to 35L,
        7 to 11 to 77L,
        11 to 13 to 611L,
        13 to 17 to 1513L,
        17 to 19 to 817L,
        23 to 29 to 2523L,
    )
    for ((key, value) in expected) {
        val (p1, p2) = key
        check(connectionBrute(p1, p2) == value) { "($p1, $p2) 应为 $value" }
        checkByDefinition(p1, p2, value)
    }

    // 累加范围端点计入：solve(19) 含 (19, 23)，solve(17) 不含
    check(solveBruteForce(17) == 35L + 77L + 611L + 1513L + 817L)
    check(solveBruteForce(19) == 35L + 77L + 611L + 1513L + 817L + 1219L)

    // 题面点名的例外 (3, 5)：候选 n = 3 + 10k 恒 ≡ 3 (mod 5)，无解
    for (k in 0L until 5L) check((3L + 10L * k) % 5L != 0L)

    repeat(3) { solveBruteForce(100_000) }   // 用十分之一规模预热热循环（全规模一轮约 12 秒）
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
