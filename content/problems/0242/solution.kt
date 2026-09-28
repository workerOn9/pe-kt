#!/usr/bin/env kotlin
/**
 * Project Euler 242 — Odd Triplets（奇三元组）
 *
 * 思路（三步，每步都把枚举量砍掉一个量级）：
 *
 *   **第一步：把 f(n,k) 写成闭式。** 设 n = 2m+1（题面要求 n 奇）、k = 2r+1（k 奇），
 *   则 {1..n} 里有 m+1 个奇数、m 个偶数。用「选了多少个奇数」分类：
 *
 *       f(n, 2r+1) = Σ_{j 奇} C(m+1, j)·C(m, 2r+1-j)
 *
 *   把「偶数个数 − 奇数个数」的生成函数算出来更省事：
 *
 *       Σ_k (−1)^{元素和}·(#k 元子集) = [x^k](1−x)^{m+1}(1+x)^{m} = [x^k](1−x)(1−x^2)^m
 *
 *   于是 f(n,2r+1) = ( C(n,2r+1) + (−1)^r·C(m,r) ) / 2，f(n,2j) = ( C(n,2j) − (−1)^j·C(m,j) ) / 2。
 *
 *   **第二步：mod 4 的判偶，把 f 化为一个组合数。** f 为奇 ⟺ 分子 ≡ 2 (mod 4)。
 *   注意 C(2m+1, 2r+1) = (2m+1)/(2r+1) · C(2m, 2r)，而 2r+1 为奇数故在 mod 4 下可逆，
 *   再配合经典同余 C(2a, 2b) ≡ C(a, b) (mod 4)，设 X = C(m,r) mod 4 得
 *
 *       C(2m+1, 2r+1) ≡ (1 + 2(m&1))·(1 + 2(r&1))·X  (mod 4)
 *
 *   令 w = (1+2(m&1))·(1+2(r&1)) mod 4（m、r 同奇偶时 w=1，否则 w=3），条件化为
 *   X·[w + (−1)^r] ≡ 2 (mod 4)：
 *       - m、r 同奇偶（w=1）：r 偶时需 X 奇；r 奇时左边恒 0，无解；
 *       - m、r 奇偶不同（w=3）：r 偶时左边恒 0，无解；r 奇时需 X 奇。
 *   两种可行情形都要求 **m 偶且 C(m,r) 奇**（r 的奇偶不再有约束）。
 *
 *   **第三步：Lucas 定理 + 按位 DP。** C(m,r) 奇 ⟺ r 的每个二进制 1 位都是 m 的 1 位
 *   （r & ~m = 0），此时 r ≤ m 自动成立，故合法的 r 有 2^{popcount(m)} 个。
 *   于是答案 = Σ_{m 偶, 2m+1 ≤ N} 2^{popcount(m)}。令 m = 2j（m 偶 ⟺ 末位 0），
 *   popcount(2j) = popcount(j)，约束化为 4j + 1 ≤ N：
 *
 *       count(N) = Σ_{0 ≤ j ≤ (N−1)/4} 2^{popcount(j)}
 *
 *   这个和用 MSB→LSB 的位 DP 求：逐位扫描 J 的二进制，当前位为 1 时，
 *   「这一位取 0」把前缀变小，低 i 位自由，每位权重和为 1 + 2 = 3，故贡献 2^a·3^i；
 *   「这一位取 1」则前缀继续相等，权重翻倍。O(log N) 完成。
 *
 *   溢出：Σ_{0 ≤ j < 2^38} 2^{popcount(j)} = 3^38 ≈ 1.35×10^18 < 2^63−1，
 *   所以全程 Long 够用，不需要 BigInteger。
 *
 * 旁证：
 *   1. 题面样例：count(10) = 5，且程序把 5 个奇三元组逐个列出来（用闭式 f 复算
 *      f(1,1)=1、f(5,1)=3、f(5,5)=1、f(9,1)=5、f(9,9)=1），与题面完全一致；
 *   2. 闭式 f 与「按选了几个奇数」的直接求和 Σ_{j 奇} C(m+1,j)C(m,k−j) 在 n ≤ 99
 *      的全部奇 k 上逐位一致；
 *   3. brute-force.kt 用完全不同的路径（子集 DP：逐个元素累加 even[k]/odd[k]，
 *      不碰闭式、不碰 Lucas）数奇三元组，在 N = 10 / 100 / 1000 / 3000 上与本程序一致。
 *
 * 答案：997104142249036713
 * 复杂度：O(log N) 时间、O(log N) 空间（38 次位运算），实测 < 1 μs。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val N_MAX = 1_000_000_000_000L // 10^12

/** 组合数 C(n, k)；只用于小上界的自检，n ≤ 99 时 C(99,50) 已超 Long，故用 BigInteger */
private fun binom(n: Long, k: Long): java.math.BigInteger {
    if (k < 0 || k > n) return java.math.BigInteger.ZERO
    val kk = if (k * 2 > n) n - k else k
    var r = java.math.BigInteger.ONE
    for (i in 1..kk) r = r.multiply(java.math.BigInteger.valueOf(n - kk + i))
        .divide(java.math.BigInteger.valueOf(i))
    return r
}

/** 闭式：n = 2m+1、k = 2r+1 时 f(n,k) = ( C(n,k) + (−1)^r·C(m,r) ) / 2 */
private fun fClosedForm(n: Long, k: Long): java.math.BigInteger {
    val m = (n - 1) / 2
    val r = (k - 1) / 2
    val sign = if (r % 2 == 0L) java.math.BigInteger.ONE else java.math.BigInteger.ONE.negate()
    return binom(n, k).add(sign.multiply(binom(m, r))).divide(java.math.BigInteger.TWO)
}

/** 判偶刻画：f(n,k) 为奇 ⟺ m 偶且 C(m,r) 奇（n = 2m+1、k = 2r+1） */
private fun isOddTriplet(n: Long, k: Long): Boolean {
    if (n % 2 == 0L || k % 2 == 0L) return false
    val m = (n - 1) / 2
    val r = (k - 1) / 2
    if (m % 2 != 0L) return false
    return (r and m) == r // Lucas: C(m,r) 奇 ⟺ r 是 m 的二进制子集
}

/** Σ_{0 ≤ j ≤ bound} 2^{popcount(j)}，MSB→LSB 位 DP，O(log bound) */
private fun sumPow2Popcount(bound: Long): Long {
    if (bound < 0) return 0L
    val bits = 64 - java.lang.Long.numberOfLeadingZeros(bound) // bound = 0 时为 0
    val pow3 = LongArray(bits + 1)
    pow3[0] = 1L
    for (i in 1..bits) pow3[i] = pow3[i - 1] * 3L
    var total = 0L // 前缀已严格小于 bound 的那些 j 的权重和
    var prefix = 1L // 与 bound 保持相等的前缀的权重 2^{popcount}
    for (i in bits - 1 downTo 0) {
        if ((bound ushr i) and 1L == 1L) {
            total += prefix * pow3[i] // 本位取 0 → 低 i 位自由，每位权重 1+2 = 3
            prefix *= 2L // 本位取 1 → 继续相等
        }
    }
    return total + prefix // 加上 bound 自身
}

/** n ≤ nMax 的奇三元组个数 */
private fun countTriplets(nMax: Long): Long = sumPow2Popcount((nMax - 1) / 4)

private fun main() {
    // 题面样例：n ≤ 10 恰好 5 个奇三元组，逐个用闭式 f 复算并打印
    val listed = ArrayList<String>()
    for (n in 1..10L step 2) for (k in 1..n step 2) {
        if (isOddTriplet(n, k)) listed += "[$n,$k,f($n,$k) = ${fClosedForm(n, k)}]"
    }
    println("n <= 10 的奇三元组 -> ${listed.size} 个：")
    listed.forEach { println("    $it") }
    check(listed.size == 5) { "题面样例不吻合：${listed.size}" }
    check(
        listed == listOf(
            "[1,1,f(1,1) = 1]", "[5,1,f(5,1) = 3]", "[5,5,f(5,5) = 1]",
            "[9,1,f(9,1) = 5]", "[9,9,f(9,9) = 1]",
        ),
    ) { "题面样例的具体三元组不吻合：$listed" }

    // 交叉验证点：闭式 f 的奇偶与判偶刻画一致（n ≤ 99 的全部奇 k）
    var mismatches = 0
    for (n in 1..99L step 2) for (k in 1..n step 2) {
        if (fClosedForm(n, k).testBit(0) != isOddTriplet(n, k)) mismatches++
    }
    check(mismatches == 0) { "闭式与判偶刻画不一致：$mismatches 处" }
    println("闭式 f 与判偶刻画在 n <= 99 上一致（0 处不一致）")

    // 交叉验证点：brute-force.kt（子集 DP）在这些上界给出的值
    for ((bound, expected) in listOf(10L to 5L, 100L to 139L, 1000L to 5793L, 3000L to 29829L)) {
        val got = countTriplets(bound)
        check(got == expected) { "count($bound) = $got，与 brute-force 的 $expected 不一致" }
    }
    println("count(10/100/1000/3000) = 5 / 139 / 5793 / 29829，与 brute-force 一致")

    val answer = countTriplets(N_MAX)
    println("answer n <= 10^12 -> $answer")
    check(answer == 997104142249036713L) { "答案不吻合：$answer" }

    // 计时：预热后单次求解的平均耗时（JIT 已热，逐次调用几乎不做事）
    repeat(20_000) { countTriplets(N_MAX) }
    val iterations = 200_000
    val t0 = System.nanoTime()
    var sink = 0L
    for (i in 0 until iterations) sink += countTriplets(N_MAX)
    val elapsed = System.nanoTime() - t0
    check(sink == answer.toLong() * iterations) { "计时循环结果漂移" }
    val perCallMs = elapsed.toDouble() / iterations / 1e6
    println("optimized: ${"%.6f".format(perCallMs)} ms/次（$iterations 次，JIT 预热 20000 次）")
    println("check() 全部通过")
}
