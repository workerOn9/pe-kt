/**
 * Project Euler 141 — Square Progressive Numbers（平方递进数）
 *
 * 思路：n = d·q + r（0 < r < d，q 是商、r 是余数），且 {d, q, r} 是某个等比数列的连续三项（顺序不限）。
 * 把这三项排序记作 A < B < C，等比数列给出 A·C = B²。将公比写成最简分数 p/q（p > q ≥ 1, gcd(p,q) = 1），
 * 三个数必然是
 *
 *     A = c·q²,   B = c·p·q,   C = c·p²        （c ≥ 1 为整数倍数）
 *
 * 枚举 (d, q, r) 的全部 6 种排列，只有 3 种满足余数条件 r < d：
 *   · (d,q,r) = (C,B,A) → n = C·B + A = c²p³q + cq²
 *   · (d,q,r) = (B,C,A) → n = C·B + A（与上一种算出同一个 n）
 *   · (d,q,r) = (C,A,B) → n = C·A + B = B² + B
 * 第三种落在两个相邻平方数之间：B² < B² + B < (B+1)²，故永不是完全平方数，整支丢弃。
 * 于是平方递进数只可能是
 *
 *     n = c·q·(c·p³ + q),    p > q ≥ 1, gcd(p,q) = 1, c ≥ 1, n < 10¹²。
 *
 * 对每组互素的 (p,q) 让 c 从 1 递增直到 n 越界（n 关于 c 单调递增），用整数开方判定完全平方数，
 * 命中值放进 HashSet 去重后求和。
 *
 * 复杂度：q = 1, c = 1 时 n = p³ + 1 < 10¹² 给出 p ≤ 9999；固定 (p,q) 时 c 的上界约 √(10¹²/(q p³))，
 * 全部候选共约 7.3×10⁶ 组，每组一次乘法 + 一次整数开方，时间 O(Σ_p Σ_q √(L/(q p³)))，空间 O(H)。
 * 中间量最大是 c·q·(c p³ + q) 在越界那一刻的值（略大于 10¹²），Long 足够。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 最大公约数。 */
fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** 整数平方根（向下取整）：浮点估计后用整数乘法回验靠拢，规避 sqrt 的舍入误差。 */
fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0L && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** n 是否为完全平方数。 */
fun isSquare(n: Long): Boolean {
    val r = isqrt(n)
    return r * r == n
}

/** 小于 limit 的全部平方递进数（升序、去重）。 */
fun solveSet(limit: Long = 1_000_000_000_000L): List<Long> {
    val hits = HashSet<Long>()
    var p = 2L
    while (p * p * p + 1 < limit) {                  // q = 1, c = 1 时 n = p³ + 1，再大的 p 无解
        val p3 = p * p * p
        var q = 1L
        while (q < p) {
            if (q * (p3 + q) >= limit) break         // c ≥ 1 时 n ≥ q(p³ + q)，关于 q 单调递增
            if (gcd(p, q) == 1L) {
                var c = 1L
                while (true) {
                    val n = c * q * (c * p3 + q)
                    if (n >= limit) break
                    if (isSquare(n)) hits.add(n)
                    c++
                }
            }
            q++
        }
        p++
    }
    return hits.sorted()
}

/** 小于 limit 的全部平方递进数之和。 */
fun solve(limit: Long = 1_000_000_000_000L): Long = solveSet(limit).sum()

/** 按题面定义逐项检验 n 是否递进（枚举除数 d，供样例断言使用）。 */
fun isProgressiveByDefinition(n: Long): Boolean {
    var d = 1L
    while (d <= n) {
        val q = n / d
        val r = n % d
        if (r > 0L && r < d) {
            val t = longArrayOf(d, q, r).sortedArray()
            if (t[0] * t[2] == t[1] * t[1]) return true
        }
        d++
    }
    return false
}

fun verifySample() {
    // 题面例子：58 ÷ 6 → 商 9 余 4，且 4, 6, 9 是公比 3/2 的等比数列连续三项
    check(58L / 6 == 9L && 58L % 6 == 4L)
    check(4L * 9L == 6L * 6L)
    // 题面点名的两个平方递进数：9 与 10404 = 102²（并按定义逐项复核）
    check(isProgressiveByDefinition(9L))
    check(isProgressiveByDefinition(10404L))
    check(isSquare(9L) && isSquare(10404L))
    // 十万以内的四个平方递进数：由题面给出的和 124657 与逐项定义检验共同锚定
    val below100k = solveSet(100_000L)
    check(below100k == listOf(9L, 10404L, 16900L, 97344L)) { "十万以内应为 9,10404,16900,97344，实得 $below100k" }
    for (n in below100k) check(isProgressiveByDefinition(n)) { "$n 不是递进数" }
    check(solve(100_000L) == 124657L) { "十万以内之和应为 124657，实得 ${solve(100_000L)}" }
    // 严格上界：小于 9 时无解
    check(solve(9L) == 0L)
    // 生成式与题面例子对上：9 = 1·1·(1·2³+1)，10404 = 36·1·(36·2³+1)
    check(1L * 1 * (1 * 8 + 1) == 9L)
    check(36L * 1 * (36 * 8 + 1) == 10404L)
}

fun main() {
    verifySample()
    repeat(5) { solve() }                            // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
