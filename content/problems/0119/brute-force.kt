/**
 * Project Euler 119 — 暴力解（教学对比用）
 *
 * 与 solution.kt 判定同一个性质「n = digitsum(n)^k，k ≥ 2，且 n 至少两位」，但走两条
 * 与 solution.kt 结构都不同的路径，互为独立对照：
 *
 * 路径 A（真暴力）：不看任何数论结构，把 10..10⁸ 的每个整数都拿出来，先算数字和 s，
 * 再用「反复除以 s，必须整除且最终商为 1」判定 n 是否为 s 的幂。这是题面性质的字面翻译，
 * 代价是约 10⁸ 次数字和扫描（solution.kt 只检查 1421 个幂），覆盖前 15 项。
 * 除法判幂与 solution.kt 的「枚举 s^k 再比数字和」是方向完全相反的两种算法。
 *
 * 路径 B（幂枚举 + 多精度）：外层枚举指数 k、内层枚举底数 s，用 BigInteger.pow 生成
 * s^k，数字和走十进制字符串。循环次序、幂的生成方式、数字和的实现三者全部与
 * solution.kt（外层底数、Long 连乘、除模求数字和）不同，因此连「上界算错」这类失误
 * 都不容易同时出现在两边。
 *
 * 两条路径都用题面锚点 a₂ = 512、a₁₀ = 614656 做运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 数字和：十进制字符串求和（与 solution.kt 的除模实现相互独立）。 */
fun digitSumString(value: BigInteger): Int =
    value.toString().sumOf { ch -> ch - '0' }

/** n 是否为 s 的某个 ≥ 2 次幂：反复整除 s，最终必须恰好除到 1。 */
fun isPowerOfDigitSum(n: Int, s: Int): Boolean {
    if (s < 2) return false
    var rest = n
    var exponent = 0
    while (rest % s == 0) {
        rest /= s
        exponent++
    }
    return rest == 1 && exponent >= 2
}

/** 数字和（整数除模，用于暴力扫描）。 */
fun digitSumInt(value: Int): Int {
    var rest = value
    var sum = 0
    while (rest > 0) {
        sum += rest % 10
        rest /= 10
    }
    return sum
}

/** 路径 A：逐个整数暴力检查 [10, bound)，返回命中的升序列表。 */
fun scanAllIntegers(bound: Int): List<Int> =
    (10 until bound).filter { n -> isPowerOfDigitSum(n, digitSumInt(n)) }

/** 路径 B：枚举 (s, k)，BigInteger 幂 + 字符串数字和，返回 limit 以内命中的升序列表。 */
fun enumeratePowers(limit: Long, maxBase: Int): List<Long> {
    val ceiling = BigInteger.valueOf(limit)
    val found = sortedSetOf<Long>()
    for (exponent in 2..64) {
        for (base in 2..maxBase) {
            val power = BigInteger.valueOf(base.toLong()).pow(exponent)
            if (power >= ceiling) break
            if (digitSumString(power) == base) found.add(power.longValueExact())
        }
    }
    return found.toList()
}

fun main() {
    val limit = 1_000_000_000_000_000L
    val powers = enumeratePowers(limit, 200)
    check(powers[1] == 512L) { "题面给出 a₂ = 512，实际 ${powers[1]}" }
    check(powers[9] == 614656L) { "题面给出 a₁₀ = 614656，实际 ${powers[9]}" }
    println("路径 B（BigInteger 幂枚举）前 30 项 = ${powers.take(30)}")
    println("路径 B 的 a₃₀ = ${powers[29]}")

    val scanned = scanAllIntegers(100_000_000)
    println("路径 A（逐个整数扫描至 10⁸）命中 ${scanned.size} 项 = $scanned")
    check(scanned == powers.filter { it < 100_000_000L }.map { it.toInt() }) {
        "两条路径在 10⁸ 以内不一致：A=${scanned}，B=${powers.filter { it < 100_000_000L }}"
    }
    println("A、B 两路在 10⁸ 以内完全一致")
}
