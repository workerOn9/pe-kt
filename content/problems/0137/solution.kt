/**
 * Project Euler 137 — Fibonacci Golden Nuggets（斐波那契金块）
 *
 * 思路推导
 * 1) 级数 A_F(x) = Σ_{k≥1} F_k x^k 有闭式和。由 F_k = F_{k−1} + F_{k−2} 得
 *      A_F(x) = x + x·A_F(x) + x²·A_F(x)  ⇒  A_F(x) = x / (1 − x − x²)，
 *    收敛半径 1/φ ≈ 0.618（分母的两个根是 1/φ 与 −φ）。
 * 2) 要求 A_F(x) = n（正整数）⇒ n x² + (n+1) x − n = 0 ⇒
 *      x = ( √(5n² + 2n + 1) − (n + 1) ) / (2n)。
 *    另一根取负号，其模长 = (m + n + 1)/(2n) ≥ ((√5 + 1)n + 1)/(2n) = φ + 1/(2n) > φ，
 *    已超出收敛半径 1/φ，对应级数发散，故只有正根有意义。
 *    x 有理 ⟺ m = √(5n² + 2n + 1) 为整数（有理数的平方根必为整数或无理数）。
 * 3) 两边乘 5：5m² = 25n² + 10n + 5 = (5n + 1)² + 4。令 u = 5n + 1，得 Pell 型方程
 *      u² − 5m² = −4，  且 u ≡ 1 (mod 5)。
 * 4) 该方程在正整数中的解恰为 (u, m) = (L_k, F_k)（k 为奇数），依据 Lucas 恒等式
 *      L_k² − 5F_k² = 4·(−1)^k。
 *    再叠加同余条件：Lucas 数模 5 以 4 为周期（1, 3, 4, 2, 1, …），
 *      L_k ≡ 1 (mod 5) ⟺ k ≡ 1 (mod 4)，
 *    k 为奇数这一条随之自动满足。于是 k = 4j + 1，第 j 个金块
 *      n_j = (L_{4j+1} − 1) / 5      (j ≥ 1；j = 0 给出 n = 0，非正整数)。
 * 5) 乘 α⁴ = (7 + 3√5)/2 得 L_{k+4} = 7·L_k − L_{k−4}，故下标递推
 *      u_{j+1} = 7·u_j − u_{j−1},   u_0 = L_1 = 1,  u_1 = L_5 = 11。
 *    第 1 个金块 = (11 − 1)/5 = 2，第 10 个 = (L_41 − 1)/5 = 74049690（与题面一致），
 *    第 15 个 = (L_61 − 1)/5 = 1120149658760 = F_30 · F_31。
 *
 * 复杂度：O(K) 次 Long 加减（K 为要求的名次，本题 15），空间 O(1)。
 * 全部中间量不超过 L_61 ≈ 5.6×10¹²，Long（上限 9.2×10¹⁸）绰绰有余。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 整数平方根（向下取整，Long 版）：整数回验，不依赖浮点 sqrt 的边界判断。 */
fun isqrt(n: Long): Long {
    require(n >= 0) { "isqrt 只接受非负整数" }
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/**
 * 按定义判定 n 是否为金块：5n² + 2n + 1 必须恰是完全平方数。
 * Long 版只在 5n² + 2n + 1 不溢出时用；n ≥ 1.36×10⁹ 起 5n² 会越过 Long 上限，
 * 必须改用 [isGoldenNuggetExact]。
 */
fun isGoldenNugget(n: Long): Boolean {
    val d = 5 * n * n + 2 * n + 1
    val r = isqrt(d)
    return r * r == d
}

private val BI_TWO = java.math.BigInteger.TWO
private val BI_FIVE = java.math.BigInteger.valueOf(5)

/** 按定义判定 n（任意非负整数）是否为金块，用 BigInteger 精确计算，不会溢出。 */
fun isGoldenNuggetExact(n: Long): Boolean {
    val bn = java.math.BigInteger.valueOf(n)
    val d = bn.multiply(bn).multiply(BI_FIVE).add(bn).add(bn).add(java.math.BigInteger.ONE)
    val r = d.sqrt()
    return r.multiply(r) == d
}

/** n 为金块时给出对应的 x = (√(5n²+2n+1) − n − 1) / (2n) 的既约形式 (分子, 分母)。 */
fun xForNugget(n: Long): Pair<Long, Long> {
    val bn = java.math.BigInteger.valueOf(n)
    val m = bn.multiply(bn).multiply(BI_FIVE).add(bn).add(bn).add(java.math.BigInteger.ONE).sqrt()
    val p = m.subtract(bn).subtract(java.math.BigInteger.ONE)
    val q = bn.multiply(BI_TWO)
    val g = p.gcd(q)
    return p.divide(g).longValueExact() to q.divide(g).longValueExact()
}

/** 按定义朴素扫描：返回不超过 limit 的全部金块（升序）。 */
fun nuggetsByDefinition(limit: Long): List<Long> {
    val out = ArrayList<Long>()
    var n = 1L
    while (n <= limit) {
        if (isGoldenNugget(n)) out.add(n)
        n++
    }
    return out
}

/**
 * 第 count 个金块：u_j = L_{4j+1} 由 u_{j+1} = 7·u_j − u_{j−1} 生成，
 * n_j = (u_j − 1) / 5。
 */
fun solve(count: Int = 15): Long {
    require(count >= 1) { "名次从 1 开始" }
    var prev = 1L          // u_0 = L_1，对应 n = 0（不算金块）
    var cur = 11L          // u_1 = L_5，对应第 1 个金块 2
    var j = 1
    while (j < count) {
        val next = 7 * cur - prev
        prev = cur
        cur = next
        j++
    }
    check((cur - 1) % 5 == 0L) { "u_j 必须 ≡ 1 (mod 5)" }
    return (cur - 1) / 5
}

/** 第 count 个斐波那契数（F_1 = F_2 = 1），用于交叉验证闭式。 */
fun fib(count: Int): Long {
    var a = 1L
    var b = 1L
    repeat(count - 1) {
        val t = a + b
        a = b
        b = t
    }
    return a
}

fun verifySample() {
    // 题面样例：前五个自然数中只有 2 是金块，且它对应 x = 1/2。
    check(xForNugget(2) == (1L to 2L)) { "n = 2 应对应 x = 1/2" }
    for (n in listOf(1L, 3L, 4L, 5L)) {
        check(!isGoldenNugget(n)) { "n = $n 不该是金块（5n²+2n+1 不是完全平方）" }
    }
    // 题面给出的表格：n = 2..5 的无理 x 值（用浮点复核 A_F(x) = n）
    for ((n, x) in listOf(2L to 0.5, 3L to (Math.sqrt(13.0) - 2) / 3, 4L to (Math.sqrt(89.0) - 5) / 8, 5L to (Math.sqrt(34.0) - 3) / 5)) {
        val byClosedForm = x / (1 - x - x * x)
        check(Math.abs(byClosedForm - n) < 1e-9) { "x = $x 时闭式和应为 $n，实得 $byClosedForm" }
    }
    // 题面剧透：第 10 个金块是 74049690
    check(solve(10) == 74049690L) { "第 10 个金块应为 74049690，实得 ${solve(10)}" }

    // 递推给出的前 5 个金块序列与题面「越来越稀有」的叙述一致
    val byRecurrence = (1..5).map { solve(it) }
    check(byRecurrence == listOf(2L, 15L, 104L, 714L, 4895L)) { "前五个金块应为 2, 15, 104, 714, 4895，实得 $byRecurrence" }

    // 交叉恒等式：n_j = F_{2j} · F_{2j+1}（由 L_{4j+1} − 1 = 5·F_{2j}F_{2j+1} 得到）
    for (j in 1..15) {
        check(solve(j) == fib(2 * j) * fib(2 * j + 1)) { "n_$j 与 F_{2j}F_{2j+1} 不符" }
    }

    // 递推的每个中间量都必须满足原始定义（5n²+2n+1 完全平方、x 的既约分母满足 q²−pq−p² = 1）
    for (j in 1..15) {
        val n = solve(j)
        check(isGoldenNuggetExact(n)) { "n_$j = $n 不满足定义" }
        val (p, q) = xForNugget(n)
        check(q * q - p * q - p * p == 1L) { "n_$j 的 x = $p/$q 应满足 q² − pq − p² = 1" }
        check(p < q) { "x 必须小于 1" }
    }

    // 溢出边界：n_12 = 3478759200 时 5n² ≈ 6.1×10¹⁹ 已越过 Long 上限 9.2×10¹⁸，
    // Long 版判定必须在此失真，改用 BigInteger 后才正确——留着这条断言防回归。
    check(!isGoldenNugget(solve(12))) { "Long 版在 n_12 处本该溢出失真，说明溢出点变了" }
    check(isGoldenNuggetExact(solve(12)))

    // 定义式暴力扫描（独立于 Pell 递推）：扫到 7.4×10⁷ 恰好得到前 10 个金块
    check(nuggetsByDefinition(74_049_690L) == (1..10).map { solve(it) }) { "按定义扫描的前 10 个金块与递推不一致" }

    // 闭式和本身：x = 1/2 的级数部分和收敛到 2
    var sum = 0.0
    var pow = 0.5                    // x^k，x = 1/2
    var fPrev = 0.0                  // F_0 = 0
    var fCur = 1.0                   // F_1 = 1
    for (k in 1..120) {
        sum += pow * fCur
        pow *= 0.5
        val next = fPrev + fCur
        fPrev = fCur
        fCur = next
    }
    check(Math.abs(sum - 2.0) < 1e-6) { "Σ (1/2)^k F_k 应趋近 2，实得 $sum" }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
