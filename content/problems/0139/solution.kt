/**
 * Project Euler 139 — Pythagorean Tiles（毕达哥拉斯地砖）
 *
 * 思路：四块全等的整数边直角三角形 (a, b, c) 以斜边贴住外边界围成边长 c 的正方形，
 * 中央留下边长 d = |b − a| 的方形洞（面积恒等式：4·(ab/2) + (b−a)² = a² + b² = c²）。
 * 题面要求的铺砖是「用中洞那样的 d×d 方块铺满 c×c」，故条件就是 d | c：
 * (3,4,5) 的洞是 1×1，5/1 = 5，正好 25 块铺满 5×5；(5,12,13) 的洞是 7×7，而 7 ∤ 13。
 *
 * 关键一步是把条件化简到本原组。每个勾股三角形唯一写成 k 倍本原组，而整除条件与尺度无关：
 * k·d | k·c ⟺ d | c。对 gcd(a, b) = 1 的本原组，gcd(d, a) = gcd(b−a, a) = gcd(b, a) = 1，
 * 同理 gcd(d, b) = 1。由 c² = a² + b² ≡ 2ab (mod d) 得 d | c ⇒ d | 2ab ⇒ d | 2。
 * d = 2 要求两腿同奇偶，但本原组两腿一奇一偶（两腿都奇则 c² ≡ 1 + 1 ≡ 2 (mod 4) 无解，
 * 两腿都偶则不本原），故 d = 1。于是合法三角形恰好是「两腿相差 1」的本原组 (x, x+1, c) 的任意倍数。
 *
 * 求全部 (x, x+1, c)：由 2x² + 2x + 1 = c²，令 u = 2x + 1 化为负 Pell 方程
 *
 *     u² − 2c² = −1,
 *
 * 它的全部正整数解由 (1 + √2)(3 + 2√2)^t（t ≥ 0）的系数给出，写成整系数递推即
 *
 *     u' = 3u + 4c,   c' = 2u + 3c,
 *
 * 而周长 = (2x + 1) + c = u + c。初始解 (u, c) = (1, 1) 是退化的 x = 0，从 (7, 5)（即 (3,4,5)）起步。
 * 每步周长乘 3 + 2√2 ≈ 5.8283，故 u + c < 10⁸ 的本原组只有 10 个；对每个本原组累加
 * floor((limit − 1) / (u + c))，即它的倍数中周长严格小于 limit 的个数。
 *
 * 复杂度：迭代 O(log limit) 次（limit = 10⁸ 时 10 次），时间 O(log limit)，空间 O(1)。
 * 中间量最大约 5.5×10⁷（下一步 3.2×10⁸ 也远小于 Long 上限），答案约 10⁷。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 铺砖条件：洞边长 d = |b − a| 必须整除外正方形边长 c。 */
fun tiles(a: Long, b: Long, c: Long): Boolean {
    val d = if (a > b) a - b else b - a
    return c % d == 0L
}

/**
 * 周长严格小于 limit、可铺砖的勾股三角形个数。
 * 合法三角形恰是 (x, x+1, c) 这种本原组的整数倍；用 Pell 递推生成 (u, c) = (2x + 1, c)，
 * 本原周长 = u + c，其倍数贡献 floor((limit − 1) / (u + c)) 个。
 */
fun solve(limit: Long = 100_000_000L): Long {
    var count = 0L
    var u = 7L
    var c = 5L                                   // 对应 (3, 4, 5)：u = 2·3 + 1 = 7
    while (u + c < limit) {
        count += (limit - 1) / (u + c)
        val nextU = 3 * u + 4 * c
        val nextC = 2 * u + 3 * c
        u = nextU
        c = nextC
    }
    return count
}

/** 小范围穷举参照：直接扫 a < b，整数开方判 c，逐条检查铺砖条件（仅供 verifySample 互证）。 */
fun countByEnumeration(limit: Long): Long {
    var count = 0L
    var a = 1L
    while (3 * a + 2 < limit) {                  // 周长 > 3a ⇒ a 更大时无解
        var b = a + 1
        while (a + 2 * b < limit) {              // c > b ⇒ 周长 > a + 2b
            val s = a * a + b * b
            var c = Math.sqrt(s.toDouble()).toLong()
            while (c * c > s) c--
            while ((c + 1) * (c + 1) <= s) c++
            if (c * c == s && a + b + c < limit && tiles(a, b, c)) count++
            b++
        }
        a++
    }
    return count
}

fun verifySample() {
    // 题面样例：(3,4,5) 围成的 5×5 正方形，中洞 1×1，用 25 = (5/1)² 块铺满
    check(tiles(3, 4, 5)) { "(3,4,5) 应可铺砖" }
    check((5 / 1) * (5 / 1) == 25) { "5×5 应由 25 块 1×1 铺满" }
    // 题面反例：(5,12,13) 的中洞 7×7，7 ∤ 13
    check(!tiles(5, 12, 13)) { "(5,12,13) 不应可铺砖" }
    // 周长严格小于 limit 的边界：12 不行（(3,4,5) 本身周长 12），13 恰有一个
    check(solve(12L) == 0L) { "limit=12 应为 0" }
    check(solve(13L) == 1L) { "limit=13 应为 1" }
    check(solve(71L) == 6L) { "limit=71 应为 6：(3,4,5) 的 5 个倍数加 (20,21,29)" }
    // 与直接枚举 a < b < c 的穷举结果逐一对齐
    for (lim in longArrayOf(13L, 100L, 1000L, 3000L, 10_000L)) {
        check(solve(lim) == countByEnumeration(lim)) { "limit=$lim：Pell 解与穷举不一致" }
    }
}

fun main() {
    verifySample()
    val baseLimit = 100_000_000L
    repeat(5) { solve(baseLimit) }               // JIT 预热
    // solve 只有十轮定长递推（约 20 ns），单发计时会被时钟抖动吞掉，故预热后连跑 10⁶ 次取平均。
    // 计时的输入必须逐轮依赖上一轮的输出，且落在答案会变化的区间里：否则 JIT 会把 solve(baseLimit)
    // 当作循环不变量提升出循环、或证明它恒等于同一个常量，从而把整段折叠掉（那样会测出 0.0000 ms）。
    // 这里让规模在 [baseLimit − 16383, baseLimit] 内随累加器抖动，答案随之变化，无法被折叠；
    // 该区间内 Pell 递推的轮数都是 10，所以被测负载与本题真实规模一致。
    val reps = 1_000_000
    val answer = solve(baseLimit)
    check(solve(baseLimit - 16_383L) < answer) { "抖动区间内答案应当变化（计时不被折叠的前提）" }
    var acc = 0L
    val start = System.nanoTime()
    repeat(reps) {
        acc += solve(baseLimit - (acc and 0x3FFFL))
    }
    val elapsedMs = (System.nanoTime() - start) / 1e6
    // 单次耗时只有几十纳秒，按 %.4f 输出会一律显示 0.0000 ms，故这里保留 6 位并附上总量便于核对
    System.err.printf("optimized: %.6f ms  (1e6 次调用共 %.3f ms)%n", elapsedMs / reps, elapsedMs)
    println(answer)
}
