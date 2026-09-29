#!/usr/bin/env kotlin
/**
 * Project Euler 268 — At Least Four Distinct Prime Factors Less Than 100
 * （至少被 4 个小于 100 的相异素数整除）
 *
 * 思路
 * ────
 * 记 P = 小于 100 的 25 个素数。对每个整数 n，令 m(n) = #{p ∈ P : p | n}，
 * 要求的是 m(n) ≥ 4 的 n < 10^16 的个数。直接判定每个 n 不可行（10^16 规模），
 * 转化为「先数所有可被某个 p 子集同时整除的 n，再做组合反演」：
 *
 *   对任意 j，记 f_j = Σ_{|S|=j} ⌊N / ∏_{p∈S} p⌋   （N 为计数上界，下同），
 *   它数的是 (n, S) 对：n ≤ N 且 S 中每个素数都整除 n。
 *   因为 f_j = Σ_{m ≥ j} C(m, j) · g_m（g_m = 恰好被 m 个 P 中素数整除的 n 的个数），
 *   二项式反演给出 g_m = Σ_j (−1)^{j−m} C(j, m) f_j，于是
 *
 *     Σ_{m ≥ 4} g_m = Σ_{j ≥ 4} w(j) · f_j，
 *     w(j) = Σ_{m=4}^{j} (−1)^{j−m} C(j, m) = (−1)^{j−4} C(j−1, 3)。
 *
 *   验证系数恒等式：j=4 → 1；j=5 → −C(5,4)+C(5,5)=−4=(−1)·C(4,3)；j=6 → C(6,4)−C(6,5)+C(6,6)=15−6+1=10=C(5,3)。
 *   即答案 = Σ_{|S| ≥ 4} (−1)^{|S|−4} C(|S|−1, 3) · ⌊N / ∏_{p∈S} p⌋。
 *
 *   关键剪枝：∏_{p∈S} p > N 的子集对总和的贡献恒为 0（⌊N/∏⌋ = 0），可以直接不展开。
 *   25 个素数连乘约 2.3×10^36，但乘积 ≤ 10^16 的子集只有约 4×10^6 个，全部枚举即可。
 *
 * 复杂度
 * ──────
 * 方法 A：DFS 枚举全部「乘积 ≤ N」的子集（素数按下标递增，乘积超过 N 即剪枝），
 *         时间 O(枚举到的子集数) ≈ 4×10^6 次乘除，内存 O(递归深度) = O(25)。
 * 方法 B：meet-in-the-middle：把 25 个素数劈成 12 + 13 两半，分别枚举全部子集
 *         （2^12、2^13 个），右半按「子集大小」分组并把乘积排序；左半每个子集对每个
 *         大小组做一次线性扫描（升序，超过 N/∏ 立即停），累计同样的权重公式。
 *         时间 O(2^12 · 13 + 有效配对数)，两半完全不同构，用来守住「容斥系数」与
 *         「floor 分块」两处最易错的地方。⌊N/(ab)⌋ = ⌊⌊N/a⌋/b⌋ 保证两半拆开仍然精确。
 * 暴力：  逐个数分解，只在小规模（≤ 10^7）可行，用于锚点与逐值对照。
 *
 * 验证
 * ────
 * · 题面锚点：小于 1000 的满足条件的正整数有 23 个 —— 方法 A、方法 B、暴力三方一致；
 * · 方法 A 与暴力在 N = 10^3…10^7 逐值一致；方法 A 与方法 B 在全部测试规模一致；
 * · 完整规模 10^16：A、B 两种独立枚举给出同一个数。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0268/solution.kt && java -cp … SolutionKt
 */

private const val LIMIT = 10_000_000_000_000_000L // 10^16：统计「小于 10^16」即 n ≤ LIMIT − 1

/** 小于 100 的素数，共 25 个。 */
private val PRIMES = intArrayOf(
    2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47,
    53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
)

/**
 * 容斥权重 w(j) = (−1)^{j−4} C(j−1, 3)（j ≥ 4；j < 4 记 0）。
 * C(j−1,3) = (j−1)(j−2)(j−3)/6，三个连续整数之积必被 6 整除，Long 内精确。
 */
private fun weights(): LongArray {
    val w = LongArray(PRIMES.size + 1)
    for (j in 4..PRIMES.size) {
        val c = (j - 1).toLong() * (j - 2) * (j - 3) / 6
        w[j] = if ((j - 4) % 2 == 0) c else -c
    }
    return w
}

/** 一次求解的结果：计数值 + 枚举过的子集数（用于复杂度对比）。 */
private data class SolveOut(val count: Long, val subsets: Long)

/**
 * 方法 A：DFS 枚举所有「∏p ≤ n」的素数子集，累加 w(|S|)·⌊n/∏⌋。
 * 素数下标递增枚举保证每个子集恰好访问一次；`prod > n / p` 即 ∏·p > n，其后更大的素数全部剪掉。
 */
private fun solveBySubsetDfs(n: Long): SolveOut {
    val w = weights()
    var total = 0L
    var subsets = 0L

    fun dfs(start: Int, size: Int, prod: Long) {
        if (size >= 4) {
            total += w[size] * (n / prod)
            subsets++
        }
        for (i in start until PRIMES.size) {
            val p = PRIMES[i].toLong()
            if (prod > n / p) break // 素数升序：本次及之后的乘积都超过 n
            dfs(i + 1, size + 1, prod * p)
        }
    }
    dfs(0, 0, 1L)
    return SolveOut(total, subsets)
}

/**
 * 方法 B（独立枚举）：meet-in-the-middle。
 * 把 25 个素数劈成 12 + 13 两半；右半的每个子集乘积按「子集大小」分组升序存表；
 * 左半 DFS 到每个子集 (q1, j1) 时，对每个大小 j2 线性扫描右半表直到 q2 > n/q1，
 * 用 ⌊n/(q1·q2)⌋ = ⌊⌊n/q1⌋/q2⌋ 累加 w(j1+j2)·⌊n/(q1 q2)⌋。
 * 与 A 的枚举顺序、剪枝方式、半边结构全部不同。
 */
private fun solveByMeetInMiddle(n: Long): SolveOut {
    val w = weights()
    val left = PRIMES.copyOfRange(0, 12)
    val right = PRIMES.copyOfRange(12, PRIMES.size)

    // 右半：按大小分组枚举全部 2^13 个子集（只需 ∏ ≤ n，超过的剪掉）
    val rightBySize = Array(right.size + 1) { ArrayList<Long>() }
    fun enumRight(start: Int, size: Int, prod: Long) {
        rightBySize[size].add(prod)
        for (i in start until right.size) {
            val p = right[i].toLong()
            if (prod > n / p) break
            enumRight(i + 1, size + 1, prod * p)
        }
    }
    enumRight(0, 0, 1L)
    val rightSorted = Array(rightBySize.size) { rightBySize[it].sorted() }

    var total = 0L
    var pairs = 0L
    fun enumLeft(start: Int, size: Int, prod: Long) {
        val bound = n / prod
        for (j2 in 0..right.size) {
            val wj = w[size + j2]
            if (wj == 0L) continue
            for (q2 in rightSorted[j2]) {
                if (q2 > bound) break // 表升序，后面的全部越界
                total += wj * (bound / q2)
                pairs++
            }
        }
        for (i in start until left.size) {
            val p = left[i].toLong()
            if (prod > n / p) break
            enumLeft(i + 1, size + 1, prod * p)
        }
    }
    enumLeft(0, 0, 1L)
    return SolveOut(total, pairs)
}

/** 暴力对照：逐个数把 25 个小素数除尽，统计不同素因子个数 ≥ 4 的 n ∈ [1, limit−1]。 */
private fun bruteCount(limit: Long): Long {
    var count = 0L
    for (m in 1 until limit) {
        var x = m
        var cnt = 0
        for (p in PRIMES) {
            if (x % p == 0L) {
                cnt++
                while (x % p == 0L) x /= p
            }
        }
        if (cnt >= 4) count++
    }
    return count
}

/** JIT 预热后 3 轮取最优（毫秒），并核对每轮结果一致。 */
private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优）")
    return best
}

fun main() {
    // ---------- 题面锚点：小于 1000 有 23 个 ----------
    val anchorA = solveBySubsetDfs(999L).count
    val anchorB = solveByMeetInMiddle(999L).count
    val anchorBrute = bruteCount(1000L)
    for ((name, v) in listOf("方法 A" to anchorA, "方法 B" to anchorB, "暴力" to anchorBrute)) {
        check(v == 23L) { "$name 在 n<1000 得 $v，题面要求 23" }
    }
    println("锚点：n < 1000 → 23（方法 A、方法 B、暴力一致）")

    // ---------- 小规模三方逐值对照 ----------
    for (lim in longArrayOf(1_000, 10_000, 100_000, 1_000_000, 10_000_000)) {
        val a = solveBySubsetDfs(lim - 1).count
        val b = solveByMeetInMiddle(lim - 1).count
        val c = bruteCount(lim)
        check(a == b && b == c) { "n < $lim：A=$a B=$b 暴力=$c 不一致" }
        println("n < $lim：A = B = 暴力 = $a")
    }

    // ---------- 完整规模 ----------
    val fullA = solveBySubsetDfs(LIMIT - 1)
    val fullB = solveByMeetInMiddle(LIMIT - 1)
    check(fullA.count == fullB.count) { "完整规模两法不一致：A=${fullA.count} B=${fullB.count}" }
    check(fullA.count > 0 && fullA.count < LIMIT) { "答案越界" }
    println("n < 10^16：方法 A 枚举 ${fullA.subsets} 个子集 → ${fullA.count}")
    println("n < 10^16：方法 B 匹配 ${fullB.subsets} 对 → ${fullB.count}")

    // ---------- 计时 ----------
    solveBySubsetDfs(10_000_000L)
    solveByMeetInMiddle(10_000_000L)
    solveBySubsetDfs(LIMIT - 1)
    solveByMeetInMiddle(LIMIT - 1)
    val msSmall = bestOf3("方法 A n<10^7（预热）", solveBySubsetDfs(9_999_999L).count) {
        solveBySubsetDfs(9_999_999L).count
    }
    val msA = bestOf3("方法 A n<10^16", fullA.count) { solveBySubsetDfs(LIMIT - 1).count }
    val msB = bestOf3("方法 B n<10^16", fullB.count) { solveByMeetInMiddle(LIMIT - 1).count }

    println()
    println("答案 = ${fullA.count}")
    println("汇总：方法 A n<10^7 ${"%.1f".format(msSmall)} ms；方法 A n<10^16 ${"%.1f".format(msA)} ms；" +
        "方法 B n<10^16 ${"%.1f".format(msB)} ms")
    println("check() 全部通过")
}
