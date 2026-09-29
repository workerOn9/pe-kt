#!/usr/bin/env kotlin
/**
 * Project Euler 275 — Balanced Sculptures：暴力对照（独立实现，不与 solution.kt 共享核心代码）
 *
 * 两条路径：
 *
 *   路径 1（按题面定义，order ≤ 10）：在**原坐标**下直接枚举 polyomino：plinth 固定在 (0,0)，
 *   blocks 满足 y ≥ 1（否则 plinth 不是唯一最低格；相邻格 (±1,0) 因此也不可能是 block），
 *   整体 4 邻接连通。做法是「按层 BFS + 形状集合判重」：第 k 层保存所有含 plinth 的 k 格连通
 *   集合（用排序后的坐标串作 key 去重），到 order+1 格时筛「blocks 的 Σx = 0」，
 *   最后把形状与自己的镜像取字典序较小者作为等价类代表。复现题面锚点 18 / 964。
 *   复杂度：第 k 层集合数 = 含固定格的 k 格多联骨牌数（1, 3, 10, ... 到 order 10 约 3.5×10^5，
 *   order 12 约 3.5×10^6），全靠哈希表存形状，order ≥ 12 后内存与时间迅速失控。
 *
 *   路径 2（无剪枝的规范枚举，order ≤ 15）：把坐标下移一格（根格 = (0,0)，其余 y ≥ 0，
 *   质心条件 = Σx = 0），用 Redelmeier 规范枚举（候选表 + 单调选择）逐个生成连通形状，
 *   **不加任何力矩剪枝**，在 n 格时数 Σx = 0 的形状（左右镜像分别计数得 F），
 *   再单独统计自对称者 S（每个格子与其镜像同时在形状中），答案 = (F + S) / 2。
 *   复现 18 / 964 / 360505；order 15 的搜索树 7.1×10^7 节点、约 1.35 秒。
 *
 * 外推（为什么完整规模必须做剪枝）：不剪枝时搜索树 ≈ 菱形域内所有 ≤ order 格的连通形状之和，
 *   order 15 → 7.1×10^7 节点、order 17 → 9.1×10^8、order 18 → 3.3×10^9 节点（每级约 ×3.6），
 *   单机 JIT 后需 1–2 分钟；solution.kt 加入力矩剪枝 |Σx| ≤ maxCorr[剩余格数] 后 order 18 降到
 *   2.0×10^9 节点，再按镜像等价类枚举（方法 A）降到 1.0×10^9 节点、约 21 秒。
 *   本文件两条路径用于把 18 / 964 / 360505 三个锚点钉死在「按定义枚举」这一层。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：bash scripts/kotlinc-shim.sh content/problems/0275/brute-force.kt -d <目录>
 *       java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *      （Kotlin 会把文件名里的 `-` mangle 成 `_`，故入口类名是 Brute_forceKt）
 */

private fun bestOf(tag: String, expected: Long, rounds: Int, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(rounds) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（$rounds 轮最优，JIT 预热后）")
    return best
}

// ─────────────────────────────────────────────────────────────────────────────
// 路径 1：按题面定义（原坐标）按层枚举 + 形状集合判重 + 镜像等价类计数
// ─────────────────────────────────────────────────────────────────────────────

/** 原坐标格子 (x,y)：plinth (0,0) 与 blocks y ≥ 1。 */
private const val OFF = 16          // 坐标编码：c = (x + OFF) * 32 + y，x ∈ [-16,16]
private fun enc(x: Int, y: Int) = (x + OFF) * 32 + y
private fun decX(c: Int) = c / 32 - OFF
private fun decY(c: Int) = c % 32

private val DIRS = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

private fun definitionalCount(order: Int): Long {
    fun keyOf(cells: IntArray) = cells.sorted().joinToString(",")
    val plinth = enc(0, 0)
    // 第 k 层：所有「含 plinth 的 k 格连通集合」（排序坐标串判重）
    var level: Set<String> = setOf(keyOf(intArrayOf(plinth)))
    for (cnt in 2..order + 1) {
        val next = HashSet<String>(1 shl 16)
        for (key in level) {
            val cells = key.split(",").map { it.toInt() }.toIntArray()
            for (c in cells) {
                val x = decX(c)
                val y = decY(c)
                for ((dx, dy) in DIRS) {
                    val nx = x + dx
                    val ny = y + dy
                    if (ny < 1) continue                       // blocks 必须 y ≥ 1（plinth 已在集合里）
                    val nc = enc(nx, ny)
                    if (cells.contains(nc)) continue
                    next.add(keyOf(cells + nc))
                }
            }
        }
        level = next
    }
    var count = 0L
    for (key in level) {
        val cells = key.split(",").map { it.toInt() }.toIntArray()   // 已排序
        var sx = 0
        for (c in cells) if (c != plinth) sx += decX(c)
        if (sx != 0) continue
        val mirrored = cells.map { enc(-decX(it), decY(it)) }.sorted()
        // 只保留字典序不大于镜像的形状 ⇒ 每个镜像等价类恰计一次
        var cmp = 0
        for (i in cells.indices) {
            if (cells[i] != mirrored[i]) { cmp = cells[i].compareTo(mirrored[i]); break }
        }
        if (cmp <= 0) count++
    }
    return count
}

// ─────────────────────────────────────────────────────────────────────────────
// 路径 2：无剪枝的规范枚举（归一化坐标，根格 (0,0)，其余 y ≥ 0）
// ─────────────────────────────────────────────────────────────────────────────

private class PlainEnum(val n: Int) {
    private val w = 2 * n - 1
    private val size = w * n
    private val xOf = IntArray(size) { (it / n) - (n - 1) }
    private val valid = BooleanArray(size) { i -> kotlin.math.abs(xOf[i]) + (i % n) <= n - 1 }
    private val mirror = IntArray(size) { i -> (xOf[i].let { -(it) + (n - 1) }) * n + (i % n) }
    private val nbr = Array(size) { i ->
        if (!valid[i]) IntArray(0) else {
            val y = i % n
            val xi = i / n
            val list = ArrayList<Int>(4)
            if (xi > 0) { val v = (xi - 1) * n + y; if (valid[v]) list.add(v) }
            if (xi < w - 1) { val v = (xi + 1) * n + y; if (valid[v]) list.add(v) }
            if (y > 0) { val v = xi * n + (y - 1); if (valid[v]) list.add(v) }
            if (y < n - 1) { val v = xi * n + (y + 1); if (valid[v]) list.add(v) }
            list.toIntArray()
        }
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
        if (cnt == n) {                       // 无剪枝：任何形状都走到底再判平衡
            if (sumx == 0) {
                total++
                if (isSymmetric(cnt)) symmetric++
            }
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

private fun main2() {
    // ---------- 路径 1：按题面定义（order ≤ 10） ----------
    val expect1 = mapOf(1 to 1L, 2 to 1L, 3 to 2L, 4 to 4L, 5 to 9L, 6 to 18L, 7 to 44L, 8 to 111L, 9 to 324L, 10 to 964L)
    for ((order, expect) in expect1) {
        val v = definitionalCount(order)
        check(v == expect) { "order $order：按定义枚举 $v ≠ 期望 $expect" }
        println("路径 1（按题面定义，原坐标集合枚举）order $order：$v")
    }
    check(definitionalCount(6) == 18L && definitionalCount(10) == 964L) { "题面锚点未复现" }
    println("路径 1 复现题面锚点 order 6 = 18、order 10 = 964")

    // ---------- 路径 2：无剪枝规范枚举（order ≤ 15） ----------
    val expect2 = mapOf(6 to 18L, 10 to 964L, 15 to 360505L)
    for ((order, expect) in expect2) {
        val v = PlainEnum(order).run()
        check(v == expect) { "order $order：无剪枝枚举 $v ≠ 期望 $expect" }
        println("路径 2（无剪枝规范枚举）order $order：$v（题面锚点）")
    }
    for (order in 11..14) {
        println("路径 2 order $order：${PlainEnum(order).run()}")
    }

    // ---------- 计时 ----------
    definitionalCount(9)
    PlainEnum(14).run()
    val msDef = bestOf("路径 1：按题面定义集合枚举（order 10）", 964L, 3) { definitionalCount(10) }
    PlainEnum(15).run()
    val msPlain = bestOf("路径 2：无剪枝规范枚举（order 15）", 360505L, 3) { PlainEnum(15).run() }
    val nodes15 = PlainEnum(15).also { it.run() }.nodes

    println()
    println("汇总：路径 1（按题面定义集合枚举）order 10 ${"%.1f".format(msDef)} ms（每层形状数 ~3.5×10^5）；" +
        "路径 2（无剪枝规范枚举）order 15 ${"%.1f".format(msPlain)} ms（节点数 $nodes15，meta.bruteForceBaselineMs 口径：暴力能达到的最大规模）")
    println("外推：无剪枝搜索树节点数每级约 ×3.6（order 15 → 7.1×10^7），order 18 约 3.3×10^9 节点、" +
        "单机需 1–2 分钟；力矩剪枝 + 镜像等价类枚举可把它降到 1.0×10^9 节点、约 21 秒（见 solution.kt）")
    println("check() 全部通过")
}

fun main() = main2()
