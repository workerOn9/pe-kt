/**
 * Project Euler 142 — Perfect Square Collection（完全平方数集合）
 *
 * 思路：把六个平方数写成
 *   x + y = a²,  x − y = b²,  x + z = c²,  x − z = d²,  y + z = e²,  y − z = f²。
 * 三组相减立刻给出三个勾股关系：
 *   d² = (x − z) = (x − y) + (y − z) = b² + f²
 *   c² = (x + z) = (x − y) + (y + z) = b² + e²
 *   a² = (x + y) = (x − z) + (y + z) = b² + e² + f²
 * 反过来，取 b ≥ 1、e > f ≥ 1、e ≡ f (mod 2)，只要 b² + e²、b² + f²、b² + e² + f²
 * 三者全是完全平方数，就由
 *   y = (e² + f²)/2,  z = (e² − f²)/2,  x = b² + y
 * 唯一还原出整数三重 (x, y, z)：六个式子逐一成立（x − z = b² + f²、x + z = b² + e²、
 * x + y = b² + e² + f²），且 x > y > z > 0 自动满足（b ≥ 1、e > f）。
 * 于是问题化为「勾股三元组的腿之间做配对」：对每条腿 b，取其全部配对腿 o（b² + o² 为平方），
 * 在配对腿内部挑同奇偶的一对 (e, f)，再验第三个条件。目标
 *   x + y + z = b² + 2y + z = b² + (3e² + f²)/2。
 *
 * 搜索边界：任一解满足 S = x + y + z > b²，故 b < √S；又 S > (3/2)e² > (3/2)f²，
 * 故 e ≤ √(2S/3)，f < e。取腿长上界 B = 2048 先求最小 S，程序内断言 B² > S 与 B² > 2S/3：
 * 任何更小的解其全部腿长必然小于 B，已被完整枚举，故所得即全局最小。
 *
 * 复杂度：Euclid 参数化生成腿长 ≤ B 的勾股三元组 O(B log B)（m ≤ √(2B)），配对表用 CSR
 * 存两遍计数 + 填充；之后对每条腿的配对表两两组合，共 O(B·d²)（d 为单条腿的配对数，实测 ≤ 10），
 * 每对含一次整数开方。空间 O(B + E)，E 为三元组条数。全部中间量 < 2³¹，用 Long 只是为乘法留余量。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

const val BOUND = 2048

/** 解的三重与它的化简参数：b² = x − y、e² = y + z、f² = y − z。 */
data class Solution(val x: Long, val y: Long, val z: Long, val b: Long, val e: Long, val f: Long) {
    val sum: Long get() = x + y + z
}

/** 勾股配对表的 CSR 形式：与腿 v 配对的腿是 data[start[v] until start[v+1]]。 */
class PartnerTable(private val start: IntArray, private val data: IntArray) {
    fun begin(v: Int): Int = start[v]
    fun end(v: Int): Int = start[v + 1]
    fun leg(v: Int, i: Int): Int = data[i]
}

fun gcd(a: Int, b: Int): Int {
    var p = a
    var q = b
    while (q != 0) {
        val t = p % q
        p = q
        q = t
    }
    return p
}

/** 整数开方（向下取整）：浮点估值后用整数回退校准，不依赖浮点边界。 */
fun isqrt(n: Long): Long {
    if (n <= 0L) return 0L
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0L && r * r > n) r--
    while ((r + 1L) * (r + 1L) <= n) r++
    return r
}

fun isSquare(n: Long): Boolean {
    if (n < 0L) return false
    val r = isqrt(n)
    return r * r == n
}

/**
 * 枚举腿长 ≤ bound 的全部勾股三元组，按 (u, v) 两条腿回调。
 * Euclid 参数化：m > n ≥ 1、m − n 奇、gcd(m, n) = 1 给出本原三元组，再乘 k 覆盖其整数倍。
 */
private inline fun forEachTripleLeg(bound: Int, action: (Int, Int) -> Unit) {
    var m = 2L
    while (m * m - 1 <= bound) {
        var n = 1L
        while (n < m) {
            if (((m - n) and 1L) == 1L && gcd(m.toInt(), n.toInt()) == 1) {
                val u0 = (m * m - n * n).toInt()
                val v0 = (2 * m * n).toInt()
                var k = 1L
                while (k * u0 <= bound && k * v0 <= bound) {
                    action((k * u0).toInt(), (k * v0).toInt())
                    k++
                }
            }
            n++
        }
        m++
    }
}

/** 腿长 ≤ bound 的勾股配对表：partners 中列出所有 o 使 v² + o² 为完全平方数。 */
fun buildPartners(bound: Int): PartnerTable {
    val deg = IntArray(bound + 2)
    forEachTripleLeg(bound) { u, v ->
        deg[u]++
        deg[v]++
    }
    val start = IntArray(bound + 2)
    for (v in 0..bound) start[v + 1] = start[v] + deg[v]
    val cursor = start.copyOf()
    val data = IntArray(start[bound + 1])
    forEachTripleLeg(bound) { u, v ->
        data[cursor[u]++] = v
        data[cursor[v]++] = u
    }
    return PartnerTable(start, data)
}

/** 在腿长 ≤ bound 的范围内求 x + y + z 最小的解，无解返回 null。 */
fun solve(bound: Int = BOUND): Solution? {
    val table = buildPartners(bound)
    var best: Solution? = null
    for (b in 1..bound) {
        val from = table.begin(b)
        val to = table.end(b)
        if (to - from < 2) continue
        val b2 = b.toLong() * b
        for (i in from until to) {
            for (j in i + 1 until to) {
                val e = maxOf(table.leg(b, i), table.leg(b, j)).toLong()   // 需要 e > f（即 z > 0）
                val f = minOf(table.leg(b, i), table.leg(b, j)).toLong()
                if (((e - f) and 1L) != 0L) continue                       // y、z 为整数 ⇒ 同奇偶
                if (!isSquare(b2 + e * e + f * f)) continue
                val sum = b2 + (3 * e * e + f * f) / 2
                if (best == null || sum < best.sum) {
                    val y = (e * e + f * f) / 2
                    val z = (e * e - f * f) / 2
                    best = Solution(b2 + y, y, z, b.toLong(), e, f)
                }
            }
        }
    }
    return best
}

/**
 * 独立路径（另一套参数化）：枚举 y、z 使 y ± z 均为平方，再由 (c − d)(c + d) = 2z 的因子对
 * 反解 x = z + d²，最后只查 x ± y 是否为平方。用于在小范围内确认没有更小的解。
 * 返回 true 表示在 y ≤ yMax 且 x + y + z < sumCap 的范围内确实无解。
 */
fun noSmallerByDefinition(yMax: Long, sumCap: Long): Boolean {
    var e = 2L
    while ((e * e + 1L) / 2 <= yMax) {
        var f = 1L
        while (f < e) {
            if (((e - f) and 1L) == 0L) {
                val y = (e * e + f * f) / 2
                if (y <= yMax) {
                    val z = (e * e - f * f) / 2
                    if (z > 0L && z < y) {
                        val t = 2 * z
                        var p = 1L
                        while (p * p <= t) {
                            if (t % p == 0L) {
                                val q = t / p
                                if (((q - p) and 1L) == 0L) {          // c、d 必须是整数
                                    val d = (q - p) / 2
                                    val x = z + d * d
                                    if (x > y && x + y + z < sumCap &&
                                        isSquare(x - y) && isSquare(x + y)
                                    ) {
                                        return false
                                    }
                                }
                            }
                            p++
                        }
                    }
                }
            }
            f++
        }
        e++
    }
    return true
}

fun verifySample() {
    val sol = solve() ?: error("B = $BOUND 范围内未找到解")

    // 1. 直接按定义回验六个式子（不看推导，只算 x、y、z 本身）
    check(sol.x > sol.y && sol.y > sol.z && sol.z > 0L) { "需要 x > y > z > 0：$sol" }
    val six = longArrayOf(
        sol.x + sol.y, sol.x - sol.y, sol.x + sol.z,
        sol.x - sol.z, sol.y + sol.z, sol.y - sol.z,
    )
    check(six.all { isSquare(it) }) { "六个式子必须全为完全平方数：${six.toList()}" }

    // 2. 化简条件自洽：b² + e²、b² + f²、b² + e² + f² 皆为平方，且与 x、y、z 的等式对齐
    val b2 = sol.b * sol.b
    check(sol.b * sol.b == sol.x - sol.y && sol.e * sol.e == sol.y + sol.z && sol.f * sol.f == sol.y - sol.z)
    check(isSquare(b2 + sol.e * sol.e)) { "x + z 应为平方" }
    check(isSquare(b2 + sol.f * sol.f)) { "x − z 应为平方" }
    check(isSquare(b2 + sol.e * sol.e + sol.f * sol.f)) { "x + y 应为平方" }
    check(sol.sum == b2 + (3 * sol.e * sol.e + sol.f * sol.f) / 2) { "闭式求和公式不符" }

    // 3. 搜索完备性：任何更小的解必有 b < √S、e ≤ √(2S/3) < B，故已被枚举
    check(BOUND.toLong() * BOUND > sol.sum) { "B 必须大于 √(x + y + z)" }
    check(BOUND.toLong() * BOUND > 2 * sol.sum / 3) { "B 必须大于 √(2S/3)" }
    // 腿长上界收缩到 800（756、520、117 都仍在范围内）答案不变，说明结论不靠放大 B 得来
    check(solve(800)?.sum == sol.sum) { "B = 800 时答案应相同" }

    // 4. 小范围独立穷举：y ≤ 50000 时不存在更小的解
    check(noSmallerByDefinition(50_000L, sol.sum)) { "y ≤ 50000 的范围内不应存在更小的解" }
}

fun main() {
    verifySample()
    repeat(20) { solve() }                           // JIT 预热
    // 单次 solve() 远短于 1 ms，单次计时会被调度抖动淹没；取 2000 次批量调用的均值
    val start = System.nanoTime()
    repeat(2000) { solve() }
    val perCallMs = (System.nanoTime() - start) / 1e6 / 2000.0
    System.err.printf("optimized: %.4f ms%n", perCallMs)
    println(solve()!!.sum)
}
