/**
 * Project Euler 135 — Same Differences（相同的差）
 *
 * 思路：设三项为 x, y, z 依次为等差数列。n = x² − y² − z² > 0 迫使公差为负：
 * 若公差 d > 0 则 y > x、z > y，于是 y² + z² > x²，得 n < 0；d = 0 则 n = −x² < 0。
 * 所以三项严格递减，可唯一写成 x = k + t, y = k, z = k − t（t ≥ 1 是公差，k 是中间项，
 * z > 0 又给出 k > t）。代入定义式：
 *     n = (k + t)² − k² − (k − t)² = 4kt − k² = k(4t − k).
 * 令 u = k、v = 4t − k，则 n = uv，且三个约束全部落在 (u, v) 上：
 *     k > t  ⟺  u > (u + v)/4  ⟺  3u > v,     n > 0  ⟺  v > 0,     t ≥ 1  ⟺  u + v ≡ 0 (mod 4).
 * 反向也成立：给定满足 u + v ≡ 0 (mod 4)、3u > v、uv < 10⁶ 的正整数对 (u, v)，
 * 取 t = (u + v)/4、k = u 就还原出唯一三元组 (u + t, u, u − t)。因此
 *     「n 的解数」 = 「把 n 写成 uv、u + v ≡ 0 (mod 4)、3u > v 的有序方式数」。
 *
 * 关键观察：条件 3u > v 只约束 u 这一侧（u 是中间项，v 只是因子），而 n = uv 对调 u、v 不变。
 * 于是按**无序因子对** {a, b}（a ≤ b，a + b ≡ 0 (mod 4)）分类：
 *   · 让 b 当中间项永远合法（3b > a 由 a ≤ b 保证），稳定贡献 1 个解；
 *   · 让 a 当中间项合法当且仅当 b < 3a，此时两种角色给出两个不同的三元组，贡献 2；
 *   · a = b 时两种角色落到同一个三元组上，只能算 1 个解。
 * 枚举 a ≤ √N、b 走同余类 b ≡ −a (mod 4) 的因子对，每对记 1 或 2 个解：既不重复计数，
 * 也不会漏解，而且顺带把 u、v 对调产生的重复解消掉了（不需要再用集合去重）。
 *
 * 复杂度：因子对总数 Σ_{a ≤ √N} N/(4a) = (N/4)·H_{√N} = O(N log N)，随后 O(N) 扫一遍计数表；
 * 空间 O(N)（一张 10⁶ 的 Int 计数数组）。n、因子与计数都远在 Int 范围内，无需 Long 大整数。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

const val LIMIT = 1_000_000
const val TARGET = 10

/**
 * 统计每个 n（下标即 n）的解数。
 * 只枚举无序因子对 a ≤ b：b 从最小的同余合法值出发，每步 +4，保证 a + b ≡ 0 (mod 4)。
 */
fun solutionCounts(limit: Int = LIMIT): IntArray {
    val counts = IntArray(limit)
    var a = 1
    while (a.toLong() * a < limit) {                 // a ≤ b 且 ab < limit ⇒ a ≤ √limit
        var b = a
        while ((a + b) % 4 != 0) b++                 // 最小可行 b：a + b ≡ 0 (mod 4)
        while (a.toLong() * b < limit) {
            counts[a * b] += if (a == b || b >= 3 * a) 1 else 2
            b += 4
        }
        a++
    }
    return counts
}

fun solve(limit: Int = LIMIT, target: Int = TARGET): Int =
    solutionCounts(limit).count { it == target }

/**
 * 按定义直接枚举 n 的全部解（验证用）：最小项 z 与公差 d 均在 [1, n] 内。
 * 边界依据：解的三元组满足 k ≤ n（因 n = k·v ≥ k）与 z < k、d < k，故 z、d 都 ≤ n。
 */
fun definitionalTriples(n: Int): List<Triple<Int, Int, Int>> {
    val out = ArrayList<Triple<Int, Int, Int>>()
    for (z in 1..n) {
        for (d in 1..n) {
            val x = z + 2 * d
            val y = z + d
            if (x.toLong() * x - y.toLong() * y - z.toLong() * z == n.toLong()) {
                out.add(Triple(x, y, z))
            }
        }
    }
    return out
}

fun verifySample() {
    // 样例一：n = 27 恰有两个解，就是题面给的两组数，且 27 是最小的「恰两解」值
    val t27 = definitionalTriples(27).toSet()
    check(t27 == setOf(Triple(34, 27, 20), Triple(12, 9, 6))) { "27 的解应为题面那两组：$t27" }
    check(34L * 34 - 27L * 27 - 20L * 20 == 27L && 12L * 12 - 9L * 9 - 6L * 6 == 27L)
    val small = solutionCounts(1156)
    check(small[27] == 2) { "27 应恰有 2 个解，实得 ${small[27]}" }
    check((1 until 27).none { small[it] == 2 }) { "27 应是最小的恰两解值" }

    // 样例二：n = 1155 恰有十个解，且是最小的「恰十解」值
    check(definitionalTriples(1155).size == 10) { "1155 应恰有 10 个解" }
    check(small[1155] == 10) { "1155 应恰有 10 个解，因子法实得 ${small[1155]}" }
    check((1 until 1155).none { small[it] == 10 }) { "1155 应是最小的恰十解值" }

    // 因子法与定义法在小范围内逐个数比对，确认「一对因子记 1 或 2」不漏不重
    val c250 = solutionCounts(251)
    for (n in 1..250) {
        val byDefinition = definitionalTriples(n).size
        check(c250[n] == byDefinition) { "n=$n：因子法 ${c250[n]} ≠ 定义法 $byDefinition" }
    }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                            // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
