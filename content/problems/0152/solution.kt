/**
 * Project Euler 152 — Sums of Square Reciprocals（平方倒数之和）
 *
 * 思路：把问题转化为子集和问题。目标值为 1/2，每个候选整数 i 的贡献为 1/i²。
 * 需要用 BigInteger 来表示有理数以避免浮点误差：设 LCM(1²..80²) 为公分母，
 * 每个贡献为整数 numerator = LCM / i²。目标 target = LCM / 2。
 *
 * 优化：用带剪枝的 DFS（回溯）。从大到小枚举候选，使用以下剪枝：
 *  - 若当前剩余目标小于 0，停止（已超）；
 *  - 若当前剩余目标大于后续所有候选贡献之和，停止（不够）；
 *  - 若当前剩余目标小于最小候选贡献，停止。
 *
 * 复杂度：最坏 2^79 个子集，但剪枝后实际可解（答案仅约 10^6 量级）。
 */

import java.math.BigInteger

val LIMIT = 80
val TARGET_NUM = 1  // 1/2

/** 预计算公分母 LCM(1²..LIMIT²) 与各项分子贡献。 */
fun prepare(): Pair<BigInteger, List<BigInteger>> {
    // LCM of i^2 for i in 2..LIMIT
    var lcm = BigInteger.ONE
    for (i in 2..LIMIT) {
        val i2 = BigInteger.valueOf((i * i).toLong())
        lcm = lcm(lcm, i2)
    }
    val numerators = (2..LIMIT).map { i ->
        val i2 = BigInteger.valueOf((i * i).toLong())
        lcm / i2
    }
    return lcm to numerators
}

fun lcm(a: BigInteger, b: BigInteger): BigInteger {
    val gcd = a.gcd(b)
    return a * b / gcd
}

fun solve(): Long {
    val (lcmDenom, numerators) = prepare()
    val target = lcmDenom / BigInteger.TWO  // LCM / 2
    
    // 后缀和表：suffixSum[k] = sum(numerators[k..])
    val suffixSum = Array(numerators.size + 1) { BigInteger.ZERO }
    for (k in numerators.size - 1 downTo 0) {
        suffixSum[k] = suffixSum[k + 1] + numerators[k]
    }
    
    var count = 0L
    
    fun dfs(idx: Int, remaining: BigInteger, usedCount: Int) {
        if (remaining == BigInteger.ZERO) {
            count++
            return
        }
        if (idx >= numerators.size) return
        if (remaining < BigInteger.ZERO) return
        
        // 剪枝：若剩余目标 > 后续所有候选贡献之和，无法达成
        if (remaining > suffixSum[idx]) return
        
        // 选当前项（若不超过剩余）
        if (numerators[idx] <= remaining) {
            dfs(idx + 1, remaining - numerators[idx], usedCount + 1)
        }
        // 不选当前项
        dfs(idx + 1, remaining, usedCount)
    }
    
    dfs(0, target, 0)
    return count
}

fun main() {
    repeat(3) { solve() }  // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
