/**
 * Project Euler 133 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的思路不同：优化解走「乘法阶」这条理论路线——求出 ord₉ₚ(10) 后判它是否
 * 形如 2^a·5^b。本解完全不用阶、不用 λ 函数，直接按定义验证整除性：
 * 由 p | R(k) ⟺ 9p | 10^k − 1，对每个素数 p 与 n = 1, 2, 3, … 用 BigInteger.modPow
 * 直接算 10^(10^n) mod 9p，命中 1 就说明 R(10^n) 被 p 整除。查表式地一遍遍做
 * 大数模幂，不用任何代数结构，这正是它与优化解的差距来源。
 *
 * n 的上界：阶不会超过单位群大小 φ(9p) < 9·10⁵ < 2²⁰。若 10^(10^n) ≡ 1 对某个 n 成立，
 * 即 ord | 10^n = 2ⁿ5ⁿ，则序中 2 的指数 a ≤ 19，取 n = max(a, b) ≤ 19 就能命中；
 * 所以搜到 n = 20 足够，绝无漏判。
 *
 * 复杂度：筛法 O(N log log N)（N = 10⁵），主体是 π(10⁵) ≈ 9592 个素数 × 最多 20 次
 * 67 位指数的 BigInteger 模幂，总计 O(π(N)·log(10²⁰)·M(m))，M 为多精度乘法代价。
 *
 * 题面样例（一百以内只有 11、17、41、73 能整除某个 R(10ⁿ)；17 | R(10⁴) 但不整除 R(10³)）
 * 全部写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 埃氏筛：返回所有小于 limit 的素数列表。 */
fun primesBelow(limit: Int): List<Int> {
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
    val primes = ArrayList<Int>()
    for (x in 2 until limit) if (isPrime[x]) primes.add(x)
    return primes
}

/** 搜索深度：见文件头，n ≤ 19 足够，取 20 留余量。 */
const val MAX_N = 20

/**
 * 按定义判断素数 p 是否可能整除某个 R(10^n)。
 * 逐 n 硬算 10^(10^n) mod 9p（BigInteger 直接做大数模幂），等于 1 即可整除。
 */
fun canBeFactorOfSomeRepunit(p: Int): Boolean {
    if (p == 2 || p == 5) return false                  // R(k) 是奇数且末位恒为 1，与 2、5 互素
    val m = BigInteger.valueOf(9L * p)                  // p | R(k) ⟺ 9p | 10^k − 1
    val base = BigInteger.TEN
    var exponent = BigInteger.ONE                       // 依次取 10¹, 10², …, 10²⁰
    for (n in 1..MAX_N) {
        exponent = exponent.multiply(BigInteger.TEN)
        if (base.modPow(exponent, m) == BigInteger.ONE) return true
    }
    return false
}

/** 小于 limit 的素数中「永远不是 R(10^n) 因子」的那些之和。 */
fun solveBruteForce(limit: Int = 100_000): Long =
    primesBelow(limit).filterNot { canBeFactorOfSomeRepunit(it) }.sumOf { it.toLong() }

fun main() {
    // 题面：一百以内能整除某个 R(10ⁿ) 的素数只有 11、17、41、73
    val canBe = primesBelow(100).filter { canBeFactorOfSomeRepunit(it) }
    check(canBe == listOf(11, 17, 41, 73)) { "一百以内应为 [11, 17, 41, 73]，实得 $canBe" }
    System.err.println("一百以内可整除者：$canBe")       // 诊断信息走 stderr，stdout 只留答案

    // 题面：R(10)、R(100)、R(1000) 不被 17 整除，R(10000) 被 17 整除
    val seventeen = BigInteger.valueOf(17)
    fun repunit(len: Int) = BigInteger.TEN.pow(len).subtract(BigInteger.ONE).divide(BigInteger.valueOf(9))
    check(repunit(10).mod(seventeen) != BigInteger.ZERO)
    check(repunit(1000).mod(seventeen) != BigInteger.ZERO)
    check(repunit(10_000).mod(seventeen) == BigInteger.ZERO)

    // 题面：19 永远不是因子——按定义逐 n 验到 10¹⁹ 都不整除
    val m19 = BigInteger.valueOf(171)                   // 9 · 19
    var exponent = BigInteger.TEN
    for (n in 1..19) {
        check(BigInteger.TEN.modPow(exponent, m19) != BigInteger.ONE) { "n = $n 时不该整除" }
        exponent = exponent.multiply(BigInteger.TEN)
    }

    check(solveBruteForce(4) == 5L)                     // 2、3
    check(solveBruteForce(11) == 17L)                   // 2、3、5、7（11 是因子）

    repeat(3) { solveBruteForce() }                     // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
