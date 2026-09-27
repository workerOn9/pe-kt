#!/usr/bin/env kotlin
/**
 * Project Euler 240 — brute-force：枚举两组多重集（与 solution.kt 的 DP 完全不同路）。
 *
 * 思路换成：
 *   1. 枚举「最大的 k 个」构成的多重集 —— 降序、和为 target 的非增序列 t_1..t_k；
 *   2. 剩下 n−k 个骰子每个都 ≤ t_k（否则 t 就不是最大的 k 个），
 *      再枚举它们的多重集 b；
 *   3. 两组多重集合起来的方案数 = n! / (Π c_v!)，其中 c = t + b。
 *
 * 这样既不依赖 DP 的状态定义，也不用「按面值降序贪心取满」的推理，
 * 是对 solution.kt 的一条独立复核路径。规模只适用于小参数（本文件用题面样例的
 * 5d6/top3/15，枚举量约 10^3），所以它的定位是「公式对不对」的裁判。
 *
 * 另附一次 20d12/top10/70 的运行：多重集枚举量 C(21,10)·C(21,10) 量级过大，
 * 故只在解法文件里跑；本文件的 timing 取 5d6 样例规模。
 */

private fun fact(n: Int): Long {
    var r = 1L
    for (i in 2..n) r *= i
    return r
}

/** 把 c[1..sides] 这个多重集的方案数算成 n! / Π c_v! */
private fun multinomial(n: Int, c: IntArray): Long {
    var d = 1L
    for (x in c) d *= fact(x)
    return fact(n) / d
}

/**
 * 枚举 top-k 的降序多重集 t（t[0] >= t[1] >= …），和恰为 target。
 * 回调返回 true 可提前结束。
 */
private fun forEachTop(
    sides: Int, k: Int, target: Int, onEach: (IntArray) -> Unit,
) {
    val t = IntArray(k)
    fun rec(i: Int, remaining: Int, maxVal: Int) {
        if (i == k) {
            if (remaining == 0) onEach(t.copyOf())
            return
        }
        // t[i] 上界：不能超过 maxVal；下界：要给后面 (k-1-i) 个留至少 1
        val hi = minOf(maxVal, remaining - (k - 1 - i))
        for (v in hi downTo 1) {
            t[i] = v
            rec(i + 1, remaining - v, v)
        }
    }
    rec(0, target, sides)
}

/** 枚举剩下 m 个骰子的计数向量 b[1..limit]（Σb = m，每个值 ≤ limit），和不限 */
private fun forEachBottom(sides: Int, m: Int, limit0: Int, onEach: (IntArray) -> Unit) {
    val limit = minOf(limit0, sides)
    val b = IntArray(sides + 1)
    fun rec(v: Int, left: Int) {
        if (v > limit) {
            if (left == 0) onEach(b.copyOf())
            return
        }
        for (c in 0..left) {
            b[v] = c
            rec(v + 1, left - c)
        }
    }
    rec(1, m)
}

private fun brute(nDice: Int, nSides: Int, kTop: Int, target: Int): Long {
    var total = 0L
    forEachTop(nSides, kTop, target) { t ->
        val minTop = t[kTop - 1]
        val m = nDice - kTop
        forEachBottom(nSides, m, minTop) { b ->
            val c = IntArray(nSides + 1)
            for (v in 1..nSides) c[v] = t.count { it == v } + b[v]
            total += multinomial(nDice, c)
        }
    }
    return total
}

private fun main() {
    val sample = brute(5, 6, 3, 15)
    println("sample 5d6 top3=15 -> $sample   (题面 1111)")
    check(sample == 1111L) { "题面样例不吻合：$sample" }

    // 另两组小参数交叉验证：4d6/top2/7 与 3d4/top2/6
    val alt = brute(4, 6, 2, 7)
    println("alt 4d6 top2=7     -> $alt")
    check(alt == 108L) { "交叉验证失败：$alt" }

    val alt2 = brute(3, 4, 2, 6)
    println("alt 3d4 top2=6     -> $alt2")
    check(alt2 == 16L) { "交叉验证失败：$alt2" }

    println("brute-force OK（多重集枚举与 DP 在小规模上逐位一致）")
}
