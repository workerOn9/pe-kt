#!/usr/bin/env kotlin
/**
 * Project Euler 280 — Ant and Seeds（蚂蚁与种子）：暴力 / 数值对照
 * 独立实现，与 solution.kt 不共享核心代码（状态编码、转移、求解全都另写一套）。
 *
 * 路径 1（全状态空间数值解，meta 的 bruteForceBaselineMs 口径）
 * ────────────────────────────────────────────────────────
 * 不用 solution.kt 的「按 (剩余种子, 已投放) 分层」技巧，直接把整个系统当一个大方程组：
 * 状态 = (蚂蚁坐标 (x, y), 11 位「种子下落」掩码)，掩码恰有 5 位为 1：
 *   第 0–4 位 = 下排各格是否还有种子；第 5–9 位 = 上排各格是否已投放；第 10 位 = 蚂蚁手上。
 * 候选 C(11,5) × 25 = 11550 个，从起点 BFS 出真正可达（自洽）的状态得到 10270 个——
 * 这个数与公开解法注释里给出的「10270 valid states」一致，本身就是一处锚点。
 *
 * 然后在整个状态空间上做值迭代（Jacobi）：
 *   V_0 ≡ 0，V_{k+1}(s) = 1 + (1/deg) Σ_{s′} V_k(s′)，终局状态无后继（V ≡ 0）。
 * 因为 V_k(s) = E[min(T, k)]，V_k 单调收敛到期望步数，每轮最大增量恰为 P(T ≥ k)。
 *
 * 路径 2（蒙特卡洛直接模拟）
 * ────────────────────────
 * 一步一步按题面规则模拟；多线程跑 10^8 个样本给出 95% 置信区间。标准误 ~ σ/√n
 * （实测 σ ≈ 128），蒙特卡洛只能把答案钉到两位小数；6 位小数级别由路径 1 与
 * solution.kt 的三条精确路径互证。
 *
 * 同一框架下再跑 K=3（3 粒种子）规模：两条路径都要复现 solution.kt 给出的
 * 64.09608737743095，证明规则实现与研究规模无关。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0280/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/** 编码口径：round(期望值 × 10^6)（题面要求 6 位小数），与 solution.kt / meta.json 一致。 */
private const val SCALE = 1_000_000L

// ─────────────────────────── 状态空间（独立编码） ───────────────────────────

private class Space(val k: Int) {
    val nPos = k * k
    val handBit = 2 * k
    val allBottom = (1 shl k) - 1
    val allTopMask = ((1 shl k) - 1) shl k
    val start = (k / 2) * k + k / 2

    fun neighbors(p: Int): IntArray {
        val r = p / k
        val c = p % k
        val out = IntArray(4)
        var n = 0
        if (r > 0) out[n++] = p - k
        if (r < k - 1) out[n++] = p + k
        if (c > 0) out[n++] = p - 1
        if (c < k - 1) out[n++] = p + 1
        return out.copyOf(n)
    }
}

/** 走到 q 后的新掩码；-1 表示这一步放下最后一粒种子（过程结束）。 */
private fun afterMove(space: Space, mask: Int, q: Int): Int {
    val k = space.k
    val row = q / k
    val col = q % k
    val hand = (mask shr space.handBit) and 1 == 1
    if (!hand) {
        if (row == 0 && (mask shr col) and 1 == 1) {                  // 拾起
            return mask and (1 shl col).inv() or (1 shl space.handBit)
        }
    } else if (row == k - 1 && ((mask shr (k + col)) and 1) == 0) {   // 放下
        val next = mask and (1 shl space.handBit).inv() or (1 shl (k + col))
        if (next and space.allTopMask == space.allTopMask) return -1  // 全部投放完毕
        return next
    }
    return mask
}

/** 从起点 BFS 出可达状态图；状态编号紧凑，后继表预计算。 */
private class Graph(val space: Space) {
    val k = space.k
    val nPos = space.nPos
    val masks = ArrayList<Int>()
    val poss = ArrayList<Int>()
    val succ = ArrayList<IntArray>()
    val index = HashMap<Int, Int>()
    var startState = -1

    private fun idOf(mask: Int, pos: Int): Int {
        val key = mask * nPos + pos
        val existing = index[key]
        if (existing != null) return existing
        val id = masks.size
        masks.add(mask)
        poss.add(pos)
        succ.add(IntArray(0))
        index[key] = id
        return id
    }

    private fun isDone(mask: Int): Boolean =
        (mask and space.allTopMask) == space.allTopMask && ((mask shr space.handBit) and 1) == 0

    init {
        startState = idOf(space.allBottom, space.start)
        val enqueued = ArrayList<Boolean>()
        while (enqueued.size < masks.size) enqueued.add(false)
        val queue = ArrayList<Int>()
        queue.add(startState)
        enqueued[startState] = true
        var head = 0
        while (head < queue.size) {
            val s = queue[head++]
            val mask = masks[s]
            val p = poss[s]
            if (isDone(mask)) continue                              // 终局：无后继（V ≡ 0）
            val out = ArrayList<Int>(4)
            for (q in space.neighbors(p)) {
                val nm = afterMove(space, mask, q)
                // 关键：放下最后一粒种子时，这一步仍要占「后继」的一个名额（值为 0 的终局态），
                // 否则该状态的平均权重会从 1/deg 变成 1/(deg−1)，结果必错。
                val t = if (nm < 0) idOf(space.allTopMask, q) else idOf(nm, q)
                out.add(t)
                while (enqueued.size <= t) enqueued.add(false)
                if (!enqueued[t]) {
                    enqueued[t] = true
                    queue.add(t)
                }
            }
            succ[s] = out.toIntArray()
        }
    }

    /** 题面规则下的「自洽」状态集合（独立于 BFS）：不携带时不在有种子的下排格、携带时不在空的上排格、
     *  全部投放后蚂蚁只能在上排。用来与 BFS 可达集合互相印证。 */
    fun validStatesFromDefinition(): Set<Int> {
        val valid = HashSet<Int>()
        val bits = 2 * k + 1
        for (mask in 0 until (1 shl bits)) {
            if (Integer.bitCount(mask) != k) continue
            val hand = (mask shr space.handBit) and 1 == 1
            val allTop = (mask and space.allTopMask) == space.allTopMask
            for (pos in 0 until nPos) {
                val row = pos / k
                val col = pos % k
                if (!hand && row == 0 && (mask shr col) and 1 == 1) continue
                if (hand && row == k - 1 && ((mask shr (k + col)) and 1) == 0) continue
                if (allTop && !hand && row != k - 1) continue
                valid.add(mask * nPos + pos)
            }
        }
        return valid
    }
}

// ─────────────────────── 路径 1：全状态空间值迭代（Jacobi） ───────────────────────

private class JacobiResult(val value: Double, val iterations: Int, val lastDelta: Double, val states: Int)

private fun jacobi(graph: Graph, targetDelta: Double, maxIter: Int): JacobiResult {
    val n = graph.masks.size
    val weight = DoubleArray(n) { if (graph.succ[it].isEmpty()) 0.0 else 1.0 / graph.succ[it].size }
    var v = DoubleArray(n)
    var nv = DoubleArray(n)
    var iter = 0
    var delta = Double.MAX_VALUE
    while (iter < maxIter) {
        var md = 0.0
        for (s in 0 until n) {
            val sc = graph.succ[s]
            if (sc.isEmpty()) { nv[s] = 0.0; continue }
            var acc = 1.0
            val w = weight[s]
            for (t in sc) acc += w * v[t]
            nv[s] = acc
            val d = abs(acc - v[s])
            if (d > md) md = d
        }
        val tmp = v
        v = nv
        nv = tmp
        iter++
        delta = md
        if (md < targetDelta) break
    }
    return JacobiResult(v[graph.startState], iter, delta, n)
}

// ───────────────────────── 路径 2：蒙特卡洛直接模拟 ─────────────────────────

/** 轻量 xorshift64：below(bound) 用 2 位拒绝采样（bound ≤ 4，正确且够快）。 */
private class XorShift(seed: Long) {
    private var s = seed or 0x9E3779B97F4A7C15uL.toLong()
    fun next(): Long {
        s = s xor (s shl 13)
        s = s xor (s ushr 7)
        s = s xor (s shl 17)
        return s
    }

    fun below(bound: Int): Int {
        while (true) {
            val r = (next() ushr 34).toInt() and 3
            if (r < bound) return r
        }
    }
}

/** 按题面规则模拟一次，返回总步数。 */
private fun simulate(k: Int, start: Int, space: Space, nb: Array<IntArray>, rng: XorShift): Long {
    var mask = space.allBottom
    var pos = start
    var steps = 0L
    while (true) {
        val list = nb[pos]
        pos = list[rng.below(list.size)]
        steps++
        val nm = afterMove(space, mask, pos)
        if (nm < 0) return steps
        mask = nm
    }
}

private class McResult(val mean: Double, val stdErr: Double, val samples: Long)

private fun monteCarlo(k: Int, samples: Long, threads: Int, seed: Long): McResult {
    val space = Space(k)
    val nb = Array(space.nPos) { space.neighbors(it) }
    val sums = DoubleArray(threads)
    val sqs = DoubleArray(threads)
    val counts = LongArray(threads)
    val perThread = samples / threads
    val workers = (0 until threads).map { t ->
        thread {
            val rng = XorShift(seed + 0x9E3779B9L * (t + 1))
            var s = 0.0
            var q = 0.0
            val n = if (t == threads - 1) samples - perThread * (threads - 1) else perThread
            for (i in 0 until n) {
                val v = simulate(k, space.start, space, nb, rng).toDouble()
                s += v
                q += v * v
            }
            sums[t] = s
            sqs[t] = q
            counts[t] = n
        }
    }
    workers.forEach { it.join() }
    var sum = 0.0
    var sumSq = 0.0
    var n = 0L
    for (t in 0 until threads) {
        sum += sums[t]
        sumSq += sqs[t]
        n += counts[t]
    }
    val mean = sum / n
    val variance = ((sumSq - n * mean * mean) / (n - 1)).coerceAtLeast(0.0)
    return McResult(mean, sqrt(variance / n), n)
}

// ─────────────────────────────── 计时工具 ───────────────────────────────

private fun bestOf3(tag: String, expected: Double, tol: Double, f: () -> Double): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(abs(out - expected) < tol) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("  $tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

private fun fmt(x: Double) = "%.9f".format(x)

fun main() {
    // solution.kt 三条精确路径的一致输出，作为本文件的对照基准
    val expectedMain = 430.088246716688          // K = 5、5 粒种子（原题）
    val expectedSmall = 64.09608737743095        // K = 3、3 粒种子（同规则缩小规模）

    // ---------- 状态空间锚点 ----------
    val g5 = Graph(Space(5))
    val valid5 = g5.validStatesFromDefinition()
    val reachable5 = g5.index.keys.toSet()
    println("状态空间锚点：候选 C(11,5)×25 = 11550；按自洽性定义筛出 ${valid5.size} 个，起点 BFS 可达 " +
        "${reachable5.size} 个，两个集合相等 = ${valid5 == reachable5}" +
        "（与公开解法注释里的「10270 valid states」一致）")
    check(valid5 == reachable5 && valid5.size == 10270) { "状态集合与自洽性定义不一致" }

    val g3 = Graph(Space(3))
    val valid3 = g3.validStatesFromDefinition()
    println("K=3：候选 C(7,3)×9 = 315，自洽状态 = ${valid3.size}，BFS 可达 = ${g3.index.size}，" +
        "两个集合相等 = ${valid3 == g3.index.keys.toSet()}")
    check(valid3 == g3.index.keys.toSet()) { "K=3 状态集合与自洽性定义不一致" }

    // ---------- 路径 1：全状态空间值迭代 ----------
    val j5 = jacobi(g5, 1e-11, 5_000_000)
    println("路径 1（K=5 全状态空间 Jacobi）：${j5.states} 个状态迭代 ${j5.iterations} 轮，" +
        "末轮增量 ${"%.2e".format(j5.lastDelta)}，期望步数 = ${fmt(j5.value)}")
    check(abs(j5.value - expectedMain) < 1e-7) { "K=5 值迭代 ${j5.value} 与基准 $expectedMain 不符" }

    // 停机精度自查：再迭代到更严的阈值，检查答案变化
    val j5b = jacobi(g5, 1e-13, 5_000_000)
    check(abs(j5b.value - j5.value) < 1e-9) { "K=5 值迭代停机阈值放宽/收紧后答案变化过大" }
    println("  停机阈值 1e-11 → 1e-13 后答案变化 = ${"%.2e".format(abs(j5b.value - j5.value))}（证明已收敛）")

    val j3 = jacobi(g3, 1e-13, 5_000_000)
    println("路径 1（K=3 全状态空间 Jacobi）：${j3.states} 个状态迭代 ${j3.iterations} 轮，" +
        "期望步数 = ${fmt(j3.value)}")
    check(abs(j3.value - expectedSmall) < 1e-8) { "K=3 值迭代 ${j3.value} 与基准 $expectedSmall 不符" }

    // ---------- 路径 2：蒙特卡洛 ----------
    val threads = 8
    val mcT0 = System.nanoTime()
    val mc5 = monteCarlo(5, 100_000_000L, threads, 280_280L)
    val mcMs = (System.nanoTime() - mcT0) / 1e6
    val lo5 = mc5.mean - 1.96 * mc5.stdErr
    val hi5 = mc5.mean + 1.96 * mc5.stdErr
    println("路径 2（K=5 蒙特卡洛 ${mc5.samples} 样本 / $threads 线程，用时 ${"%.1f".format(mcMs)} ms）：" +
        "均值 ${fmt(mc5.mean)}，95% CI [${fmt(lo5)}, ${fmt(hi5)}]（标准误 ${"%.4f".format(mc5.stdErr)}）")
    check(lo5 <= expectedMain && expectedMain <= hi5) { "实跑值不在 MC 置信区间内" }

    val mc3 = monteCarlo(3, 5_000_000L, threads, 280_303L)
    val lo3 = mc3.mean - 1.96 * mc3.stdErr
    val hi3 = mc3.mean + 1.96 * mc3.stdErr
    println("路径 2（K=3 蒙特卡洛 ${mc3.samples} 样本）：均值 ${fmt(mc3.mean)}，" +
        "95% CI [${fmt(lo3)}, ${fmt(hi3)}]（标准误 ${"%.4f".format(mc3.stdErr)}）")
    check(lo3 <= expectedSmall && expectedSmall <= hi3) { "K=3 实跑值不在 MC 置信区间内" }

    // ---------- 计时（meta 的 bruteForceBaselineMs 取全状态空间值迭代） ----------
    jacobi(g5, 1e-11, 5_000_000)
    val msJ = bestOf3("路径 1 全状态空间 Jacobi（K=5，10270 状态 × ~2 万轮）", expectedMain, 1e-7) {
        jacobi(g5, 1e-11, 5_000_000).value
    }
    val t0 = System.nanoTime()
    monteCarlo(5, 4_000_000L, 1, 999L)
    val msMc1 = (System.nanoTime() - t0) / 1e6
    println("  （参考）蒙特卡洛单线程 4×10^6 样本 ≈ ${"%.0f".format(msMc1)} ms；" +
        "本文件 10^8 样本用 $threads 线程并行，仅作统计旁证，不计入 baseline")

    // ---------- 编码与总结 ----------
    val encoded = Math.round(j5.value * SCALE)
    println()
    println("暴力/数值解期望步数 = ${fmt(j5.value)}")
    println("答案 = $encoded（编码：round(期望值 × 10^6)）")
    check(encoded == 430088247L) { "编码答案 $encoded ≠ 430088247" }
    check(Math.round(expectedSmall * SCALE) == Math.round(j3.value * SCALE))
    println("汇总：全状态空间 Jacobi ${"%.0f".format(msJ)} ms（meta baseline 口径）；" +
        "蒙特卡洛 10^8 样本 ${"%.0f".format(mcMs)} ms（$threads 线程）均值 ${fmt(mc5.mean)} ± " +
        "${"%.3f".format(1.96 * mc5.stdErr)}")
    println("check() 全部通过")
}
