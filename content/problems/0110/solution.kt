/**
 * Project Euler 110 — Diophantine Reciprocals II（丢番图倒数 II）
 *
 * 思路（与 108 同一族方程，推导直接复用）：1/x + 1/y = 1/n 两边乘 nxy 移项得
 * (x − n)(y − n) = n²。令 a = x − n，则 n² 的每个正因子 a 给出解 (x, y) = (n + a, n + n²/a)，
 * a 与 n²/a 给出同一个解；取 a ≤ n 即 x ≤ y。n² 是完全平方数，只有 a = n 与自身配对，
 * 故不同解个数 = (d(n²) + 1) / 2。n = ∏pᵢ^{eᵢ} 时 d(n²) = ∏(2eᵢ + 1) 恒为奇数，于是
 * 「解个数 > 4×10⁶」⟺ d(n²) > 7 999 999 ⟺ d(n²) ≥ 8 000 001。
 * （题面的 1260 那组样例同理：> 100 ⟺ d(n²) ≥ 201，225 给出 113 个解。）
 *
 * 求最小 n：指数必按非递增顺序落在最小的那几个素数上——若 eᵢ < eⱼ 而 pᵢ < pⱼ，交换两者
 * n 变小而 d(n²) 不变；若用到的素数不是前 k 个，换成更小的未用素数同理。所以候选空间只是
 * 「前 k 个素数上的非递增指数向量」，深度优先枚举 + 「前缀 n ≥ 已知最优解即剪枝」即可。
 * 搜索从贪心上界出发（前 15 个素数各一次，3^15 = 14348907 ≥ 8000001，
 * n = 614889782588491410），一路把最优值压到 9.35×10¹⁵ 量级。
 *
 * 复杂度：时间 O(搜索树节点数)（实测 17411 个节点、6 次最优值更新，每节点 1 次乘法 + 1 次除法），空间 O(k) 递归栈。
 * 所有候选 n 都被剪枝压在贪心上界之下，中间量最大约 6.1×10¹⁷ < `Long` 上限 9.22×10¹⁸，
 * 无需 BigInteger；因子计数单独滚动维护，从不计算会溢出的 n²。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private val FIRST_PRIMES = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53)

/** 目标：解个数 > 4×10⁶ ⟺ d(n²) ≥ 8 000 001。 */
private const val DIVISOR_TARGET = 8_000_001L

/** 搜索过程中不断下降的最优（最小）n。 */
private var bestN = 0L

/** 贪心上界：前 k 个素数各取一次（每项贡献 3），3^k ≥ 8 000 001 即 k = 15。 */
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
