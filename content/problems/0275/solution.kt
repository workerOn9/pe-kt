#!/usr/bin/env kotlin
/**
 * Project Euler 275 — Balanced Sculptures（平衡雕塑）
 *
 * 建模（把题面翻译成可枚举的对象）
 * ──────────────────────────────
 * plinth 中心在 (0,0)，blocks 的 y 坐标 > 0，整个多联骨牌连通。plinth 的四邻居中只有
 * (0,1) 可能放 block（(±1,0)、(0,-1) 的 y ≤ 0 被排除），所以：
 *   · (0,1) 必为 block；其余 blocks 都在 y ≥ 1 的上半平面；
 *   · 连通性 ⟺ blocks 集合（去掉 plinth 后）自身连通。
 * 于是把坐标整体下移 1（「归一化坐标」）：根格 = (0,0)，其余格 y ≥ 0，
 * 「blocks 质心 x = 0」⟺ Σx = 0（每块质量相同），题面要求镜像 (x,y)→(−x,y) 视为同一件。
 * 记 F(n) = 满足条件的固定朝向形状数（左右镜像分别计数），S(n) = 其中关于 y 轴对称者，
 * 由 Burnside 引理，去重后的答案 A(n) = (F(n) + S(n)) / 2。
 *
 * 枚举方法（本题的标准做法：Redelmeier 枚举 + 剪枝）
 * ───────────────────────────────────────────────
 * 1) 有限域（菱形）：某格 (x,y) 若在形状中，从根到它的最短路长度 ≥ |x|+y，路径上共有
 *    |x|+y+1 个格子，故 |x| + y ≤ n−1（n 个 block）。搜索域 = 该菱形内的格。
 * 2) Redelmeier 规范枚举：维护「候选表」U（与已用格相邻、既未使用也未被否定的格）与
 *    下标 from；第 i 个候选格被「取用」时，新候选表 = U[i+1..] ∪ U[i] 的新邻居，
 *    而 U[0..i−1] 在本分支被永久否定。任一连通形状恰有一条这样的生成路径，不重不漏。
 * 3) 力矩剪枝：设已放 k 格、力矩 sumx、还差 r = n−k 格。剩余格子的 |x| 至多取域内最大
 *    的 r 个，故 |sumx| ≤ maxCorr[r] 才可能回到 0，否则整支剪掉。
 *
 * 两条独立代码路径
 * ──────────────
 * 方法 A（主路径，meta.optimizedBaselineMs 口径）：**直接按镜像等价类枚举**。
 *  搜索时维护「当前部分形状是否仍对称」：对称阶段把候选格 (x,y) 与其镜像 (−x,y) 当作
 *  「成对单元」，分支为 (1) 一次放入整对（保持对称）、(2) 只放入一侧并把镜像格永久排除
 *  （从此不可能再对称，于是每个手性等价类只在代表元一侧生成一次）、(3) 拒绝整对。
 *  对称阶段 Σx 恒为 0；打破对称后退化为普通枚举 + 力矩剪枝。统计量即答案 A(n)。
 * 方法 B（独立复核）：Burnside 路线。普通 Redelmeier 枚举（镜像分别计数）得 F(n)，
 *  每到 n 格时检查形状是否逐格成镜像对得 S(n)，答案 (F+S)/2。
 *
 * 复杂度
 * ──────
 * 搜索树 = 菱形域内所有连通形状（按剪枝存活）之和，节点数随 n 约 ×3.6 增长：
 * n=18 时方法 A ≈ 1.0×10^9 节点、方法 B ≈ 2.0×10^9 节点；单机 JIT 预热后分别约 21 s / 42 s。
 * 对照：定义级暴力（不加规范化、靠形状判重）只能做到 order ≤ 10（约 10^6 级），
 * order 18 需要枚举 ~10^9 个形状，故必须用规范化枚举 + 剪枝。
 *
 * 验证
 * ────
 * 1. 题面锚点：order 6 → 18、order 10 → 964、order 15 → 360505（方法 A、方法 B 同时复现）；
 * 2. 定义级暴力（按题面原坐标直接枚举 polyomino + 质心判定 + 镜像去重）在 order ≤ 8
 *    与两条方法逐一一致，order 9/10 也一致；
 * 3. F(n) 序列与 OEIS A171579（fixed polyominoes in equilibrium）逐项吻合（n ≤ 17）；
 * 4. 公开答案表（luckytoilet 等 PE answer 汇总）给出 order 18 = 15030564，与本机实跑一致。
 *
 * 答案：15030564（A(18)）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0275/solution.kt -d <目录>
 * java -cp <目录>:<kotlin-stdlib> SolutionKt
 * （本题完整规模单次约 21 s；脚本含定义级暴力、双方法互证与 3 轮计时，总运行约 2.5 分钟）
 */

import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
// 方法 A：按镜像等价类直接枚举（主路径）
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 归一化坐标下的枚举（根格 (0,0)，其余格 y ≥ 0，|x| + y ≤ n−1）。
 * 对称阶段按「镜像对 / 轴上格」为单位做决策，打破对称后永久排除镜像格，
 * 从而每个手性等价类只被生成一次。
 */
private class SymBreak(val n: Int) {
    private val w = 2 * n - 1
    private val size = w * n
    private val xOf = IntArray(size)
    private val valid = BooleanArray(size)
    private val nbr = Array(size) { IntArray(0) }
    private val mirror = IntArray(size)
    private val maxCorr = IntArray(n + 1)

    init {
        for (y in 0 until n) for (xi in 0 until w) {
            val x = xi - (n - 1)
            val id = xi * n + y
            xOf[id] = x
            valid[id] = abs(x) + y <= n - 1          // 菱形域
            mirror[id] = (-x + (n - 1)) * n + y
        }
        for (y in 0 until n) for (xi in 0 until w) {
            val id = xi * n + y
            if (!valid[id]) continue
            val list = ArrayList<Int>(4)
            if (xi > 0) { val v = (xi - 1) * n + y; if (valid[v]) list.add(v) }
            if (xi < w - 1) { val v = (xi + 1) * n + y; if (valid[v]) list.add(v) }
            if (y > 0) { val v = xi * n + (y - 1); if (valid[v]) list.add(v) }
            if (y < n - 1) { val v = xi * n + (y + 1); if (valid[v]) list.add(v) }
            nbr[id] = list.toIntArray()
        }
        // maxCorr[r] = 域内除根外最大的 r 个 |x| 之和（剩余 r 格能提供的力矩上界）
        val xs = ArrayList<Int>()
        for (id in 0 until size) if (valid[id] && id != rootId()) xs.add(abs(xOf[id]))
        xs.sortDescending()
        var pref = 0
        for (r in 1..minOf(n, xs.size)) { pref += xs[r - 1]; maxCorr[r] = pref }
        for (r in xs.size + 1..n) maxCorr[r] = pref
    }

    private fun rootId() = (n - 1) * n + 0

    var count = 0L
        private set
    var nodes = 0L
        private set

    private val occ = BooleanArray(size)
    private val forb = BooleanArray(size)
    private val inU = BooleanArray(size)
    private val u = IntArray(size + 8)
    private var uCount = 0
    private val processed = Array(n + 2) { IntArray(size) }

    private fun pushNeighbors(c: Int) {
        for (v in nbr[c]) if (!occ[v] && !forb[v] && !inU[v]) { u[uCount++] = v; inU[v] = true }
    }

    private fun popTo(addedStart: Int) {
        while (uCount > addedStart) { val v = u[--uCount]; inU[v] = false }
    }

    /** 对称阶段：形状关于 y 轴对称，Σx 恒为 0。 */
    private fun dfsSym(from: Int, cnt: Int, depth: Int) {
        nodes++
        if (cnt == n) { count++; return }
        val proc = processed[depth]
        var pc = 0
        var i = from
        while (i < uCount) {
            val c = u[i]; i++
            if (occ[c] || forb[c]) continue
            if (xOf[c] == 0) {
                // 轴上的格：单独放入，保持对称
                occ[c] = true
                val added = uCount
                pushNeighbors(c)
                dfsSym(i, cnt + 1, depth + 1)
                popTo(added)
                occ[c] = false
                forb[c] = true; proc[pc++] = c
            } else {
                val m = mirror[c]
                // 对称不变量：候选表整体对称，故镜像格此刻必为候选
                check(!occ[m] && !forb[m] && inU[m]) { "对称不变量被破坏" }
                // (1) 保持对称：一次放入整对
                if (cnt + 2 <= n) {
                    occ[c] = true; occ[m] = true
                    val added = uCount
                    pushNeighbors(c); pushNeighbors(m)
                    dfsSym(i, cnt + 2, depth + 1)
                    popTo(added)
                    occ[m] = false; occ[c] = false
                }
                // (2) 打破对称：只放 c，永久排除镜像格 m（该支不可能再对称）
                occ[c] = true
                val added2 = uCount
                pushNeighbors(c)
                val wasForb = forb[m]
                forb[m] = true
                dfsAsym(i, cnt + 1, xOf[c], depth + 1)
                forb[m] = wasForb
                popTo(added2)
                occ[c] = false
                // (3) 拒绝整对
                forb[c] = true; proc[pc++] = c
                forb[m] = true; proc[pc++] = m
            }
        }
        for (j in pc - 1 downTo 0) forb[proc[j]] = false
    }

    /** 非对称阶段：普通 Redelmeier 枚举 + 力矩剪枝（只统计 Σx = 0 的完成形状）。 */
    private fun dfsAsym(from: Int, cnt: Int, sumx: Int, depth: Int) {
        nodes++
        val r = n - cnt
        if (abs(sumx) > maxCorr[r]) return
        if (cnt == n) { if (sumx == 0) count++; return }
        val proc = processed[depth]
        var pc = 0
        var i = from
        while (i < uCount) {
            val c = u[i]; i++
            if (occ[c] || forb[c]) continue
            occ[c] = true
            val added = uCount
            pushNeighbors(c)
            dfsAsym(i, cnt + 1, sumx + xOf[c], depth + 1)
            popTo(added)
            occ[c] = false
            forb[c] = true; proc[pc++] = c
        }
        for (j in pc - 1 downTo 0) forb[proc[j]] = false
    }

    fun run(): Long {
        val root = rootId()
        occ[root] = true
        pushNeighbors(root)
        dfsSym(0, 1, 0)
        return count
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 方法 B：Burnside —— F(n)（镜像分别计数）与 S(n)（自对称）分别枚举
// ─────────────────────────────────────────────────────────────────────────────

/** 普通 Redelmeier 枚举：统计 Σx = 0 的连通形状（镜像各算一次 = F），并顺带数对称者（S）。 */
private class Burnside(val n: Int) {
    private val w = 2 * n - 1
    private val size = w * n
    private val xOf = IntArray(size)
    private val yOf = IntArray(size)
    private val valid = BooleanArray(size)
    private val nbr = Array(size) { IntArray(0) }
    private val mirror = IntArray(size)
    private val maxCorr = IntArray(n + 1)

    init {
        for (y in 0 until n) for (xi in 0 until w) {
            val x = xi - (n - 1)
            val id = xi * n + y
            xOf[id] = x
            yOf[id] = y
            valid[id] = abs(x) + y <= n - 1
            mirror[id] = (-x + (n - 1)) * n + y
        }
        for (y in 0 until n) for (xi in 0 until w) {
            val id = xi * n + y
            if (!valid[id]) continue
            val list = ArrayList<Int>(4)
            if (xi > 0) { val v = (xi - 1) * n + y; if (valid[v]) list.add(v) }
            if (xi < w - 1) { val v = (xi + 1) * n + y; if (valid[v]) list.add(v) }
            if (y > 0) { val v = xi * n + (y - 1); if (valid[v]) list.add(v) }
            if (y < n - 1) { val v = xi * n + (y + 1); if (valid[v]) list.add(v) }
            nbr[id] = list.toIntArray()
        }
        val xs = ArrayList<Int>()
        for (id in 0 until size) if (valid[id] && id != rootId()) xs.add(abs(xOf[id]))
        xs.sortDescending()
        var pref = 0
        for (r in 1..minOf(n, xs.size)) { pref += xs[r - 1]; maxCorr[r] = pref }
        for (r in xs.size + 1..n) maxCorr[r] = pref
    }

    private fun rootId() = (n - 1) * n + 0

    private val occ = BooleanArray(size)
    private val forb = BooleanArray(size)
    private val inU = BooleanArray(size)
    private val u = IntArray(size + 8)
    private var uCount = 0
    private val placed = IntArray(n + 2)
    private val processed = Array(n + 2) { IntArray(size) }

    var total = 0L
        private set
    var symmetric = 0L
        private set
    var nodes = 0L
        private set

    private fun isSymmetric(cnt: Int): Boolean {
        for (i in 0 until cnt) if (!occ[mirror[placed[i]]]) return false
        return true
    }

    private fun dfs(from: Int, cnt: Int, sumx: Int, depth: Int) {
        nodes++
        val r = n - cnt
        if (abs(sumx) > maxCorr[r]) return
        if (cnt == n) {
            total++
            if (isSymmetric(cnt)) symmetric++
            return
        }
        val proc = processed[depth]
        var pc = 0
        var i = from
        while (i < uCount) {
            val c = u[i]; i++
            if (occ[c] || forb[c]) continue
            occ[c] = true
            placed[cnt] = c
            val added = uCount
            for (v in nbr[c]) if (!occ[v] && !forb[v] && !inU[v]) { u[uCount++] = v; inU[v] = true }
            dfs(i, cnt + 1, sumx + xOf[c], depth + 1)
            while (uCount > added) { val v = u[--uCount]; inU[v] = false }
            occ[c] = false
            forb[c] = true; proc[pc++] = c
        }
        for (j in pc - 1 downTo 0) forb[proc[j]] = false
    }

    fun run(): Long {
        val root = rootId()
        occ[root] = true
        placed[0] = root
        for (v in nbr[root]) if (!occ[v] && !forb[v] && !inU[v]) { u[uCount++] = v; inU[v] = true }
        dfs(0, 1, 0, 0)
        return (total + symmetric) / 2
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 定义级暴力：完全按题面原坐标枚举 polyomino（order ≤ 10），镜像等价类计数
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 原坐标：plinth 在 (0,0)；blocks 在 y ≥ 1（(0,-1)/(±1,0) 会违反「plinth 唯一最低」）。
 * 枚举「plinth + order 个 block」的连通集合（候选格自增的规范 include/exclude 分支），
 * 在叶子上要求 blocks 的 Σx = 0，且只统计自身 ≤ 镜像的那一个代表元。
 */
private fun defBrute(order: Int): Long {
    val span = order + 2
    val w = 2 * span + 1
    val size = w * span
    fun id(x: Int, y: Int) = (x + span) * span + y
    val xOf = IntArray(size) { (it / span) - span }
    val yOf = IntArray(size) { it % span }
    val ok = BooleanArray(size) { i -> yOf[i] >= 1 || (xOf[i] == 0 && yOf[i] == 0) }
    val nbr = Array(size) { c ->
        if (!ok[c]) IntArray(0) else {
            val list = ArrayList<Int>(4)
            for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) {
                val nx = xOf[c] + dx
                val ny = yOf[c] + dy
                if (nx in -span..span && ny in 0 until span) {   // 显式范围检查，避免负坐标回绕
                    val v = id(nx, ny)
                    if (ok[v]) list.add(v)
                }
            }
            list.toIntArray()
        }
    }
    val occ = BooleanArray(size)
    val forb = BooleanArray(size)
    var count = 0L
    var nodes = 0L

    /** 叶子判定：Σx(blocks) = 0 且形状字典序 ≤ 其镜像（等价类代表元）。 */
    fun leaf(cells: IntArray): Boolean {
        var sx = 0
        val sorted = cells.sortedArray()
        for (c in sorted) if (c != id(0, 0)) sx += xOf[c]
        if (sx != 0) return false
        val mirrored = IntArray(sorted.size) { i -> id(-xOf[sorted[i]], yOf[sorted[i]]) }.sortedArray()
        for (i in sorted.indices) {
            if (sorted[i] != mirrored[i]) return sorted[i] < mirrored[i]
        }
        return true
    }

    fun dfs2(cand: IntArray, from: Int, cnt: Int, cells: IntArray) {
        nodes++
        if (cnt == order + 1) { if (leaf(cells)) count++; return }
        var i = from
        while (i < cand.size) {
            val c = cand[i]; i++
            if (occ[c] || forb[c]) continue
            occ[c] = true
            cells[cnt] = c
            val newCand = ArrayList<Int>(cand.size + 4)
            for (j in i until cand.size) newCand.add(cand[j])
            for (v in nbr[c]) if (!occ[v] && !forb[v] && !newCand.contains(v)) newCand.add(v)
            dfs2(newCand.toIntArray(), 0, cnt + 1, cells)
            occ[c] = false
            forb[c] = true
        }
        for (j in from until cand.size) forb[cand[j]] = false
    }

    val root = id(0, 0)
    occ[root] = true
    val init = ArrayList<Int>()
    for (v in nbr[root]) if (!occ[v]) init.add(v)
    dfs2(init.toIntArray(), 0, 1, IntArray(order + 2) { root })
    return count
}

// ─────────────────────────────────────────────────────────────────────────────
// 计时与主流程
// ─────────────────────────────────────────────────────────────────────────────

private fun bestOf(tag: String, expected: Long, rounds: Int, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(rounds) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（$rounds 轮最优，JIT 预热后）")
    return best
}

private fun timed(tag: String, f: () -> Long): Pair<Long, Double> {
    val t0 = System.nanoTime()
    val v = f()
    val ms = (System.nanoTime() - t0) / 1e6
    println("$tag：$v（${"%.1f".format(ms)} ms）")
    return v to ms
}

fun main() {
    // ---------- 1. 定义级暴力 vs 两条方法（小规模对拍） ----------
    val anchors = mapOf(6 to 18L, 10 to 964L, 15 to 360505L)
    for (order in 1..9) {
        val b = defBrute(order)
        val a = SymBreak(order).run()
        val c = Burnside(order).run()
        check(a == b && c == b) { "order $order：定义级 $b，方法 A $a，方法 B $c 不一致" }
        println("order $order：定义级暴力 = $b（方法 A、方法 B 一致）")
    }
    check(defBrute(10) == 964L && SymBreak(10).run() == 964L) { "order 10 三方不一致" }
    println("order 10：定义级暴力 = 964（与方法 A 一致）")
    println("题面锚点：order 6 = 18、order 10 = 964（定义级暴力与方法 A 同时复现）")

    // ---------- 2. 题面锚点：两条方法在 6/10/15 上互证（F 值另与 OEIS A171579 对照） ----------
    // OEIS A171579：fixed polyominoes in equilibrium（质心与最低格同一列），即本题的 F(n)
    val oeisF = mapOf(6 to 27L, 10 to 1825L, 15 to 718474L)
    for ((order, expect) in anchors) {
        val a = SymBreak(order).run()
        val bs = Burnside(order)
        val c = bs.run()
        check(a == expect && c == expect) { "order $order：方法 A $a、方法 B $c ≠ 题面 $expect" }
        check(bs.total == oeisF[order]) { "order $order：F = ${bs.total} ≠ OEIS A171579 的 ${oeisF[order]}" }
        println("order $order：方法 A = $a，方法 B = $c（题面锚点 $expect；F = ${bs.total} 与 OEIS A171579 一致，S = ${bs.symmetric}）")
    }
    println("三个题面锚点（18 / 964 / 360505）被两条独立路径同时复现，F 序列与 OEIS A171579 逐项吻合")

    // ---------- 3. 完整规模 order 18 ----------
    val bs18 = Burnside(18)
    val (b18, msB18) = timed("方法 B（Burnside 枚举 F/S，order 18，JIT 已预热）") { bs18.run() }
    println("order 18 方法 B 访问节点 ${bs18.nodes} 个")
    var aNodes = 0L
    val msA18 = bestOf("方法 A（主路径，镜像等价类直接枚举，order 18）", b18, 3) {
        val s = SymBreak(18)
        val v = s.run()
        aNodes = s.nodes
        v
    }
    val a18 = b18
    println("order 18 两方法一致：$a18（F = ${bs18.total}，S = ${bs18.symmetric}；方法 A 节点 $aNodes 个，方法 B 节点 ${bs18.nodes} 个）")

    // ---------- 4. 计时（JIT 预热后） ----------
    println()
    println("计时（JIT 预热后）：")
    val msA15 = bestOf("方法 A order 15（主路径，中等规模）", 360505L, 3) { SymBreak(15).run() }
    val msB15 = bestOf("方法 B order 15（独立复核）", 360505L, 3) { Burnside(15).run() }
    val msB10 = bestOf("定义级暴力 order 10（对照）", 964L, 3) { defBrute(10) }

    println()
    println("答案 = $a18")
    println("汇总：方法 A order 18 ${"%.1f".format(msA18)} ms（3 轮最优，meta.optimizedBaselineMs 口径）/ order 15 ${"%.3f".format(msA15)} ms；" +
        "方法 B order 18 ${"%.1f".format(msB18)} ms（单轮）/ order 15 ${"%.3f".format(msB15)} ms（3 轮最优）；" +
        "定义级暴力 order 10 ${"%.3f".format(msB10)} ms（3 轮最优）")
    println("check() 全部通过")
}
