/**
 * Project Euler 141 — 平方递进数 · 暴力对照解
 *
 * 与 solution.kt 的枚举方向相反。优化解以「公比的最简分数 p/q」为出发点正向生成候选：
 * 遍历互素的 (p,q) 与倍数 c，n = c·q·(c·p³ + q) 直接算出来再判平方。
 * 本解完全不用 p, q, c 这套参数，而是从**等比三项的因子结构**出发：
 * 三项排序后 A < B < C 满足 A·C = B²，且递进数的可行排列只有 (d,q,r) = (C,B,A)、(B,C,A)、(C,A,B)，
 * 前两者给出同一个 n = B·C + A，第三种给出 n = B(B+1)（永非平方，见下）。于是逐个枚举
 * 「中项 B」与「小项 A」，其中 A | B²、A < B，令 C = B²/A，检验 n = B·C + A 是否为完全平方数。
 * 枚举 A | B² 的技巧：a | b² ⟺ β(a) | b，其中 β(a) = ∏ f^⌈e/2⌉ 是「最小平方倍数根」，
 * 由最小质因子筛 O(N log log N) 预处理，再对每个 a 走一遍 β(a) 的倍数。
 *
 * 为什么 n = B(B+1) 可以整支丢掉：B² < B² + B < (B+1)²，即它严格落在两个相邻平方数之间。
 *
 * 复杂度：筛法 O(N log log N)（N = 10⁶）；主循环沿 β(a) 的倍数遍历共 3.64×10⁷ 次，其中 7.3×10⁶ 次
 * 通过上界检查、各做一次 64 位除法与一次整数开方；时间 O(N log log N + N·Σ 1/β(a))，空间 O(N)。
 * 优化解不需要筛法，候选由闭式直接生成，循环次数与之一致（约 7.3×10⁶）却没有这层常数开销，
 * 这是两者的主要差距来源。
 *
 * 样例断言：9 与 10404 = 102² 都是平方递进数，十万以内之和为 124657。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

const val N = 1_000_000                       // 中项 B 的上界：n > B² 迫使 B < √(10¹²)

/** 最小质因子筛：spf[x] 是 x 的最小质因子（spf[1] = 1）。 */
fun spfSieve(n: Int): IntArray {
    val spf = IntArray(n) { it }
    var i = 2
    while (i.toLong() * i < n) {
        if (spf[i] == i) {
            var j = i * i
            while (j < n) {
                if (spf[j] == j) spf[j] = i
                j += i
            }
        }
        i++
    }
    return spf
}

/** β(a) = ∏ f^⌈e/2⌉：使 a | b² 成立的最小 b 因子，即 b 必须是 β(a) 的倍数。 */
fun betaOf(a: Int, spf: IntArray): Int {
    var x = a
    var res = 1
    while (x > 1) {
        val f = spf[x]
        var e = 0
        while (x % f == 0) {
            x /= f
            e++
        }
        var pe = 1
        var k = 0
        while (k < (e + 1) / 2) {                 // ⌈e/2⌉ 次乘 f
            pe *= f
            k++
        }
        res *= pe
    }
    return res
}

/** 整数平方根（向下取整），浮点估计后用整数回验靠拢。 */
fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0L && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

fun isSquare(n: Long): Boolean {
    val r = isqrt(n)
    return r * r == n
}

/** 小于 limit 的全部平方递进数之和（按 A | B² 的因子结构枚举）。 */
fun solveBruteForce(limit: Long = 1_000_000_000_000L, cap: Int = N): Long {
    val spf = spfSieve(cap)
    val hits = HashSet<Long>()
    for (a in 1 until cap) {
        val b0 = betaOf(a, spf)                   // b 必须是 b0 的倍数才有 a | b²
        var b = b0
        while (b < cap) {
            if (b > a) {                          // 需要 A < B（C = B²/A > B 随之成立）
                val c = b.toLong() * b / a        // 由 a | b² 保证整除
                val n = b.toLong() * c + a        // = B·C + A，即 (d,q,r) = (C,B,A) 或 (B,C,A)
                if (n >= limit) break             // n 关于 b 单调递增
                if (isSquare(n)) hits.add(n)
            }
            b += b0
        }
    }
    return hits.sum()
}

fun verifySample() {
    // 题面例子：58 = 6×9 + 4，且 4, 6, 9 成等比
    check(58L / 6 == 9L && 58L % 6 == 4L)
    check(4L * 9L == 6L * 6L)
    // 9 = 4×2 + 1（1,2,4 成等比）；10404 = 102²
    check(9L / 4 == 2L && 9L % 4 == 1L)
    check(1L * 4L == 2L * 2L)
    check(isSquare(9L) && isSquare(10404L))
    // 两个样例值必须真的被枚举路径找到
    check(solveBruteForce(100_000L, 1000) == 124657L) { "十万以内应为 124657" }
    // 被丢弃的那一支 n = B(B+1) 确实永远不是平方数（小范围直接验证）
    var b = 1L
    while (b <= 100_000L) {
        check(!isSquare(b * (b + 1))) { "B(B+1) 不应是平方数：B = $b" }
        b++
    }
    // a | b² ⟺ β(a) | b：小范围逐对互证，确保筛法给出的 β 正确
    val spfSmall = spfSieve(4000)
    for (a in 1 until 300) {
        val b0 = betaOf(a, spfSmall)
        for (bb in 1 until 4000) {
            check(((bb.toLong() * bb) % a == 0L) == (bb % b0 == 0)) { "β 判定错误：a=$a b=$bb β=$b0" }
        }
    }
}

fun main() {
    verifySample()
    repeat(3) { solveBruteForce() }               // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
