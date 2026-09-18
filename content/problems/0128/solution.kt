/**
 * Project Euler 128 — Hexagonal Tile Differences（六边形地砖的差值）
 *
 * 思路：地砖按六边形螺旋编号，第 k 环（k ≥ 1）从 3k²−3k+2 编到 3k²+3k+1，共 6k 块。
 * 环内除「缝」两侧的两块砖外，每块砖的六个邻差里必有两个 1（同环前后邻居），
 * 剩下四个差值形如 A、A+5、A+6、A+7，其中 A 与 A+6 同奇偶、A+5 与 A+7 同奇偶，
 * 于是四者里恰有两个偶数（k ≥ 2 时都 ≥ 6，必为合数），至多只剩两个素数：
 * 非缝砖恒有 PD ≤ 2，因此 PD = 3 只可能出现在两类砖上：
 *
 *   · 环起始砖 n = 3k²−3k+2（12 点方向的缝），六差为
 *     {1, 6k−6, 6k−1, 6k, 6k+1, 12k+5}；其中 6k−6 = 6(k−1) 与 6k 必为合数，故
 *     PD = 3 ⟺ 6k−1、6k+1、12k+5 同时是素数。
 *   · 环结束砖 n = 3k²+3k+1（缝的另一侧），六差为
 *     {1, 6k−1, 6k, 6k+5, 6k+6, 12k−7}；其中 6k 与 6k+6 = 6(k+1) 必为合数，故
 *     PD = 3 ⟺ 6k−1、6k+5、12k−7 同时是素数。
 *
 * 模 5 剪枝：12k+5 ≡ 2k、6k−1 ≡ k−1、6k+1 ≡ k+1 (mod 5)，故起始砖必须 k ≡ 2,3 (mod 5)；
 * 6k+5 ≡ k、6k−1 ≡ k−1、12k−7 ≡ 2k−2 (mod 5)，故结束砖必须 k ≡ 2,3,4 (mod 5)。
 * 余数不对的 k 无需做任何素性判断即可跳过：起始砖免掉 3/5、结束砖免掉 2/5 的候选。
 *
 * 序列的前两块是特例：中心砖 1（邻差 1…6，素数 2、3、5 ⇒ PD = 3）与第一环起始砖 2
 * （邻差 1,1,5,6,7,17 ⇒ PD = 3）；第 1 环没有内环，上面的通式从 k = 2 起才成立。
 * 又因 3k²+3k+1 < 3(k+1)²−3(k+1)+2，环内先起始砖后结束砖的枚举顺序本身就是升序，无需排序。
 *
 * 复杂度：筛法 O(L log log L)，L 为覆盖 12k+5 所需的筛上界（实测 L = 2^20 = 1048576）；
 * 环扫描在剪枝后 O(K) 次查表，K 为找到第 2000 块砖所需的环数（实测 K = 69563）。空间 O(L)。
 * 全部中间量小于 2^63：K 约几万，砖号 3K² 约 1.45×10^10，需用 Long。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 埃氏筛：isPrime[x] 为 true 表示 x 是素数（x ≥ 2）。 */
fun sieve(limit: Int): BooleanArray {
    val isPrime = BooleanArray(limit) { true }
    if (limit > 0) isPrime[0] = false
    if (limit > 1) isPrime[1] = false
    var i = 2
    while (i.toLong() * i < limit) {
        if (isPrime[i]) {
            var j = i * i
            while (j < limit) {
                isPrime[j] = false
                j += i
            }
        }
        i++
    }
    return isPrime
}

/** 环 k 起始砖的编号：3k² − 3k + 2。 */
fun ringStart(k: Int): Long = 3L * k * k - 3L * k + 2L

/** 环 k 结束砖的编号：3k² + 3k + 1。 */
fun ringEnd(k: Int): Long = 3L * k * k + 3L * k + 1L

/**
 * 序列中第 nth 块 PD = 3 的砖。筛上界自适应倍增：候选值最大为 12k+5，
 * 若扫描越过当前筛上界而项数还不够，就把上界翻倍重来（重来一次的代价有界）。
 */
fun nthTerm(nth: Int): Long {
    if (nth <= 2) return if (nth == 1) 1L else 2L         // 前两块特例：1 与 2
    var limit = 1 shl 12                                  // 从 4096 起，够跑小规模样例
    while (true) {
        val isPrime = sieve(limit)
        var count = 2
        var k = 2
        while (12L * k + 5L < limit) {
            if (k % 5 == 2 || k % 5 == 3) {
                if (isPrime[6 * k - 1] && isPrime[6 * k + 1] && isPrime[12 * k + 5]) {
                    count++
                    if (count == nth) return ringStart(k)
                }
            }
            if (k % 5 >= 2) {                             // 2、3、4
                if (isPrime[6 * k - 1] && isPrime[6 * k + 5] && isPrime[12 * k - 7]) {
                    count++
                    if (count == nth) return ringEnd(k)
                }
            }
            k++
        }
        limit *= 2                                        // 上界不足，翻倍重扫
    }
}

/**
 * 按定义算 PD(n)：把小范围（环 ≤ 4）的六边形网格建成「坐标 → 编号」表，
 * 取 n 的六个邻居编号作差，数其中素数的个数。只用于复现题面的手工样例。
 */
fun pdByDefinition(n: Int): Int {
    val dirs = listOf(0 to -1, -1 to 0, -1 to 1, 0 to 1, 1 to 0, 1 to -1)
    val corners = { k: Int -> listOf(0 to -k, -k to 0, -k to k, 0 to k, k to 0, k to -k) }
    val step = listOf(-1 to 1, 0 to 1, 1 to 0, 1 to -1, 0 to -1, -1 to 0)
    val pos = HashMap<Pair<Int, Int>, Int>()
    pos[0 to 0] = 1
    for (k in 1..4) {
        for (q in 0..5) {
            val c = corners(k)[q]
            val d = step[q]
            for (r in 0 until k) {
                pos[(c.first + r * d.first) to (c.second + r * d.second)] = ringStart(k).toInt() + q * k + r
            }
        }
    }
    val isPrime = sieve(200)
    var here = 0 to 0
    for ((p, v) in pos) if (v == n) here = p
    var count = 0
    for ((dx, dy) in dirs) {
        val other = pos[(here.first + dx) to (here.second + dy)] ?: continue
        val diff = Math.abs(other - n)
        if (diff >= 2 && isPrime[diff]) count++
    }
    return count
}

fun verifySample() {
    // 题面手工样例：绕砖 8 顺时针的差是 12, 29, 11, 6, 1, 13 ⇒ PD(8) = 3
    check(pdByDefinition(8) == 3) { "PD(8) 应为 3，实得 ${pdByDefinition(8)}" }
    // 题面手工样例：绕砖 17 的差是 1, 17, 16, 1, 11, 10 ⇒ PD(17) = 2
    check(pdByDefinition(17) == 2) { "PD(17) 应为 2，实得 ${pdByDefinition(17)}" }
    // 中心砖与第一环起始砖（通式不覆盖的两个特例）
    check(pdByDefinition(1) == 3) { "PD(1) 应为 3，实得 ${pdByDefinition(1)}" }
    check(pdByDefinition(2) == 3) { "PD(2) 应为 3，实得 ${pdByDefinition(2)}" }
    // 序列第 3 项：环 2 起始砖 8 的六差 {1,6,11,12,13,29}，素数 11、13、29 ⇒ PD = 3
    check(pdByDefinition(8) == 3) { "PD(8) 应为 3，实得 ${pdByDefinition(8)}" }
    // 第 4 项 19：六差 {1,11,12,17,17,18}，素数 11、17、17 ⇒ PD = 3
    check(pdByDefinition(19) == 3) { "PD(19) 应为 3，实得 ${pdByDefinition(19)}" }
    // 题面锚点：序列第 10 块砖是 271
    check(nthTerm(10) == 271L) { "第 10 块应为 271，实得 ${nthTerm(10)}" }
    // 序列开头与前几项
    check(nthTerm(1) == 1L && nthTerm(2) == 2L && nthTerm(3) == 8L)
    check(nthTerm(4) == 19L && nthTerm(5) == 20L && nthTerm(6) == 37L)
}

fun main() {
    verifySample()
    val nth = 2000
    repeat(5) { nthTerm(nth) }                            // JIT 预热
    var best = Double.MAX_VALUE                           // 取多次最小值，压住机器负载抖动
    var answer = 0L
    repeat(7) {
        val start = System.nanoTime()
        answer = nthTerm(nth)
        val ms = (System.nanoTime() - start) / 1e6
        if (ms < best) best = ms
    }
    System.err.printf("optimized: %.4f ms%n", best)
    println(answer)
}
