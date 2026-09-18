/**
 * Project Euler 146 — 暴力对照解（全范围顺序扫描，不做剩余类推导）
 *
 * 与优化解的思路差别：优化解先用「小素数剩余类筛」把 1.5×10⁸ 缩到约 4.3 万个候选再判定；
 * 本解不推导任何剩余类，只保留两条最朴素的必要条件 —— n 必须是偶数（否则 n²+1 是大于 2 的
 * 偶数），且 5 | n（否则 n²+1 或 n²+9 是 5 的倍数且大于 5）—— 于是从 10 开始以 10 为步长
 * 顺序扫描全部 1500 万个 n，按定义逐个判定。
 *
 * 判定流程（按定义，不做理论剪枝）：先用 3..149 的素数对六个要求值试除（绝大多数候选在这里
 * 被否掉），六个都通过试除的用确定性 Miller–Rabin 确认素性，最后确认夹在中间的八个奇数
 * n²+5, n²+11, …, n²+25 全为合数。素性判定走 JDK 的 BigInteger.modPow，基集
 * {2, 325, 9375, 28178, 450775, 9780504, 1795265022} 对 64 位整数确定有效，
 * 与优化解手写的 64 位模乘是两条完全独立的实现路径。
 *
 * 复杂度：候选 1.5×10⁷ 个，每个候选期望试除约 32 次（合计约 4.7×10⁸ 次取模），素性判定只
 * 发生在六个值都通过试除的极少数候选上 —— 实测约 0.62 秒，约为优化解的 18.9 倍。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

private const val LIMIT = 150_000_000

/** 必须为素数的六个偏移。 */
private val REQ = intArrayOf(1, 3, 7, 9, 13, 27)

/** 必须为合数的八个中间奇数偏移。 */
private val MID = intArrayOf(5, 11, 15, 17, 19, 21, 23, 25)

/** 试除用的小素数：3 ≤ p < 150 —— 纯按定义做的小因子预筛，不含任何剩余类分析。 */
private val TRIAL: IntArray = run {
    val isP = BooleanArray(150) { true }
    isP[0] = false
    isP[1] = false
    var i = 2
    while (i * i < 150) {
        if (isP[i]) {
            var j = i * i
            while (j < 150) {
                isP[j] = false
                j += i
            }
        }
        i++
    }
    (3 until 150).filter { isP[it] }.toIntArray()
}

/** 小素数试除；返回 true 表示没找到小因子（可能是素数，也可能是大因子合数）。 */
private fun passesTrial(v: Long): Boolean {
    for (p in TRIAL) {
        if (v % p == 0L) return v == p.toLong()
    }
    return true
}

/** 64 位范围内确定有效的 Miller–Rabin 基（Sinclair 给出的 7 元组）。 */
private val MR_BASES_64 = longArrayOf(2, 325, 9375, 28178, 450775, 9780504, 1795265022)

/** 确定性 Miller–Rabin，模幂交给 JDK 的 BigInteger。 */
private fun isPrime(v: Long): Boolean {
    if (v < 2L) return false
    if (v % 2L == 0L) return v == 2L
    val n = BigInteger.valueOf(v)
    val nMinus1 = n.subtract(BigInteger.ONE)
    var d = nMinus1
    var s = 0
    while (!d.testBit(0)) {
        d = d.shiftRight(1)
        s++
    }
    for (b in MR_BASES_64) {
        val a = BigInteger.valueOf(b).mod(n)
        if (a.signum() == 0) continue
        var x = a.modPow(d, n)
        if (x == BigInteger.ONE || x == nMinus1) continue
        var strong = false
        for (i in 1 until s) {
            x = x.multiply(x).mod(n)
            if (x == nMinus1) {
                strong = true
                break
            }
        }
        if (!strong) return false
    }
    return true
}

/** 按定义判定单个 n：六个要求值为素数，八个中间值为合数。 */
private fun isPattern(n: Long): Boolean {
    val n2 = n * n
    for (c in REQ) if (!passesTrial(n2 + c)) return false
    for (c in REQ) if (!isPrime(n2 + c)) return false
    for (c in MID) if (isPrime(n2 + c)) return false
    return true
}

/** 扫描所有 n = 10, 20, 30, … < limit 并累加解。 */
fun solve(limit: Int = LIMIT): Long {
    var sum = 0L
    var n = 10L
    while (n < limit) {
        if (isPattern(n)) sum += n
        n += 10L
    }
    return sum
}

fun verifySample() {
    // 题面：n = 10 时六个数是 101, 103, 107, 109, 113, 127
    val six = REQ.map { 10L * 10 + it }
    check(six == listOf(101L, 103L, 107L, 109L, 113L, 127L)) { "n = 10 处六个数不对：$six" }
    check(isPattern(10L)) { "n = 10 必须是解" }
    // 题面：n = 10 是最小的解；比它小的正整数的六元组都过不了合数检查
    for (n in 1L..9L) check(!isPattern(n)) { "n = $n 不该是解" }
    // 题面：一百万以内所有这样的 n 之和为 1242490
    check(solve(1_000_000) == 1_242_490L) { "solve(10⁶) 应为 1242490，实得 ${solve(1_000_000)}" }
}

fun main() {
    verifySample()
    repeat(2) { solve() }                        // JIT 预热（单轮约 0.6 秒）
    val times = DoubleArray(5)                   // 单轮耗时受机器负载影响波动较大，取中位数
    var answer = 0L
    for (i in times.indices) {
        val start = System.nanoTime()
        answer = solve()
        times[i] = (System.nanoTime() - start) / 1e6
    }
    times.sort()
    System.err.printf("brute: %.4f ms%n", times[times.size / 2])
    println(answer)
}
