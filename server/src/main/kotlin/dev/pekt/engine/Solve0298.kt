package dev.pekt.engine

import java.util.Locale
import kotlin.math.abs

/**
 * PE 298 — Selective Amnesia（选择性遗忘）：50 轮后 E|L−R| = 1.76882294（8 位小数）。
 *
 * 题目：数字 1..10 均匀独立地逐个叫出，叫中记忆中的数得 1 分；未命中则加入记忆（容量 5），
 *   Larry 淘汰「最久未被叫」（LRU）、Robin 淘汰「最早进入」（FIFO）。求 50 轮后 E|L−R|。
 *
 * 推导（完整过程与互证见 content/problems/0298/solution.kt 与 0298/analysis.md）：
 *   10 个数字完全对称，只需追踪「相等结构」——Larry 记忆按最近被叫排序为槽位 1..k，
 *   Robin 队列（队首最老）的每一项要么引用某个槽位 i，要么是「幽灵」（该数已不在 Larry
 *   记忆中），幽灵彼此只按位置区分。被叫数字以 1/10 落在每个槽位、每个幽灵上，以
 *   (10−k−g)/10 落在全新数字上；三类转移分别更新两方记忆与得分（叫到槽位时 Larry 必中，
 *   叫到幽灵时 Robin 必中）。Δ = L − R 每次只平移 ±1/0，第 j 步后 |Δ| ≤ j，只扫窗口
 *   [t−j, t+j]，最后 E|Δ| = Σ|δ|·P(Δ=δ)。可达状态仅 439 个；答案前 10 位小数为
 *   1.7688229423…，第 9 位小数是 2，舍入到 8 位无歧义。
 *
 * 复杂度：439 状态 × 101 宽 Δ 窗口 × 出度 ≤ 11 × 50 步 ≈ 1.3×10^7 次转移，本机 JIT 预热后
 *   约 14 ms；content 版另用「定义级 BigInteger 精确 DP」（与之相差 < 5e-15）、t ≤ 8 的全序列
 *   枚举与 10^7 条蒙特卡洛互证。远低于 10 s 熔断线。
 *
 * 本题没有素数筛 / 组合数 / gcd 等通用步骤（纯状态压缩 DP + 浮点期望），未用到 dev.pekt.math 工具。
 */

private const val CAP298 = 5            // 记忆容量
private const val DIGITS298 = 10        // 数字 1..10 共 10 种
private const val TURNS298 = 50         // 轮数
private const val POW7_5_298 = 16807    // 7^5：Robin 队列标签编码基数（0 = 幽灵，1..5 = 槽位）

/** 抽象状态编码：(k, m, Σ labels[j]·7^j)，k 为 Larry 槽位数、labels 为 Robin 队列（队首在前）。 */
private fun enc298Slot(k: Int, labels: IntArray): Int {
    var code = 0
    for (j in labels.indices.reversed()) code = code * 7 + labels[j]
    return (k * 6 + labels.size) * POW7_5_298 + code
}

private class SlotGraph298(
    val targets: Array<IntArray>,
    val probs: Array<DoubleArray>,
    val incL: Array<IntArray>,
    val incR: Array<IntArray>,
    val depth: IntArray,
) {
    val size: Int get() = targets.size
}

/**
 * 从空记忆出发 BFS 展开可达状态（439 个）；每条边的概率已折成 Double（分子/10）。
 * 出度 = k + 幽灵数 + (全新数字类 ≤ 1) ≤ 11。
 */
private fun build298SlotGraph(): SlotGraph298 {
    val index = HashMap<Int, Int>()
    val ks = ArrayList<Int>()
    val lbls = ArrayList<IntArray>()
    val depth = ArrayList<Int>()
    val tgt = ArrayList<IntArray>()
    val prob = ArrayList<DoubleArray>()
    val iL = ArrayList<IntArray>()
    val iR = ArrayList<IntArray>()

    fun intern(k: Int, labels: IntArray, dep: Int): Int {
        val key = enc298Slot(k, labels)
        val old = index[key]
        if (old != null) return old
        val id = ks.size
        index[key] = id
        ks.add(k)
        lbls.add(labels)
        depth.add(dep)
        tgt.add(IntArray(0))
        prob.add(DoubleArray(0))
        iL.add(IntArray(0))
        iR.add(IntArray(0))
        return id
    }

    intern(0, IntArray(0), 0)
    var cursor = 0
    while (cursor < ks.size) {
        val k = ks[cursor]
        val labels = lbls[cursor]
        val m = labels.size
        var ghosts = 0
        for (x in labels) if (x == 0) ghosts++
        val dep = depth[cursor] + 1
        val ts = ArrayList<Int>()
        val ps = ArrayList<Double>()
        val asL = ArrayList<Int>()
        val asR = ArrayList<Int>()

        fun add(t: Int, num: Int, l: Int, r: Int) {
            ts.add(t)
            ps.add(num * 0.1)
            asL.add(l)
            asR.add(r)
        }

        // (a) 叫到 Larry 槽位 i：Larry 必中；Robin 中与否看槽位 i 是否出现在队列里
        for (i in 1..k) {
            var robinHit = false
            for (x in labels) if (x == i) { robinHit = true; break }
            val out = ArrayList<Int>(m)
            for (x in labels) out.add(
                when {
                    x == 0 -> 0
                    x == i -> 1
                    x < i -> x + 1
                    else -> x
                },
            )
            if (!robinHit) {                 // 未中：Robin 把该数压队尾（满 5 丢队首）
                if (out.size == CAP298) out.removeAt(0)
                out.add(1)
            }
            add(intern(k, out.toIntArray(), dep), 1, 1, if (robinHit) 1 else 0)
        }
        // (b) 叫到第 p 位的幽灵：Robin 必中、Larry 未命中
        for (p in 0 until m) if (labels[p] == 0) {
            val out = IntArray(m)
            for (j in 0 until m) {
                val x = labels[j]
                out[j] = when {
                    x == 0 -> if (j == p) 1 else 0
                    k == CAP298 && x == CAP298 -> 0    // 槽位 5 被 Larry 淘汰 → 变幽灵
                    else -> x + 1
                }
            }
            add(intern(minOf(k + 1, CAP298), out, dep), 1, 0, 1)
        }
        // (c) 全新数字：两人都未命中
        val fresh = DIGITS298 - k - ghosts
        if (fresh > 0) {
            val out = ArrayList<Int>(m)
            for (x in labels) out.add(if (x == 0 || (k == CAP298 && x == CAP298)) 0 else x + 1)
            if (out.size == CAP298) out.removeAt(0)
            out.add(1)
            add(intern(minOf(k + 1, CAP298), out.toIntArray(), dep), fresh, 0, 0)
        }

        tgt[cursor] = ts.toIntArray()
        prob[cursor] = ps.toDoubleArray()
        iL[cursor] = asL.toIntArray()
        iR[cursor] = asR.toIntArray()
        cursor++
    }
    return SlotGraph298(
        tgt.toTypedArray(), prob.toTypedArray(), iL.toTypedArray(), iR.toTypedArray(), depth.toIntArray(),
    )
}

/** 推进 [turns] 步，返回 Δ = L − R 的边缘分布（长度 2·turns+1，索引 i ⇔ δ = i − turns）。 */
private fun dp298Delta(g: SlotGraph298, turns: Int): DoubleArray {
    val w = 2 * turns + 1
    val off = turns
    val s = g.size
    var dp = DoubleArray(s * w)
    var next = DoubleArray(s * w)
    dp[off] = 1.0 // 起点状态编号 0
    for (step in 0 until turns) {
        java.util.Arrays.fill(next, 0.0)
        val lo = maxOf(0, off - step)
        val hi = minOf(w - 1, off + step)
        for (st in 0 until s) {
            if (g.depth[st] > step) continue
            val base = st * w
            val tg = g.targets[st]
            val pb = g.probs[st]
            val l0 = g.incL[st]
            val r0 = g.incR[st]
            for (i in lo..hi) {
                val v = dp[base + i]
                if (v == 0.0) continue
                for (e in tg.indices) {
                    val j = i + l0[e] - r0[e]
                    if (j < 0 || j >= w) continue
                    next[tg[e] * w + j] += v * pb[e]
                }
            }
        }
        val tmp = dp
        dp = next
        next = tmp
    }
    val marg = DoubleArray(w)
    for (i in 0 until w) {
        var v = 0.0
        for (st in 0 until s) v += dp[st * w + i]
        marg[i] = v
    }
    return marg
}

internal fun solve0298Impl(): String {
    val graph = build298SlotGraph()
    val delta = dp298Delta(graph, TURNS298)
    var expected = 0.0
    for (i in delta.indices) {
        val p = delta[i]
        if (p != 0.0) expected += abs(i - TURNS298) * p
    }
    return String.format(Locale.ROOT, "%.8f", expected)
}
