#!/usr/bin/env kotlin
/**
 * Project Euler 297 — Zeckendorf Representation（Zeckendorf 表示）
 *
 * 题目：Fibonacci 序列从 1, 2 起（1, 2, 3, 5, 8, 13, ...），每个正整数都能唯一地写成
 * 若干「互不相邻」的 Fibonacci 项之和（Zeckendorf 表示）。z(n) 是表示中项的个数，
 * 已知 0 < n < 10^6 时 Σz(n) = 7894453，求 0 < n < 10^17 时 Σz(n)。
 *
 * 记号与基本事实
 * ──────────────
 * 把序列记成权重 w_0 = 1, w_1 = 2, w_i = w_{i-1} + w_{i-2}：
 *     w = 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...
 * 「无相邻 1」的长度 m 位串与整数 [0, w_m − 1] 一一对应（这就是 Zeckendorf 表示：
 * 每个整数恰好对应一个合法串，z(n) 就是串里 1 的个数）。两个关键量只依赖 Fibonacci 数：
 *     C(m) = 长度 m 的合法串个数    = Fib(m+2)
 *     T(m) = 这些串中 1 的总个数    = T(m−1) + T(m−2) + C(m−2)
 * 其中 Fib 为教科书版 1, 1, 2, 3, 5, ...（C(m) = #合法串：按最高位 0 / 最高两位 10 分类）。
 *
 * 路径 A：数位 DP（高位 → 低位）
 * ──────────────────────────────
 * 上界 B = X − 1，按 B 的 Zeckendorf 串从高位到低位逐位转移。状态只有
 * (tight, prev)：tight = 前缀是否仍与 B 完全相同，prev = 上一位是不是 1（防相邻）。
 * 每个状态携带一对值 (方案数, 已放 1 的个数之和)；取位 b 时要求 b ≤ B 的对应位（tight 时）、
 * b = 1 时 prev 必须为 0。最后把所有状态的「1 的个数之和」相加。O(L)，L 为位数。
 *
 * 路径 B：B 的 1 位分解（秩分解，闭式求和）
 * ────────────────────────────────────────
 * 把 [0, B] 里的串按「第一个小于 B 的位置 i」分类，再加上 B 自身，得到
 *     Σ_{n ≤ B} z(n) = z(B) + Σ_{i: b_i = 1} [ onesAbove(i)·C(i) + T(i) ]
 * 其中 onesAbove(i) 是 B 在 i 位以上 1 的个数。理由：在 b_i = 1 处取 0、以下位完全自由，
 * 自由段有 C(i) 个串、贡献 T(i) 个 1，而高位固定段每次贡献 onesAbove(i) 个 1。
 * 不需要逐位推进，只查 Fibonacci 闭式表。O(L)。
 *
 * 验证
 * ────
 * 1. 定义级贪心：n < 10^k（k = 1..6）逐值做「最大项贪心分解」求和，与 A、B 全等；
 *    n < 10^6 得 7894453，与题面给定值一致；
 * 2. z(5) = 1、z(14) = 2、z(100) = 3，且 100 的分解恰是 3 + 8 + 89；
 * 3. 全尺寸 X = 10^17 上 A、B 给出同一个数；
 * 4. brute-force.kt 里有第三套独立实现：值递归
 *    T(M) = T(w_j) + (M − w_j) + T(M − w_j)（w_j 为 ≤ M 的最大项）复核全尺寸答案；
 * 5. 另一份 Python 重写（贪心 + 秩分解）给出同样的样例值与全尺寸答案（跨语言旁证）。
 *
 * 复杂度
 * ──────
 * A、B 都是 O(L)（L = B 的 Zeckendorf 位数，本题 82），状态数/查表数 ~10^2，微秒级；
 * 对拍用的贪心是 O(X·L)（只跑到 10^6）。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0297/solution.kt -d /tmp/kc-0297
 * java -cp /tmp/kc-0297:<kotlin-stdlib> SolutionKt
 */

private const val TARGET = 100_000_000_000_000_000L   // 10^17

/** PE 版 Fibonacci 权重 w[0] = 1, w[1] = 2, ...；n = 90 时最大项 4.66e18，不会溢出 Long。 */
private fun weights(n: Int): LongArray {
    val w = LongArray(n)
    w[0] = 1L
    if (n > 1) w[1] = 2L
    for (i in 2 until n) w[i] = w[i - 1] + w[i - 2]
    return w
}

/** 定义级贪心分解：每次取 ≤ n 的最大项，返回项数（即 z(n)）。 */
private fun zGreedy(n0: Long, w: LongArray): Int {
    var n = n0
    var cnt = 0
    var j = w.size - 1
    while (n > 0L) {
        while (w[j] > n) j--
        n -= w[j]
        cnt++
        j--      // n − w[j] < w[j−1]，下一项至少往小走两格；内层 while 再兜底定位
    }
    return cnt
}

/** 同上的贪心，但把用到的项列出来（抽查分解用）。 */
private fun zTerms(n0: Long, w: LongArray): List<Long> {
    var n = n0
    val terms = mutableListOf<Long>()
    var j = w.size - 1
    while (n > 0L) {
        while (w[j] > n) j--
        terms += w[j]
        n -= w[j]
        j--
    }
    return terms
}

/** 定义级逐值求和：Σ_{0 < n < X} z(n)。只用于小规模对拍。 */
private fun sumByGreedy(X: Long, w: LongArray): Long {
    var s = 0L
    var n = 1L
    while (n < X) {
        s += zGreedy(n, w)
        n++
    }
    return s
}

/** 把 B 的 Zeckendorf 表示拆成布尔位（下标 0 = 最低项 w_0）。返回长度 L 的数组，要求 w[L] > B。 */
private fun bitsOf(B: Long, w: LongArray): BooleanArray {
    var L = 0
    while (w[L] <= B) L++
    val bits = BooleanArray(L)
    var rem = B
    var j = L - 1
    while (rem > 0L) {
        while (w[j] > rem) j--
        bits[j] = true
        rem -= w[j]
        j--
    }
    return bits
}

/** 路径 A：数位 DP。返回 Σ_{0 ≤ n ≤ X−1} z(n)。 */
private fun sumByBitDp(X: Long, w: LongArray): Long {
    val bits = bitsOf(X - 1L, w)
    val L = bits.size
    // 状态编号 s = (tight shl 1) or prev
    var cnt = LongArray(4)
    var acc = LongArray(4)
    cnt[2] = 1L                                    // 空前缀：s = (tight = 1, prev = 0) = 2
    for (pos in L - 1 downTo 0) {
        val bi = if (bits[pos]) 1 else 0
        val nCnt = LongArray(4)
        val nAcc = LongArray(4)
        for (s in 0..3) {
            val c = cnt[s]
            val a = acc[s]
            if (c == 0L && a == 0L) continue
            val tight = s shr 1
            val prev = s and 1
            for (b in 0..1) {
                if (tight == 1 && b > bi) continue              // 贴着上界时不能超过 B
                if (b == 1 && prev == 1) continue               // 相邻两个 1 不合法
                val nt = if (tight == 1 && b == bi) 1 else 0
                val t = (nt shl 1) or b
                nCnt[t] += c
                nAcc[t] += a + c * b                            // 新放的 1 给每个串 +1
            }
        }
        cnt = nCnt
        acc = nAcc
    }
    var total = 0L
    for (s in 0..3) total += acc[s]
    return total
}

/** 路径 B：B 的 1 位分解 + Fibonacci 闭式表。返回 Σ_{0 ≤ n ≤ X−1} z(n)。 */
private fun sumByRank(X: Long, w: LongArray): Long {
    val bits = bitsOf(X - 1L, w)
    val L = bits.size
    val cntFree = LongArray(L + 1)                 // C(m)
    val onesFree = LongArray(L + 1)                // T(m)
    cntFree[0] = 1L
    if (L >= 1) {
        cntFree[1] = 2L
        onesFree[1] = 1L
    }
    for (m in 2..L) {
        cntFree[m] = cntFree[m - 1] + cntFree[m - 2]
        onesFree[m] = onesFree[m - 1] + onesFree[m - 2] + cntFree[m - 2]
    }
    var total = 0L
    var onesAbove = 0
    for (pos in L - 1 downTo 0) {
        if (bits[pos]) {
            // 分支：高位与 B 相同、pos 位取 0（比 B 小）、以下 pos 位完全自由
            total += onesAbove.toLong() * cntFree[pos] + onesFree[pos]
            onesAbove++
        }
    }
    total += onesAbove.toLong()                    // B 自身（最高位分支的收尾）
    return total
}

private fun pow10(k: Int): Long {
    var r = 1L
    repeat(k) { r *= 10L }
    return r
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.4f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    val w = weights(90)

    println("== 1. 定义级抽查 ==")
    val z5 = zGreedy(5, w)
    val z14 = zGreedy(14, w)
    val z100 = zGreedy(100, w)
    check(z5 == 1 && z14 == 2 && z100 == 3) { "抽查不符：z(5)=$z5, z(14)=$z14, z(100)=$z100" }
    println("z(5) = $z5（期望 1）、z(14) = $z14（期望 2）、z(100) = $z100（期望 3）")
    val terms100 = zTerms(100, w)
    println("100 的 Zeckendorf 分解 = ${terms100.joinToString(" + ")}（期望 3 项：89 + 8 + 3，即 3 + 8 + 89）")

    println()
    println("== 2. 小规模对拍：贪心逐值 vs 数位 DP(A) vs 1 位分解(B) ==")
    for (k in 1..6) {
        val X = pow10(k)
        val g = sumByGreedy(X, w)
        val a = sumByBitDp(X, w)
        val b = sumByRank(X, w)
        check(g == a && a == b) { "k = $k 不一致：贪心 $g / DP $a / 秩分解 $b" }
        println("n < 10^$k：贪心 = DP = 秩分解 = $g")
    }
    val sample = sumByBitDp(1_000_000L, w)
    check(sample == 7894453L) { "题面样例不符：$sample ≠ 7894453" }
    println("题面给定 Σz(n), n < 10^6 = 7894453：本机实跑 $sample ✓")

    println()
    println("== 3. 全尺寸 X = 10^17 ==")
    val ansA = sumByBitDp(TARGET, w)
    val ansB = sumByRank(TARGET, w)
    check(ansA == ansB) { "两条路径不一致：A = $ansA，B = $ansB" }
    println("路径 A（数位 DP，状态 (tight, prev)）        = $ansA")
    println("路径 B（B 的 1 位分解 + Fibonacci 闭式表） = $ansB")
    println("一致性 ✓（平均每个 n 约 ${"%.4f".format(ansA.toDouble() / (TARGET - 1))} 项）")

    println()
    println("== 4. 计时 ==")
    val msA = bestOf3("路径 A（数位 DP）", ansA) { sumByBitDp(TARGET, w) }
    val msB = bestOf3("路径 B（秩分解 + 闭式表）", ansB) { sumByRank(TARGET, w) }
    val msG = bestOf3("贪心逐值求和 10^6（对拍用）", 7894453L) { sumByGreedy(1_000_000L, w) }

    println()
    println("== 5. 结果 ==")
    println("Σ z(n)（0 < n < 10^17）= $ansA")
    println("check() 全部通过；A ${"%.4f".format(msA)} ms，B ${"%.4f".format(msB)} ms，贪心 10^6 ${"%.2f".format(msG)} ms")
}
