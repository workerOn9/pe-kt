/**
 * Project Euler 137 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的差距来源：优化解先做数论归约——把「A_F(x) 是正整数」化成 Pell 型方程
 * u² − 5m² = −4，只在 Lucas 下标上做 15 次二项递推，属于「先归约、后枚举抽象下标」；
 * 本解不做任何 Pell / Lucas / 斐波那契归约，直接按题面定义在**有理性**上搜索：
 * 把 x 写成既约分数 p/q，闭式和给出
 *      A_F(p/q) = pq / (q² − pq − p²)。
 * 分母为正要求 p² + pq < q²，即 p < (√5−1)/2 · q ≈ 0.618 q；在此范围内枚举 q 与 p
 * 是二次量级的搜索（q ≤ 2×10⁶ 时约 1.2×10¹² 对，跑不完）。于是对每个分母 q 解二次方程
 *      p² + qp − (q² − 1) = 0  ⇒  p = ( √(5q² − 4) − q ) / 2，
 * 用整数开方判定 5q² − 4 是否完全平方，回代验证 q² − pq − p² = 1 后得到金块 n = pq。
 * 这条捷径只用了一次「既约分数下分母与 pq 互素」的观察，其余全是整数开方的穷举；
 * 它的正确性由下面 verifySample 里**没有用该观察**的朴素双重循环在 q ≤ 20000 上验证。
 *
 * 复杂度：O(QMAX) 次整数开方 + O(1) 回代，空间 O(1)（只存命中的金块）。
 * QMAX = 2×10⁶ 是刻意的边界：第 15 个金块的分母是 F₃₁ = 1346269，恰好落在界内；
 * 第 16 个的分母是 F₃₃ = 3524578，已越界；若把上界收到 10⁶（< F₃₁），只能找到 14 个。
 * 也就是说本题第 15 个金块强制搜索规模上到百万级分母，这正是与优化解 15 次递推的差距来源。
 *
 * 题面样例（第 10 个金块是 74049690、x = 1/2 对应 2）写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

const val QMAX = 2_000_000L

/** 整数平方根（向下取整）：整数回验，不依赖浮点 sqrt 的边界判断。 */
fun isqrt(n: Long): Long {
    require(n >= 0) { "isqrt 只接受非负整数" }
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** 一个分母 q ≤ QMAX 时 5q² − 4 < 9.2×10¹⁸，Long 运算不会溢出。 */
fun safeDenominatorLimit(qMax: Long): Boolean = 5L * qMax * qMax < Long.MAX_VALUE

/**
 * 分母为 q 时，唯一可能的既约分子是 p = (√(5q²−4) − q)/2；
 * 返回对应的金块 n = pq，若不存在则返回 null。
 */
fun nuggetOfDenominator(q: Long): Long? {
    val disc = 5L * q * q - 4
    val s = isqrt(disc)
    if (s * s != disc) return null              // 判别式不是完全平方
    val diff = s - q
    if (diff <= 0 || diff % 2 != 0L) return null
    val p = diff / 2
    if (p < 1L) return null
    if (q * q - p * q - p * p != 1L) return null // 回代验证，排除开方误判
    return p * q
}

/** 逐分母扫描，按升序返回分母不超过 qMax 的全部金块。 */
fun nuggetsUpTo(qMax: Long = QMAX): LongArray {
    require(safeDenominatorLimit(qMax)) { "分母上界过大，5q² 会溢出 Long" }
    val out = ArrayList<Long>()
    var q = 2L
    while (q <= qMax) {
        nuggetOfDenominator(q)?.let { out.add(it) }
        q++
    }
    return out.toLongArray()
}

/**
 * 完全不用捷径的对照实现：双重循环枚举所有 (p, q)（不要求既约），
 * 直接按定义判断 n = pq/(q² − pq − p²) 是否为正整数。
 * 代价是二次量级，只在 q ≤ 20000 这种小规模上跑。
 */
fun naiveNuggets(qMax: Long): List<Long> {
    val seen = HashSet<Long>()
    for (q in 2..qMax) {
        val qq = q * q
        var p = 1L
        while (p * (p + q) < qq) {              // 等价于 q² − pq − p² > 0
            val num = p * q
            val den = qq - p * q - p * p
            if (num % den == 0L) seen.add(num / den)
            p++
        }
    }
    return seen.sorted()
}

fun solveBrute(qMax: Long = QMAX): Long {
    val nuggets = nuggetsUpTo(qMax)
    check(nuggets.size >= 15) { "分母上界 $qMax 内只找到 ${nuggets.size} 个金块" }
    return nuggets[14]
}

fun verifySample() {
    // 题面样例：x = 1/2（既约分母 2）给出 A_F = 2；而 n = 1, 3, 4, 5 对应的 x 都是无理数，
    // 所以分母 3、4 上不该冒出金块来。
    check(nuggetOfDenominator(2L) == 2L) { "x = 1/2（分母 2）应给出金块 2" }
    check(nuggetOfDenominator(3L) == null && nuggetOfDenominator(4L) == null) { "分母 3、4 不该给出金块" }
    check(nuggetsUpTo(20L).toList() == listOf(2L, 15L, 104L)) { "分母 ≤ 20 的金块应为 2, 15, 104" }

    // 朴素双重循环（不使用任何二次方程捷径）在 q ≤ 20000 上的结果，
    // 必须与逐分母解法完全一致，且前 10 个金块恰好以题面给出的 74049690 收尾。
    val naive = naiveNuggets(20_000L)
    check(naive == nuggetsUpTo(20_000L).toList()) { "朴素枚举与逐分母解法不一致：$naive" }
    check(naive == listOf(2L, 15L, 104L, 714L, 4895L, 33552L, 229970L, 1576239L, 10803704L, 74049690L)) {
        "前 10 个金块应为题面序列，实得 $naive"
    }

    // 上界敏感性：第 j 个金块的既约分母是 F_{2j+1}，所以 10⁶ 只覆盖到第 14 个（F₂₉ = 514229），
    // 2×10⁶ 才够到第 15 个（F₃₁ = 1346269）。这里只断言「多出恰好一个」这种关系，不写死答案。
    val c1 = nuggetsUpTo(1_000_000L).size
    val c2 = nuggetsUpTo(2_000_000L).size
    check(c2 == c1 + 1) { "上界从 10⁶ 放宽到 2×10⁶ 应恰好多出 1 个金块，实得 $c1 → $c2" }
    check(c1 == 14) { "分母 10⁶ 内应恰有 14 个金块（对应分母到 F₂₉ = 514229），实得 $c1" }
}

fun main() {
    verifySample()
    repeat(3) { solveBrute() }                   // JIT 预热
    val start = System.nanoTime()
    val answer = solveBrute()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
