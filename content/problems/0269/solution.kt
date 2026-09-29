#!/usr/bin/env kotlin
/**
 * Project Euler 269 — Polynomials with at Least One Integer Root
 * （至少有一个整数根的多项式）
 *
 * 思路
 * ────
 * 把 n 写成十进制数位 d_{L-1}…d_0（允许前导零，P_n 不受前导零影响），
 * 则 P_n(x) = Σ_{i=0}^{L-1} d_i x^i，且 P_n(0) = d_0、P_n(1) = 数位和、P_n(10) = n。
 * 三个事实把问题化成数位 DP：
 *
 *   1) 根的范围：|r| ≥ 10 时首项 d_{L-1} r^{L-1} 严格压倒其余各项之和
 *      （d_{L-1}|r|^{L-1} ≥ |r|^{L-1} > 9(|r|^{L-1}−1)/(|r|−1) ≥ |其余项|），
 *      故 P_n(r) ≠ 0；又 r = 1 时 P_n(1) = 数位和 > 0。所以只需考虑 r ∈ [-9, 9]。
 *   2) 有理根定理：整根 r 必整除常数项 d_0（末位数字）。若 d_0 = 0，则 r = 0 恒为根，
 *      所有末位为 0 的数自动满足——在 [0, 10^L−1] 的 L 位串里正好有 10^{L-1} 个。
 *   3) 末位 d_0 = d ∈ {1..9} 时，候选根 = d 的约数（正负都取，去掉 +1，再含 −1），
 *      至多 7 个：例如 d = 8 时是 {−1, ±2, ±4, ±8}。
 *
 *   记 Z(10^L) 为 n ≤ 10^L 中有根的个数。n = 0（全零串）不是正数，但 P_0 ≡ 0 有根；
 *   n = 10^L 的 P = x^L 有根 0。两者一出一进恰好抵消，于是
 *
 *     Z(10^L) = 10^L − Σ_{d=1}^{9} N_L(d)，
 *
 *   其中 N_L(d) 是「L 位串、末位为 d、且没有任何候选根」的个数。
 *
 * 方法 A（主路径）：从高位到低位扫数位，对每个候选根 r 维护已处理前缀的值
 *     V_r = Σ_{i=k}^{L-1} d_i r^{i-k}（第 k 位处理完后的归一化前缀值）。
 *   若 r 是该串的根，则剩余数位（第 0..k−1 位）必须恰好抵消 r^k·V_r，于是
 *     |V_r| ≤ 9k（|r| = 1）或 |V_r|·(|r|−1) ≤ 9（|r| ≥ 2）；
 *   一旦越界就永久剪掉该根（它一定不是根），此后再不管它的值。每个数串只有一条
 *   状态路径（状态 = 各存活根的 V），末尾「仍有存活的根满足 V_r = −d_0/r」即有根。
 *   一个末位 d 跑一次 DP，9 次 DP 覆盖全部；不做容斥、无重复计数。 *
 * 方法 B（独立复核）：从低位到高位做进位 DP + 容斥反演。
 *   对候选根子集 T，数出「P_n(r) = 0 对所有 r ∈ T」的串数 A_T：进位
 *   c_k = −(Σ_{i<k} d_i r^i)/r^k 从 c_0 = 0 出发，逐位要求 (c_k − d_k) 被 r 整除，
 *   终态要求所有 c_L = 0（等价于 P_n(r) = 0）。再按容斥
 *   U_L(d) = Σ_{∅≠T⊆候选} (−1)^{|T|+1} A_T 得到「至少一个根」的串数。
 *   与 A 相比：扫描方向相反、状态语义不同（进位 vs 前缀值）、合并方式不同（容斥 vs 单遍剪枝）。
 *
 * 复杂度
 * ──────
 * 方法 A：9 个末位类 × L−1 步 × 每步状态数 × 10 个数字；状态数被剪枝压得很小
 *   （L = 16 时单步状态数最多 5089 个，实跑毫秒级）。内存 = 单步状态表。
 * 方法 B：Σ_{d} (2^{|S_d|}−1) 个根子集，每个子集一次 O(状态数) 的进位 DP；
 *   子集总数 345 个。两法都不含任何近似。
 * 暴力：对每个 n ≤ 10^L 直接代入 19 个候选根求值，只在小规模（L ≤ 6）可行。
 *
 * 验证
 * ────
 * · 题面锚点：Z(10^5) = 14696 —— 方法 A、方法 B、暴力三方一致；
 * · 暴力在 L = 3, 4, 5, 6 与两种方法逐值一致（172、1754、14696、152960）；
 * · 完整规模 L = 16：A、B 两法（含逐末位类分解）完全一致。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0269/solution.kt && java -cp … SolutionKt
 */

import kotlin.math.abs

private const val FULL_L = 16

/** 10 的幂（Long）。 */
private fun pow10(e: Int): Long {
    var r = 1L
    repeat(e) { r *= 10L }
    return r
}

/**
 * 末位为 d0 时的候选根：d0 的正约数 d ≤ 9 取 ±d，去掉 +1（P_n(1) = 数位和 > 0），
 * 保留 −1。d0 ∈ 1..9 时候选根个数最多 7（d0 = 8：{−1, ±2, ±4, ±8}）。
 */
private fun candidates(d0: Int): IntArray {
    val list = ArrayList<Int>(8)
    for (d in 1..9) {
        if (d0 % d != 0) continue
        list.add(-d)
        if (d != 1) list.add(d)
    }
    return list.toIntArray()
}

// 状态打包：每个根的 V / 进位用 9 位存储，偏移 256；dead = 400 表示该根已被剪掉。
private const val SHIFT = 9
private const val SLOT_MASK = (1L shl SHIFT) - 1
private const val BASE = 256
private const val DEAD = 400

/**
 * 方法 A：高位优先 + 根剪枝。返回「L 位串、末位固定 d0、且没有整数根」的串数。
 *
 * V_r 在每步后按下式剪枝（k = 刚处理完的数位下标，剩余 k 位）：
 *   |r| = 1：若 |V| > 9k 则 r 不可能是根；
 *   |r| ≥ 2：若 |V|·(|r|−1) > 9 则 r 不可能是根（剩余位最多凑出 9/(|r|−1)）。
 */
private fun noRootCountMSB(d0: Int, L: Int): Long {
    val roots = candidates(d0)
    val n = roots.size
    val target = IntArray(n) { -d0 / roots[it] } // 末尾命中的 V 值：−d0/r（r 整除 d0）

    var cur = HashMap<Long, Long>()
    var init = 0L
    for (i in 0 until n) init = init or (BASE.toLong() shl (SHIFT * i)) // 所有 V = 0
    cur[init] = 1L

    var pos = L - 1
    while (pos >= 1) {
        val next = HashMap<Long, Long>(cur.size * 3)
        for ((st, cnt) in cur) {
            for (d in 0..9) {
                var ns = 0L
                var i = 0
                while (i < n) {
                    val stored = ((st ushr (SHIFT * i)) and SLOT_MASK).toInt()
                    var out = DEAD
                    if (stored != DEAD) {
                        val r = roots[i]
                        val nv = (stored - BASE) * r + d
                        val a = abs(nv)
                        val keep = (if (r == -1) a <= 9 * pos else a * (abs(r) - 1) <= 9)
                        if (keep) out = nv + BASE
                    }
                    ns = ns or (out.toLong() shl (SHIFT * i))
                    i++
                }
                next[ns] = (next[ns] ?: 0L) + cnt
            }
        }
        cur = next
        pos--
    }

    // 末态：只要有一个存活根的 V 恰好等于 −d0/r，这个串就有根。
    var noRoot = 0L
    for ((st, cnt) in cur) {
        var hit = false
        var i = 0
        while (i < n) {
            val stored = ((st ushr (SHIFT * i)) and SLOT_MASK).toInt()
            if (stored != DEAD && stored - BASE == target[i]) {
                hit = true
                break
            }
            i++
        }
        if (!hit) noRoot += cnt
    }
    return noRoot
}

/**
 * 方法 B：低位优先的进位 DP。数出「L 位串、末位固定 d0、且 P_n(r) = 0 对所有 r ∈ T」
 * 的串数。进位 c_k = −(Σ_{i<k} d_i r^i)/r^k；每位要求 (c − d) 被 r 整除，终态要求 c_L = 0。
 */
private fun countAllRootsLSB(T: IntArray, d0: Int, L: Int): Long {
    val n = T.size
    var start = 0L
    for (i in 0 until n) {
        start = start or (((-d0 / T[i]) + BASE).toLong() shl (SHIFT * i)) // r | d0，精确整除
    }
    var cur = HashMap<Long, Long>()
    cur[start] = 1L

    repeat(L - 1) {
        val next = HashMap<Long, Long>(cur.size * 3)
        for ((st, cnt) in cur) {
            for (d in 0..9) {
                var ns = 0L
                var ok = true
                var i = 0
                while (i < n) {
                    val r = T[i]
                    val c = ((st ushr (SHIFT * i)) and SLOT_MASK).toInt() - BASE
                    val num = c - d
                    if (num % abs(r) != 0) {
                        ok = false
                        break
                    }
                    ns = ns or (((num / r) + BASE).toLong() shl (SHIFT * i))
                    i++
                }
                if (ok) next[ns] = (next[ns] ?: 0L) + cnt
            }
        }
        cur = next
    }
    var zero = 0L
    for (i in 0 until n) zero = zero or (BASE.toLong() shl (SHIFT * i)) // 所有进位 = 0
    return cur[zero] ?: 0L // 终态：所有进位为 0
}

/** 方法 B 的容斥：末位 d0、至少一个候选根成立时的串数。 */
private fun unionCountLSB(d0: Int, L: Int): Long {
    val roots = candidates(d0)
    val m = roots.size
    var total = 0L
    val subset = IntArray(m)
    for (mask in 1 until (1 shl m)) {
        var k = 0
        for (i in 0 until m) if ((mask shr i) and 1 == 1) subset[k++] = roots[i]
        var v = countAllRootsLSB(subset.copyOf(k), d0, L)
        if (Integer.bitCount(mask) % 2 == 0) v = -v
        total += v
    }
    return total
}

/** 暴力：对 n = 1..10^L 逐个数位求值，检查 r ∈ [-9, 9] 是否有根。 */
private fun bruteZ(L: Int): Long {
    val limit = pow10(L)
    var count = 0L
    val digits = IntArray(L + 1)
    for (n in 1..limit) {
        var len = 0
        var x = n
        while (x > 0) {
            digits[len++] = (x % 10L).toInt()
            x /= 10
        }
        var has = false
        for (r in -9..9) {
            var v = 0L
            for (i in len - 1 downTo 0) v = v * r + digits[i]
            if (v == 0L) {
                has = true
                break
            }
        }
        if (has) count++
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
    /** 方法 A 的总计数：Z(10^L) = 10^L − Σ 无根串数。 */
    fun solveA(L: Int): Long {
        var noRoot = 0L
        for (d0 in 1..9) noRoot += noRootCountMSB(d0, L)
        return pow10(L) - noRoot
    }

    /** 方法 B 的总计数：Z(10^L) = 10^L − Σ 无根串数，其中无根 = 10^{L-1} − 容斥计出的有根数。 */
    fun solveB(L: Int): Long {
        val half = pow10(L - 1)
        var noRoot = 0L
        for (d0 in 1..9) noRoot += half - unionCountLSB(d0, L)
        return pow10(L) - noRoot
    }

    // ---------- 小规模：暴力和两法三方对照 ----------
    for (L in 3..6) {
        val a = solveA(L)
        val b = solveB(L)
        val c = bruteZ(L)
        check(a == b && b == c) { "L=$L：A=$a B=$b 暴力=$c 不一致" }
        println("L=$L：A = B = 暴力 = $a")
    }

    // ---------- 题面锚点 ----------
    check(solveA(5) == 14_696L) { "Z(10^5) 应为 14696" }
    println("锚点：Z(100000) = 14696（三方一致）")

    // ---------- 逐末位类分解（用 A、B 分别核对） ----------
    val half = pow10(FULL_L - 1)
    var sumA = 0L
    var sumB = 0L
    for (d0 in 1..9) {
        val a = half - noRootCountMSB(d0, FULL_L)
        val b = unionCountLSB(d0, FULL_L)
        check(a == b) { "末位 $d0：A 计 $a，B 计 $b" }
        sumA += a
        sumB += b
        println("末位 $d0：至少一个根的数串 $a（两法一致）")
    }

    // ---------- 完整规模 ----------
    val fullA = half + sumA
    val fullB = half + sumB
    check(fullA == fullB) { "完整规模不一致：A=$fullA B=$fullB" }
    println("Z(10^$FULL_L)：末位 0 的 $half 个全部计入；其余 $sumA 个；合计 $fullA")

    // ---------- 计时 ----------
    solveA(FULL_L)
    solveB(FULL_L)
    val msA = bestOf3("方法 A Z(10^$FULL_L)", fullA) { solveA(FULL_L) }
    val msB = bestOf3("方法 B Z(10^$FULL_L)", fullB) { solveB(FULL_L) }
    val msBrute = bestOf3("暴力 Z(10^6)", 152_960L) { bruteZ(6) }

    println()
    println("答案 = $fullA")
    println("汇总：方法 A ${"%.1f".format(msA)} ms；方法 B ${"%.1f".format(msB)} ms；暴力 L=6 ${"%.1f".format(msBrute)} ms")
    println("check() 全部通过")
}
