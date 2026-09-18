/**
 * Project Euler 127 — abc-hits（abc 三元组）
 *
 * 思路：rad(n) 是 n 的不同素因子之积。题目要求 a < b、a + b = c，且三对 gcd 全为 1。
 * 因为 c = a + b，三个条件等价于一个：
 *   gcd(a,c) = gcd(a, a+b) = gcd(a,b)，gcd(b,c) = gcd(b, a+b) = gcd(b,a)。
 * 两两互素又保证 abc 没有跨数的公共素因子，于是
 *   rad(abc) = rad(a)·rad(b)·rad(c)，
 * 判定式 rad(abc) < c 化为对称的 rad(a)·rad(b)·rad(c) < a + b。
 *
 * 剪枝：c ≥ 3 时 rad(c) ≥ 2，而 c ≤ limit−1，所以任何合法对都必须满足
 *   2·rad(a)·rad(b) ≤ limit−2          （必要条件，关于 a、b 对称）
 * 把 1…limit−1 按 rad 值计数排序（桶按 rad 升序），对每个位置 i 只向后扫描
 * rad ≤ (limit−2)/(2·radList[i]) 的那段连续前缀——这正是必要条件本身。
 * 必要条件关于 a、b 对称，所以每个无序对 {x,y} 恰被处理一次：设 rad(x) ≤ rad(y)，
 * 则 y 的 rad 落在 x 的窗口内，扫描到 x 时 j 就会越过 y 的位置；反过来扫描 y 时
 * 条件已不成立。判定式对 a、b 也对称，故循环里不必区分大小。
 * 当 (limit−2)/(2·rad(x)) < 2 时必要条件退化为 rad(y) ≤ 1，即只可能配 y = 1，
 * 而 y = 1 在表首已被 i = 0 那轮扫过，于是整段外循环可以直接 break。
 *
 * 复杂度：rad 用筛法 O(limit·log log limit)，计数排序 O(limit)，
 * 扫描规模 Σ_i #{j > i : 2·rad_i·rad_j ≤ limit−2}，实测 1.58×10⁷ 次候选检查，
 * 而按定义枚举是 limit²/4 ≈ 3.6×10⁹ 对。空间 O(limit)。
 * 三元积最坏约 7.2×10⁹（rad(c) 可到 119999），超出 Int 上限，用 Long 相乘；
 * 累加和 18 407 904 也在 Long 内。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 欧几里得算法求最大公约数。 */
fun gcd(x: Int, y: Int): Int {
    var p = x
    var q = y
    while (q != 0) {
        val t = p % q
        p = q
        q = t
    }
    return p
}

/** rad[1..limit-1]，rad[n] = n 的不同素因子之积（rad[1] = 1）。 */
fun radicals(limit: Int): IntArray {
    val rad = IntArray(limit) { 1 }
    val composite = BooleanArray(limit)
    for (p in 2 until limit) {
        if (composite[p]) continue                  // 只对素数筛它的倍数，每个素因子恰好乘一次
        var m = p
        while (m < limit) {
            rad[m] *= p
            if (m != p) composite[m] = true
            m += p
        }
    }
    return rad
}

/** 按定义判定：三对 gcd 逐个检查，rad 乘积即 rad(abc)（三者互素时成立）。 */
fun isAbcHit(a: Int, b: Int, c: Int, rad: IntArray): Boolean =
    a < b && a + b == c &&
        gcd(a, b) == 1 && gcd(a, c) == 1 && gcd(b, c) == 1 &&
        rad[a].toLong() * rad[b] * rad[c] < c

/**
 * 返回 [命中个数, Σc]，统计所有 c < limit 的 abc-hit。
 *
 * radList / valueList 是按 rad 升序装桶后的 1…limit−1（计数排序，桶内顺序无关），
 * 于是对位置 i，满足必要条件 2·rad_i·rad_j ≤ limit−2 的 j 构成一段连续前缀，
 * 循环里遇到更大的 rad 即可 break。判定先做便宜的乘积比较，最后才做 gcd。
 */
fun abcHits(limit: Int): LongArray {
    if (limit < 4) return longArrayOf(0L, 0L)        // c = a+b ≥ 3，更小就没有三元组
    val rad = radicals(limit)
    val n = limit - 1

    val hist = IntArray(limit)                       // hist[r] = #{n : rad[n] == r}
    for (v in 1 until limit) hist[rad[v]]++
    val cursor = IntArray(limit)
    var acc = 0
    for (r in 1 until limit) {
        cursor[r] = acc
        acc += hist[r]
    }
    val valueList = IntArray(n)
    val radList = IntArray(n)
    for (v in 1 until limit) {
        val slot = cursor[rad[v]]++
        valueList[slot] = v
        radList[slot] = rad[v]
    }

    var count = 0L
    var total = 0L
    for (i in 0 until n) {
        val x = valueList[i]
        val rx = radList[i]
        val bound = (limit - 2) / (2 * rx)           // 必要条件：rad(y) ≤ bound
        if (bound < 2) break                         // rad 已升序，后面只会更小
        var j = i + 1
        while (j < n) {
            val ry = radList[j]
            if (ry > bound) break                    // 前缀到此为止：rx·ry ≤ limit−2 已不成立
            val c = x + valueList[j]
            j++
            if (c >= limit) continue
            // 满足 ry ≤ bound ⇒ rx·ry ≤ (limit−2)/2 < 2³¹，二元积先用 Int 乘，再升 Long 与 rad(c) 相乘
            val pairProduct = rx * ry
            if (pairProduct.toLong() * rad[c] < c && gcd(x, c - x) == 1) {
                count++
                total += c
            }
        }
    }
    return longArrayOf(count, total)
}

fun solve(limit: Int = 120_000): Long = abcHits(limit)[1]

fun verifySample() {
    val rad = radicals(505)
    check(rad[504] == 42) { "题面例子：rad(504) 应为 42" }      // 504 = 2³·3²·7
    // 题面例子 (5, 27, 32)：三对 gcd 全为 1，rad(5·27·32) = rad(4320) = 30 < 32
    check(isAbcHit(5, 27, 32, rad)) { "(5,27,32) 应是 abc-hit" }
    check(rad[5] * rad[27] * rad[32] == 30)
    check(abcHits(10).contentEquals(longArrayOf(1L, 9L))) { "c < 10 只有 (1,8,9)" }
    // 题面锚点：c < 1000 恰有 31 个 abc-hit，Σc = 12523
    val small = abcHits(1_000)
    check(small[0] == 31L && small[1] == 12523L) { "c < 1000 应为 31 个、Σc = 12523，实得 ${small.toList()}" }
    check(solve(4) == 0L)                                     // 边界：没有任何 c < 4 的三元组
}

fun main() {
    verifySample()
    repeat(5) { solve() }                            // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
