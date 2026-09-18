/**
 * Project Euler 131 — Prime Cube Partnership（素数立方伙伴）
 *
 * 思路：要求 n³ + n²p = n²(n + p) 是完全立方数 m³，p 为素数、n 为正整数。
 * 记 g = gcd(n, n + p) = gcd(n, p)。p 素数 ⇒ g ∈ {1, p}：
 *
 *   · g = 1：n² 与 n + p 互素，而二者之积是立方数，故 n² 与 n + p 各自是立方数。
 *     写 n = a³（n² 为立方数 ⇔ n 为立方数）、n + p = b³，则
 *     p = b³ − a³ = (b − a)(b² + ab + a²)。p 为素数迫使 b − a = 1（否则小因子 ≥ 2
 *     且大因子也 > 1），于是
 *         p = 3a² + 3a + 1，n = a³，
 *     解唯一（a 由 p 唯一确定），完全立方数恰为 b³ = (a + 1)³。
 *
 *   · p | n：写 n = pk、k ≥ 1，则 n²(n + p) = p³k²(k + 1)，需 k²(k + 1) 为立方数。
 *     k² 与 k + 1 互素，故各自是立方数：k = j³ 且 j³ + 1 为立方数。相邻两立方数之间
 *     没有第三个立方数（j³ < j³ + 1 < (j + 1)³ 对 j ≥ 1 成立），无解。
 *
 * 所以任务化为：统计形如 3a² + 3a + 1（a ≥ 1）的素数中小于 10⁶ 的个数。
 * 由 3a² + 3a + 1 < 10⁶ 得 a ≤ 576，只需筛一张 10⁶ 以内的素数表再查 576 次。
 *
 * 复杂度：筛法 O(N log log N)（N = 10⁶），之后 576 次常数查询；空间 O(N)。
 * 所有中间量（a ≤ 576，3a² + 3a + 1 ≤ 997057）都远在 Int 范围内。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

/** 埃氏筛：isPrime[x] 为 true 表示 x 是素数（x ≥ 2）。 */
fun sieve(limit: Int): BooleanArray {
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
    return isPrime
}

/** 小于 limit 的素数中，形如 3a² + 3a + 1（a ≥ 1）的个数。 */
fun solve(limit: Int = 1_000_000): Long {
    val isPrime = sieve(limit)
    var count = 0L
    var a = 1
    while (true) {
        val p = 3 * a * a + 3 * a + 1        // 恒为奇数且 ≡ 1 (mod 3)，只需查素数表
        if (p >= limit) break
        if (isPrime[p]) count++
        a++
    }
    return count
}

fun verifySample() {
    // 题面：一百以内恰有四个这样的素数
    check(solve(100) == 4L) { "一百以内应有 4 个，实得 ${solve(100)}" }
    // 题面例子的数值：p = 19（a = 2）时应为 8³ + 8² × 19 = 512 + 1216 = 1728 = 12³
    check(8L * 8 * 8 + 8L * 8 * 19 == 12L * 12 * 12)

    // 定义级验证：对每个被计数的素数列出其唯一解 n = a³，直接按定义核对方程
    // n³ + n²p = m³（用 BigInteger 算，n 最大到 576³ ≈ 1.9×10⁸，n³ 超出 Long）。
    val isPrime = sieve(1_000_000)
    val checked = ArrayList<Int>()
    var a = 1
    while (true) {
        val p = 3 * a * a + 3 * a + 1
        if (p >= 1_000_000) break
        if (isPrime[p]) {
            checked.add(p)
            val n = BigInteger.valueOf(a.toLong()).pow(3)                     // n = a³
            val lhs = n.pow(3).add(n.pow(2).multiply(BigInteger.valueOf(p.toLong())))
            val m = BigInteger.valueOf(a.toLong()).pow(2).multiply(BigInteger.valueOf(a.toLong() + 1L))
            check(m.pow(3) == lhs) { "p = $p 的定义级验证失败" }              // m = a²(a+1)
        }
        a++
    }
    check(checked.subList(0, 4) == listOf(7, 19, 37, 61)) { "一百以内的四个应为 7, 19, 37, 61：$checked" }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
