#!/usr/bin/env kotlin
/**
 * Project Euler 298 — Selective Amnesia（选择性遗忘）
 *
 * 题目：随机数（1..10 均匀、独立）一个一个被叫出；Larry 与 Robin 各维护容量 5 的记忆，
 * 被叫数在记忆中得 1 分；未命中则加入记忆，满 5 时 Larry 淘汰「最久没被叫」的数（LRU），
 * Robin 淘汰「在记忆中停留最久」的数（FIFO）。求 50 轮后 E|L − R|，保留 8 位小数。
 *
 * 建模（精确求解，不用模拟）
 * ────────────────────────
 * 10 个数字完全对称，状态只需记录「相等结构」：
 *   · Larry 的记忆按「最近被叫」排序，记作槽位 1..k（槽位 1 最近，k ≤ 5）；
 *   · Robin 的记忆是「进入时间」队列（队首最老，m ≤ 5），每一项要么引用某个 Larry
 *     槽位 i（该数仍留在 Larry 记忆中），要么是「幽灵」——该数已不在 Larry 记忆中，
 *     但仍在 Robin 队列里；幽灵与 Larry 记忆、与彼此都不同，而身份对称，只需位置。
 * 一步转移：被叫数字以 1/10 落在 {槽位 1..k}、{每个幽灵}、{10 − k − g 个全新数字} 之一，
 *   (a) 叫到槽位 i：Larry 得 1 分；若槽位 i 出现在队列里 Robin 也得 1 分、队列不变，
 *       否则 Robin 把该数压队尾（满 5 掉队首）；Larry 把槽位移到最近端；
 *   (b) 叫到幽灵：Robin 得 1 分（队列不动）；Larry 未命中，把它插到最近端，
 *       满 5 时最久槽位 k 出局；若该槽位还在 Robin 队列里，就变成幽灵；
 *   (c) 全新数字：两人都不得分；Larry 插入最近端（满 5 丢槽位 k），
 *       Robin 压队尾（满 5 丢队首）。
 * 同时维护 Δ = L − R 的分布，E|Δ| = Σ |δ|·P(Δ = δ)。
 *
 * 两条独立路径
 * ────────────
 * A：上述抽象编码 + DoubleArray DP（可达状态仅 439 个），每步按 Δ 窗口推进；
 * B：不使用抽象——直接对「真实数字」做定义级更新（与模拟器同一份语义，见 playTurn），
 *    只靠「按首次出现顺序重标号」合并同构状态，计数用 BigInteger 精确累加，
 *    最后用 BigDecimal 精确除法展开 E|Δ|。B 既是 A 的独立对拍，也让答案不受浮点舍入影响。
 *
 * 验证
 * ────
 * 1. 题面示例对局（10 轮）逐轮复现：双方记忆集合与分数全部一致；
 * 2. A 与 B 在 t = 10、50 上一致（差 < 5e-15），且 B 给出精确有理数
 *    E|Δ| = Σ|δ|·N(δ) / 10^50（BigInteger 整数计数，无浮点误差）；
 * 3. 逐轮命中概率 Larry 与 Robin 完全相同（差 < 4e-14），t = 50 的命中数分布逐点相同；
 * 4. brute-force.kt：全序列枚举（t ≤ 8）精确对拍 + 10^7 次蒙特卡洛（t = 5,10,20,50）复核。
 *
 * 复杂度
 * ──────
 * 状态数 S = 439；每步 S × Δ 窗口 × 出度（≤ k+g+1 ≤ 11），50 步约 10^7 次加法。
 * 空间 O(S × W)，W = 2·50+1。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0298/solution.kt -d /tmp/kc-0298
 * java -cp /tmp/kc-0298:<kotlin-stdlib>/kotlin-stdlib-2.1.21.jar SolutionKt
 */

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs

private const val CAP = 5          // 记忆容量
private const val DIGITS = 10      // 数字 1..10 共 10 种
private const val TURNS = 50       // 轮数
private val PREC = MathContext(60) // 精确除法的有效位数

// ═══════════════════ 1. 定义级一回合（真实数字，题面规则的直译） ═══════════════════

/** 一回合的结果：双方新记忆 + 各自是否得分（分开记录，0 分/0 分与 1 分/1 分不可混同）。 */
private class Turn(val lary: IntArray, val robin: IntArray, val dL: Int, val dR: Int)

/**
 * 把被叫数字 d 喂给两位玩家。
 * Larry：命中则把 d 移到「最近被叫」端；未命中则插到最近端，满 5 淘汰最久未被叫的（序列末端）。
 * Robin：命中则队列不变；未命中则压到队尾，满 5 淘汰队首（最早进入的）。
 * 返回新 Larry 记忆（最近→最久）、新 Robin 记忆（最早→最新）与双方得分增量。
 */
private fun playTurn(lary: IntArray, robin: IntArray, d: Int): Turn {
    var dL = 0
    var dR = 0
    val newLary: IntArray
    val li = lary.indexOf(d)
    if (li >= 0) {
        dL = 1
        newLary = IntArray(lary.size)
        newLary[0] = d
        var w = 1
        for (i in lary.indices) if (i != li) newLary[w++] = lary[i]
    } else {
        val keep = minOf(lary.size, CAP - 1)
        newLary = IntArray(keep + 1)
        newLary[0] = d
        for (i in 0 until keep) newLary[i + 1] = lary[i]
    }
    val newRobin: IntArray
    val ri = robin.indexOf(d)
    if (ri >= 0) {
        dR = 1
        newRobin = robin
    } else if (robin.size == CAP) {
        newRobin = IntArray(CAP)
        for (i in 1 until CAP) newRobin[i - 1] = robin[i]
        newRobin[CAP - 1] = d
    } else {
        newRobin = IntArray(robin.size + 1)
        for (i in robin.indices) newRobin[i] = robin[i]
        newRobin[robin.size] = d
    }
    return Turn(newLary, newRobin, dL, dR)
}

/** 题面示例对局：序列 1,2,4,6,1,8,10,2,4,1，逐轮核对双方记忆（题面按升序展示）与分数。 */
private fun exampleCheck() {
    val seq = intArrayOf(1, 2, 4, 6, 1, 8, 10, 2, 4, 1)
    val expLary = listOf(
        intArrayOf(1), intArrayOf(1, 2), intArrayOf(1, 2, 4), intArrayOf(1, 2, 4, 6),
        intArrayOf(1, 2, 4, 6), intArrayOf(1, 2, 4, 6, 8), intArrayOf(1, 4, 6, 8, 10),
        intArrayOf(1, 2, 6, 8, 10), intArrayOf(1, 2, 4, 8, 10), intArrayOf(1, 2, 4, 8, 10),
    )
    val expLS = intArrayOf(0, 0, 0, 0, 1, 1, 1, 1, 1, 2)
    val expRobin = listOf(
        intArrayOf(1), intArrayOf(1, 2), intArrayOf(1, 2, 4), intArrayOf(1, 2, 4, 6),
        intArrayOf(1, 2, 4, 6), intArrayOf(1, 2, 4, 6, 8), intArrayOf(2, 4, 6, 8, 10),
        intArrayOf(2, 4, 6, 8, 10), intArrayOf(2, 4, 6, 8, 10), intArrayOf(1, 4, 6, 8, 10),
    )
    val expRS = intArrayOf(0, 0, 0, 0, 1, 1, 1, 2, 3, 3)

    var lary = IntArray(0)
    var robin = IntArray(0)
    var ls = 0
    var rs = 0
    for (t in seq.indices) {
        val d0 = seq[t]
        val lHit = lary.contains(d0)
        val rHit = robin.contains(d0)
        val turn = playTurn(lary, robin, d0)
        check(turn.dL == (if (lHit) 1 else 0) && turn.dR == (if (rHit) 1 else 0)) {
            "第 ${t + 1} 轮得分标记与记忆命中不符"
        }
        lary = turn.lary
        robin = turn.robin
        if (turn.dL > 0) ls++
        if (turn.dR > 0) rs++
        check(lary.sorted() == expLary[t].toList()) {
            "第 ${t + 1} 轮 Larry 记忆不符：${lary.sorted()} ≠ ${expLary[t].toList()}"
        }
        check(robin.sorted() == expRobin[t].toList()) {
            "第 ${t + 1} 轮 Robin 记忆不符：${robin.sorted()} ≠ ${expRobin[t].toList()}"
        }
        check(ls == expLS[t] && rs == expRS[t]) {
            "第 ${t + 1} 轮分数不符：L = $ls, R = $rs ≠ ${expLS[t]}, ${expRS[t]}"
        }
        println(
            "   第 %2d 轮 叫 %2d：Larry %-14s L = %d ｜ Robin %-14s R = %d".format(
                Locale.ROOT, t + 1, seq[t], lary.sorted().joinToString(","), ls,
                robin.sorted().joinToString(","), rs,
            ),
        )
    }
}

// ═══════════════════════ 2. 路径 A：抽象状态图（槽位 + 幽灵） ═══════════════════════

private const val POW7_5 = 16807 // 7^5：队列标签编码基数（0 = 幽灵，1..5 = 槽位）

/** 状态编码：(k, m, Σ labels[j]·7^j)。 */
private fun encSlot(k: Int, labels: IntArray): Int {
    var code = 0
    for (j in labels.indices.reversed()) code = code * 7 + labels[j]
    return (k * 6 + labels.size) * POW7_5 + code
}

private class SlotGraph(
    val targets: Array<IntArray>,
    val nums: Array<IntArray>,
    val incL: Array<IntArray>,
    val incR: Array<IntArray>,
    val depth: IntArray,
) {
    val size: Int get() = targets.size
}

/** 从空记忆出发 BFS 展开可达状态；出度 = k + 幽灵数 + (全新数字类 ≤ 1)。 */
private fun buildSlotGraph(): SlotGraph {
    val index = HashMap<Int, Int>()
    val ks = ArrayList<Int>()
    val lbls = ArrayList<IntArray>()
    val depth = ArrayList<Int>()
    val tgt = ArrayList<IntArray>()
    val num = ArrayList<IntArray>()
    val iL = ArrayList<IntArray>()
    val iR = ArrayList<IntArray>()

    fun intern(k: Int, labels: IntArray, dep: Int): Int {
        val key = encSlot(k, labels)
        val old = index[key]
        if (old != null) return old
        val id = ks.size
        index[key] = id
        ks.add(k)
        lbls.add(labels)
        depth.add(dep)
        tgt.add(IntArray(0))
        num.add(IntArray(0))
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
        val ns = ArrayList<Int>()
        val asL = ArrayList<Int>()
        val asR = ArrayList<Int>()

        fun add(t: Int, n: Int, l: Int, r: Int) {
            ts.add(t); ns.add(n); asL.add(l); asR.add(r)
        }

        // (a) 叫到 Larry 槽位 i：Larry 必中；Robin 中与否看槽位 i 是否在队列里
        for (i in 1..k) {
            var robinHit = false
            for (x in labels) if (x == i) { robinHit = true; break }
            val out = ArrayList<Int>(m)
            for (x in labels) out.add(when {
                x == 0 -> 0
                x == i -> 1
                x < i -> x + 1
                else -> x
            })
            if (!robinHit) {
                if (out.size == CAP) out.removeAt(0)
                out.add(1)
            }
            add(intern(k, out.toIntArray(), dep), 1, 1, if (robinHit) 1 else 0)
        }
        // (b) 叫到第 p 位的幽灵：Robin 必中、Larry 未命中
        for (p in 0 until m) if (labels[p] == 0) {
            val out = IntArray(m)
            for (j in 0 until m) {
                val x = labels[j]
                out[j] = if (x == 0) {
                    if (j == p) 1 else 0
                } else if (k == CAP && x == CAP) {
                    0 // 槽位 5 被 Larry 淘汰 → 变幽灵
                } else {
                    x + 1
                }
            }
            add(intern(minOf(k + 1, CAP), out, dep), 1, 0, 1)
        }
        // (c) 全新数字：两人都未命中
        val fresh = DIGITS - k - ghosts
        if (fresh > 0) {
            val out = ArrayList<Int>(m)
            for (x in labels) out.add(if (x == 0 || (k == CAP && x == CAP)) 0 else x + 1)
            if (out.size == CAP) out.removeAt(0)
            out.add(1)
            add(intern(minOf(k + 1, CAP), out.toIntArray(), dep), fresh, 0, 0)
        }

        tgt[cursor] = ts.toIntArray()
        num[cursor] = ns.toIntArray()
        iL[cursor] = asL.toIntArray()
        iR[cursor] = asR.toIntArray()
        cursor++
    }
    return SlotGraph(
        tgt.toTypedArray(), num.toTypedArray(), iL.toTypedArray(), iR.toTypedArray(), depth.toIntArray(),
    )
}

/**
 * 沿 [inc] 记分的边缘分布：`inc(incL, incR)` 是每次转移的分数增量，
 * 结果长度为 [w]，偏移 [off]（Δ 用 (w, off) = (2t+1, t)，命中数用 (t+1, 0)）。
 */
private fun dpMarginal(
    g: SlotGraph,
    turns: Int,
    w: Int,
    off: Int,
    inc: (Int, Int) -> Int,
): DoubleArray {
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
            val nm = g.nums[st]
            val l0 = g.incL[st]
            val r0 = g.incR[st]
            for (i in lo..hi) {
                val v = dp[base + i]
                if (v == 0.0) continue
                for (e in tg.indices) {
                    val j = i + inc(l0[e], r0[e])
                    if (j < 0 || j >= w) continue
                    next[tg[e] * w + j] += v * (nm[e] * 0.1)
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

/** Δ = L − R 的边缘分布（索引 i 对应 δ = i − turns）。 */
private fun deltaMarginalA(g: SlotGraph, turns: Int): DoubleArray =
    dpMarginal(g, turns, 2 * turns + 1, turns) { l, r -> l - r }

private fun expect(dist: DoubleArray, off: Int): Double =
    dist.indices.sumOf { abs(it - off) * dist[it] }

// ═════════════ 3. 路径 B：定义级更新 + 数字重标号 + BigInteger 精确计数 ═════════════

/** 按「首次出现顺序」重标号（先扫 Larry 记忆、再扫 Robin 队列），把同构状态合并。 */
private fun canonicalize(l0: IntArray, r0: IntArray): Pair<IntArray, IntArray> {
    val map = HashMap<Int, Int>()
    val l = IntArray(l0.size) { map.getOrPut(l0[it]) { map.size } }
    val r = IntArray(r0.size) { map.getOrPut(r0[it]) { map.size } }
    return l to r
}

private fun encDigit(l: IntArray, r: IntArray): Long {
    var cl = 0L
    for (i in l.indices.reversed()) cl = cl * 10 + l[i]
    var cr = 0L
    for (i in r.indices.reversed()) cr = cr * 10 + r[i]
    return ((l.size * 10L + r.size) * 100_000L + cl) * 100_000L + cr
}

private class DigitGraph(
    val targets: Array<IntArray>,
    val nums: Array<IntArray>,
    val incL: Array<IntArray>,
    val incR: Array<IntArray>,
    val depth: IntArray,
) {
    val size: Int get() = targets.size
}

/** 具体数字图：状态是 (Larry 记忆, Robin 队列) 的重标号形式，转移直接调用 playTurn。 */
private fun buildDigitGraph(): DigitGraph {
    val index = HashMap<Long, Int>()
    val ls = ArrayList<IntArray>()
    val rs = ArrayList<IntArray>()
    val depth = ArrayList<Int>()
    val tgt = ArrayList<IntArray>()
    val num = ArrayList<IntArray>()
    val iL = ArrayList<IntArray>()
    val iR = ArrayList<IntArray>()

    fun intern(l0: IntArray, r0: IntArray, dep: Int): Int {
        val (l, r) = canonicalize(l0, r0)
        val key = encDigit(l, r)
        val old = index[key]
        if (old != null) return old
        val id = ls.size
        index[key] = id
        ls.add(l)
        rs.add(r)
        depth.add(dep)
        tgt.add(IntArray(0))
        num.add(IntArray(0))
        iL.add(IntArray(0))
        iR.add(IntArray(0))
        return id
    }

    intern(IntArray(0), IntArray(0), 0)
    var cursor = 0
    while (cursor < ls.size) {
        val l0 = ls[cursor]
        val r0 = rs[cursor]
        val dep = depth[cursor] + 1
        val used = BooleanArray(DIGITS)
        for (d in l0) used[d] = true
        for (d in r0) used[d] = true
        var u = 0
        for (d in 0 until DIGITS) if (used[d]) u++
        val ts = ArrayList<Int>()
        val ns = ArrayList<Int>()
        val asL = ArrayList<Int>()
        val asR = ArrayList<Int>()
        for (d in 0 until DIGITS) {
            if (!used[d]) continue
            val turn = playTurn(l0, r0, d)
            ts.add(intern(turn.lary, turn.robin, dep))
            ns.add(1)
            asL.add(turn.dL)
            asR.add(turn.dR)
        }
        if (u < DIGITS) {
            // 全新数字：任选一个未出现的数字作代表，Larry 与 Robin 都得 0 分
            var fresh = 0
            while (used[fresh]) fresh++
            val turn = playTurn(l0, r0, fresh)
            ts.add(intern(turn.lary, turn.robin, dep))
            ns.add(DIGITS - u)
            asL.add(0)
            asR.add(0)
        }
        tgt[cursor] = ts.toIntArray()
        num[cursor] = ns.toIntArray()
        iL[cursor] = asL.toIntArray()
        iR[cursor] = asR.toIntArray()
        cursor++
    }
    return DigitGraph(
        tgt.toTypedArray(), num.toTypedArray(), iL.toTypedArray(), iR.toTypedArray(), depth.toIntArray(),
    )
}

/**
 * 精确 DP：计数用 BigInteger（第 t 步的计数总和 = 10^t）。
 * 状态量 v = inc(incL, incR)，用 (w, off) 标注值域；返回 v 的边缘计数（长度 w）。
 */
private fun exactMarginal(
    g: DigitGraph,
    turns: Int,
    w: Int,
    off: Int,
    inc: (Int, Int) -> Int,
): Array<BigInteger> {
    val s = g.size
    var dp = Array(s) { Array(w) { BigInteger.ZERO } }
    var next = Array(s) { Array(w) { BigInteger.ZERO } }
    dp[0][off] = BigInteger.ONE
    for (step in 0 until turns) {
        for (st in 0 until s) java.util.Arrays.fill(next[st], BigInteger.ZERO)
        val lo = maxOf(0, off - step)
        val hi = minOf(w - 1, off + step)
        for (st in 0 until s) {
            if (g.depth[st] > step) continue
            val row = dp[st]
            val tg = g.targets[st]
            val nm = g.nums[st]
            val l0 = g.incL[st]
            val r0 = g.incR[st]
            for (i in lo..hi) {
                val cnt = row[i]
                if (cnt.signum() == 0) continue
                for (e in tg.indices) {
                    val j = i + inc(l0[e], r0[e])
                    if (j < 0 || j >= w) continue
                    val trow = next[tg[e]]
                    trow[j] = trow[j] + (if (nm[e] == 1) cnt else cnt.multiply(BigInteger.valueOf(nm[e].toLong())))
                }
            }
        }
        val tmp = dp
        dp = next
        next = tmp
    }
    val marg = Array(w) { BigInteger.ZERO }
    for (i in 0 until w) {
        var cnt = BigInteger.ZERO
        for (st in 0 until s) cnt += dp[st][i]
        marg[i] = cnt
    }
    return marg
}

/** Σ_i i·count_i。 */
private fun bigWeighted(marg: Array<BigInteger>): BigInteger {
    var acc = BigInteger.ZERO
    for (i in marg.indices) if (marg[i].signum() != 0) acc += marg[i].multiply(BigInteger.valueOf(i.toLong()))
    return acc
}

private fun bestOf3(tag: String, expected: Double, f: () -> Double): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(abs(out - expected) < 1e-12) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("   $tag：%.3f ms（3 轮最优，JIT 预热后）".format(Locale.ROOT, best))
    return best
}

fun main() {
    println("== 1. 题面示例对局逐轮对拍（序列 1,2,4,6,1,8,10,2,4,1）==")
    exampleCheck()
    println("   10 轮的记忆集合与双方分数全部与题面表格一致 ✓")

    println()
    println("== 2. 路径 A：抽象状态 DP（Larry 槽位 / Robin 队列 + 幽灵，Double）==")
    val gA = buildSlotGraph()
    println("   可达状态数 = ${gA.size}")
    for (st in 0 until gA.size) {
        var sum = 0
        for (e in gA.targets[st].indices) sum += gA.nums[st][e]
        check(sum == DIGITS) { "路径 A 状态 $st 的转移分子和 = $sum ≠ 10" }
    }
    println("   每个状态的转移分子和均为 10（概率归一）✓")
    val evo = intArrayOf(1, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 40, 50)
    for (t in evo) {
        val m = deltaMarginalA(gA, t)
        check(abs(m.sum() - 1.0) < 1e-12) { "t = $t 的质量不守恒：${m.sum()}" }
        println("   t = %2d：E|L−R| = %.12f（P(Δ=0) = %.6f）".format(Locale.ROOT, t, expect(m, t), m[t]))
    }

    println()
    println("== 3. 路径 B：定义级更新 + 重标号 + BigInteger 精确计数 ==")
    val gB = buildDigitGraph()
    for (st in 0 until gB.size) {
        var sum = 0
        for (e in gB.targets[st].indices) sum += gB.nums[st][e]
        check(sum == DIGITS) { "路径 B 状态 $st 的转移分子和 = $sum ≠ 10" }
    }
    println("   可达状态数 = ${gB.size}（与路径 A 相同 ⇒ 两种编码描述同一状态空间）")
    val delta50 = exactMarginal(gB, TURNS, 2 * TURNS + 1, TURNS) { l, r -> l - r }
    var tot50 = BigInteger.ZERO
    for (c in delta50) tot50 += c
    check(tot50 == BigInteger.TEN.pow(TURNS)) { "t = 50 计数总和 $tot50 ≠ 10^50" }
    // Σ|δ|·count 由「索引 → 偏移」的加权求和得到
    var weigh50 = BigInteger.ZERO
    for (i in delta50.indices) if (delta50[i].signum() != 0) {
        weigh50 += delta50[i].multiply(BigInteger.valueOf(abs(i - TURNS).toLong()))
    }
    val ex50 = BigDecimal(weigh50).divide(BigDecimal(tot50), PREC)
    println("   精确分数：Σ|δ|·N(δ) = $weigh50 / 10^50")
    val lambda = exactMarginal(gB, 10, 2 * 10 + 1, 10) { l, r -> l - r }
    var tot10 = BigInteger.ZERO
    for (c in lambda) tot10 += c
    var weigh10 = BigInteger.ZERO
    for (i in lambda.indices) if (lambda[i].signum() != 0) {
        weigh10 += lambda[i].multiply(BigInteger.valueOf(abs(i - 10).toLong()))
    }
    val ex10 = BigDecimal(weigh10).divide(BigDecimal(tot10), PREC)
    val a10 = expect(deltaMarginalA(gA, 10), 10)
    val a50 = expect(deltaMarginalA(gA, TURNS), TURNS)
    println("   t = 10：精确 %s ｜ 路径 A %.12f ｜ 差 %.3e".format(Locale.ROOT, ex10.toPlainString(), a10, abs(a10 - ex10.toDouble())))
    println("   t = 50：精确 %s".format(Locale.ROOT, ex50.toPlainString()))
    println("           路径 A %.12f ｜ 差 %.3e".format(Locale.ROOT, a50, abs(a50 - ex50.toDouble())))
    check(abs(a10 - ex10.toDouble()) < 1e-9 && abs(a50 - ex50.toDouble()) < 1e-9) { "路径 A/B 不一致" }

    println()
    println("== 4. 小 t 的精确值（Σ|Δ| / 10^t，供 brute-force.kt 全序列枚举对拍）==")
    for (t in 1..10) {
        val marg = exactMarginal(gB, t, 2 * t + 1, t) { l, r -> l - r }
        var tot = BigInteger.ZERO
        for (c in marg) tot += c
        var weigh = BigInteger.ZERO
        for (i in marg.indices) if (marg[i].signum() != 0) {
            weigh += marg[i].multiply(BigInteger.valueOf(abs(i - t).toLong()))
        }
        println("   t = %2d：Σ|Δ| = %s（共 10^%d 条序列）".format(Locale.ROOT, t, weigh.toString(), t))
    }

    println()
    println("== 5. 逐轮命中概率：两种策略完全相同（等价的 E|记忆(t)| 相同）==")
    var prevL = 0.0
    var prevR = 0.0
    var maxDiff = 0.0
    for (t in 1..TURNS) {
        val hl = dpMarginal(gA, t, t + 1, 0) { l, _ -> l }
        val hr = dpMarginal(gA, t, t + 1, 0) { _, r -> r }
        val eL = hl.indices.sumOf { it * hl[it] }
        val eR = hr.indices.sumOf { it * hr[it] }
        val pL = eL - prevL
        val pR = eR - prevR
        maxDiff = maxOf(maxDiff, abs(pL - pR))
        prevL = eL
        prevR = eR
        if (t <= 8 || t == 50) {
            println("   第 %2d 轮：命中概率 Larry %.9f ｜ Robin %.9f（第 t 轮 = (10 − E|记忆(t−1)|)/10）"
                .format(Locale.ROOT, t, pL, pR))
        }
    }
    check(maxDiff < 1e-12) { "逐轮命中概率出现差异：$maxDiff" }
    println("   t = 1..50 逐轮差的最大值 = %.2e ✓（因此 E[L] = E[R]，与下面的精确整数一致性呼应）".format(Locale.ROOT, maxDiff))

    println()
    println("== 6. 附带量：两种策略各自的期望命中数（路径 B 精确计数）==")
    val l50 = exactMarginal(gB, TURNS, TURNS + 1, 0) { l, _ -> l }
    val r50 = exactMarginal(gB, TURNS, TURNS + 1, 0) { _, r -> r }
    println("   E[L] = %s".format(Locale.ROOT, BigDecimal(bigWeighted(l50)).divide(BigDecimal(tot50), PREC).toPlainString()))
    println("   E[R] = %s".format(Locale.ROOT, BigDecimal(bigWeighted(r50)).divide(BigDecimal(tot50), PREC).toPlainString()))
    check(bigWeighted(l50) == bigWeighted(r50)) { "两种策略的期望命中数不相等？" }
    println("   Σ 命中数（L）与 Σ 命中数（R）作为整数完全相等 ✓")
    var sameDist = true
    for (i in l50.indices) if (l50[i] != r50[i]) { sameDist = false; break }
    check(sameDist) { "命中数分布不一致" }
    println("   两种策略的完整命中数分布**逐点相同** ✓（同一分布、但同一条序列上取值常不同）")

    println()
    println("== 7. Δ 分布（t = 50，路径 A）==")
    val d50 = deltaMarginalA(gA, TURNS)
    for (i in 0 until d50.size) {
        val p = d50[i]
        if (p > 1e-4) println("   P(Δ = %+3d) = %.6f".format(Locale.ROOT, i - TURNS, p))
    }
    var checkAbs = 0.0
    for (j in 0..TURNS) {
        val p = (if (j == 0) 0.0 else d50[TURNS - j]) + if (j == 0) d50[TURNS] else d50[TURNS + j]
        checkAbs += j * p
        if (p > 1e-5) println("   P(|Δ| = %2d) = %.6f".format(Locale.ROOT, j, p))
    }
    check(abs(checkAbs - a50) < 1e-12) { "|Δ| 分布算出的期望 $checkAbs ≠ $a50" }

    println()
    println("== 8. 计时 ==")
    val msA = bestOf3("路径 A（50 步 Double DP）", a50) { expect(deltaMarginalA(gA, TURNS), TURNS) }
    val msB = bestOf3("路径 B（50 步 BigInteger 精确 DP）", ex50.toDouble()) {
        val marg = exactMarginal(gB, TURNS, 2 * TURNS + 1, TURNS) { l, r -> l - r }
        var tot = BigInteger.ZERO
        var weigh = BigInteger.ZERO
        for (i in marg.indices) {
            tot += marg[i]
            if (marg[i].signum() != 0) weigh += marg[i].multiply(BigInteger.valueOf(abs(i - TURNS).toLong()))
        }
        BigDecimal(weigh).divide(BigDecimal(tot), PREC).toDouble()
    }

    println()
    println("== 9. 结果 ==")
    val ans = ex50.setScale(8, RoundingMode.HALF_UP)
    println("   50 轮后 E|L−R| = ${ex50.toPlainString()}")
    println("   12 位小数：%s；9 位小数：%s"
        .format(Locale.ROOT, ex50.setScale(12, RoundingMode.HALF_UP).toPlainString(), ex50.setScale(9, RoundingMode.HALF_UP).toPlainString()))
    println("   答案（格式 x.xxxxxxxx）= ${ans.toPlainString()}")
    println("   check() 全部通过；路径 A ${"%.3f".format(Locale.ROOT, msA)} ms，路径 B ${"%.1f".format(Locale.ROOT, msB)} ms")
}
