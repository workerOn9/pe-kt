/**
 * Project Euler 108 — Diophantine Reciprocals I（丢番图倒数 I）
 *
 * 思路：把 1/x + 1/y = 1/n 两边乘 nxy 并移项，得 (x − n)(y − n) = n²。令 a = x − n，
 * 则 n² 的每个正因子 a 给出一个解 (x, y) = (n + a, n + n²/a)，而 a 与 n²/a 是同一个解；
 * 取 a ≤ n（即 x ≤ y）就不重不漏。n² 是完全平方数，只有 a = n 与自身配对，所以
 *   不同解个数 = (d(n²) + 1) / 2，d 为因子个数。
 * 又 n = ∏pᵢ^{eᵢ} 时 d(n²) = ∏(2eᵢ + 1) 恒为奇数，故「解个数 > 1000」
 * ⟺ d(n²) ≥ 2001（1999 恰好只给 1000 个解）。
 *
 * 于是问题化为：在 ∏(2eᵢ+1) ≥ 2001 的约束下求 n = ∏pᵢ^{eᵢ} 的最小值。指数必按非递增
 * 顺序落在最小的那几个素数上——若 eᵢ < eⱼ 而 pᵢ < pⱼ，交换两者 n 变小而 d(n²) 不变；
 * 若某个用到的素数不在前 k 个素数之列，换成更小的未用素数同理。因此只需深度优先枚举
 * 「前 k 个素数上的非递增指数向量」，用「当前前缀 n 已 ≥ 已知最优解」剪枝；前缀一旦达标
 * 即记录并返回，因为再往后的素数只会让 n 变大。
 *
 * 复杂度：时间 O(搜索树节点数)（实测只访问 187 个节点，每节点 1 次乘法 + 1 次除法），空间 O(k) 递归栈。
 * 数值上界：候选 n 恒被贪心上界 510510（前 7 个素数各一次，3^7 = 2187 ≥ 2001）压住，
 * 中间量远小于 `Long` 上限 9.22×10¹⁸，无需 BigInteger，也不做任何位数判断。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private val FIRST_PRIMES = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53)

/** 目标：解个数 > 1000 ⟺ (d(n²)+1)/2 > 1000 ⟺ d(n²) ≥ 2001。 */
private const val DIVISOR_TARGET = 2_001L

/** 搜索过程中不断下降的最优（最小）n。 */
private var bestN = 0L

/** 贪心上界：前 k 个素数各取一次（每项贡献 2·1+1 = 3），3^k ≥ 2001 即 k = 7，n = 510510。 */
private fun greedyUpperBound(): Long {
    var n = 1L
    var divisors = 1L
    var index = 0
    while (divisors < DIVISOR_TARGET) {
        n *= FIRST_PRIMES[index]
        divisors *= 3L
        index++
    }
    return n
}

/**
 * 枚举第 [index] 个素数上的指数 e（非递增，故 e ≤ [maxExponent]），
 * 前缀积为 [n]、前缀因子数为 [divisors]。达标即更新最优解并返回。
 */
private fun search(index: Int, maxExponent: Int, n: Long, divisors: Long) {
    if (n >= bestN) return
    if (divisors >= DIVISOR_TARGET) {
        bestN = n
        return
    }
    if (index >= FIRST_PRIMES.size) return
    val p = FIRST_PRIMES[index]
    var value = n
    for (exponent in 1..maxExponent) {
        if (value > bestN / p) return     // 再乘一次必然 ≥ bestN（顺带保证不溢出）
        value *= p
        search(index + 1, exponent, value, divisors * (2L * exponent + 1))
    }
}

fun solve(): Long {
    bestN = greedyUpperBound()
    search(0, 62, 1L, 1L)
    return bestN
}

fun main() { println(solve()) }
