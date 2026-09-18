/**
 * Project Euler 127 — abc-hits 暴力对照解
 *
 * 与优化解的差距来源：solution.kt 先用必要条件 2·rad(a)·rad(b) ≤ limit−2 把 1…limit−1
 * 按 rad 值装桶排序，再对每个位置只扫描由该必要条件定出的常数段前缀（实测 1.58×10⁷ 次
 * 候选检查）；本解完全不剪枝，直接按题面定义枚举：对每个 c 遍历所有 a < c/2（约 limit²/4 对），
 * 每对都做一次 rad(a)·rad(b)·rad(c) < c 的乘积比较，命中时才逐对验证三对 gcd。
 * 判据也刻意写成定义的字面形式：三对 gcd 分别检查、rad(abc) 用三个 rad 的乘积
 * （三者互素时才是 radical 的乘积），不使用「三对 gcd 等价于 gcd(a,b)=1」这条化简。
 *
 * 复杂度：O(limit²/4) 次迭代 + 命中时各 3 次 gcd，空间 O(limit)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
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
        if (composite[p]) continue
        var m = p
        while (m < limit) {
            rad[m] *= p
            if (m != p) composite[m] = true
            m += p
        }
    }
    return rad
}

/** 返回 [命中个数, Σc]：把每个 c 的全部 a < c/2 枚举一遍，逐对按定义判定。 */
fun bruteHits(limit: Int): LongArray {
    val rad = radicals(limit)
    var count = 0L
    var total = 0L
    for (c in 3 until limit) {
        val rc = rad[c]
        val half = (c - 1) / 2
        for (a in 1..half) {
            val b = c - a
            if (rad[a].toLong() * rad[b] * rc < c &&
                gcd(a, b) == 1 && gcd(a, c) == 1 && gcd(b, c) == 1
            ) {
                count++
                total += c
            }
        }
    }
    return longArrayOf(count, total)
}

const val LIMIT = 120_000

fun main() {
    // 题面锚点：c < 1000 时恰有 31 个 abc-hit、Σc = 12523
    val small = bruteHits(1_000)
    check(small[0] == 31L && small[1] == 12523L) { "c < 1000 应为 31 个、Σc = 12523，实得 ${small.toList()}" }
    // 边界：c < 10 只有 (1,8,9)；c < 4 没有任何三元组
    check(bruteHits(10).contentEquals(longArrayOf(1L, 9L)))
    check(bruteHits(4)[1] == 0L)

    repeat(3) { bruteHits(LIMIT) }                   // JIT 预热
    val start = System.nanoTime()
    val answer = bruteHits(LIMIT)
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer[1])
}
