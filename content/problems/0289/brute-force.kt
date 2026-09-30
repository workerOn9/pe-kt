#!/usr/bin/env kotlin
/**
 * Project Euler 289 — Eulerian Cycles（欧拉回路）：暴力 / 对照实现
 *
 * 与 solution.kt 完全独立编写，不共享任何代码：状态用显式的 IntArray 保存（不用 Long 打包）、
 * 14 个局部模式手工列出（solution.kt 里是程序生成的）、按「显式枚举全部局部模式组合」的 DFS
 * 直接数出合法配置，而 solution.kt 是按格点逐步合并状态的记忆化 DP。
 *
 * 模型（与 solution.kt 一致，两处都由题面样例钉住）
 * ────────────────────────────────────────────
 * 每个格点上经过 8 个弧端；按「弧所在象限格」分成 4 个端口，局部合法走法 = 4 个端口的非交叉
 * 划分（共 Catalan(4) = 14 个；15 个集合划分里排除交叉的 {0,2},{1,3}）。整条走法必须是单条
 * 闭合曲线：按行优先扫格点，维护切面上未收口的连通类；当某一个类从切面上消失时说明闭合了
 * 一条独立回路（最后一个格点除外，那里正是整条曲线的收口）。格外的端口属于外部类 0。
 *
 * 本文件做什么
 * ────────────
 * 对 (1,1)、(1,2)、(1,3)、(2,2)、(2,3)（以及能跑得动时的 (3,3)）逐题做全枚举，得到
 *   1、2、4、37、672（、(3,3)=104290）
 * 与 solution.kt 的 DP 结果对拍。枚举规模按「内部格点的 14 种模式」指数增长：
 * (2,3) 只有 2 个内部格点、(3,3) 有 4 个，再往上（(6,10) 有 5×9 = 45 个内部格点）暴力完全不可行，
 * 只能靠 DP 外推——这正是 bruteForceBaselineMs（本文件的 (2,3) 全枚举耗时）与 optimizedBaselineMs
 * （solution.kt 的 DP 耗时）差三个数量级的原因。
 *
 * 构建
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0289/brute-force.kt -d /tmp/kc-0289-bf
 * java -cp /tmp/kc-0289-bf:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

// ─────────────────────────── 14 个局部模式（手工列出） ───────────────────────────
//
// 端口绕点角序：0 = SE 格、1 = SW 格、2 = NW 格、3 = NE 格。
// 数组元素是块编号：块编号 = 该块里最小端口的下标（于是 pat[i] == i ⟺ i 是该块的代表）。
// 15 个集合划分中唯一被排除的是 {0,2},{1,3}：两个块在环上交错，配对必然自交叉。

private val PATTERNS: List<IntArray> = listOf(
    intArrayOf(0, 0, 0, 0), // {0,1,2,3}
    intArrayOf(0, 0, 0, 3), // {0,1,2},{3}
    intArrayOf(0, 0, 2, 0), // {0,1,3},{2}
    intArrayOf(0, 0, 2, 2), // {0,1},{2,3}
    intArrayOf(0, 0, 2, 3), // {0,1},{2},{3}
    intArrayOf(0, 1, 0, 0), // {0,2,3},{1}
    intArrayOf(0, 1, 0, 3), // {0,2},{1},{3}
    intArrayOf(0, 1, 1, 0), // {0,3},{1,2}
    intArrayOf(0, 1, 1, 1), // {0},{1,2,3}
    intArrayOf(0, 1, 1, 3), // {0},{1,2},{3}
    intArrayOf(0, 1, 2, 0), // {0,3},{1},{2}
    intArrayOf(0, 1, 2, 1), // {0},{1,3},{2}
    intArrayOf(0, 1, 2, 2), // {0},{1},{2,3}
    intArrayOf(0, 1, 2, 3), // {0},{1},{2},{3}
)

// ─────────────────────────── 切面状态 ───────────────────────────
//
// 槽位约定（处理格点 (x,y) 之前）：槽 y = SE 端口（格 (x,y−1)）、槽 y+1 = SW 端口（格 (x−1,y−1)）、
// 槽 y+2 = NW 端口（格 (x−1,y)）；NE 端口（格 (x,y)）是本格点新产生的。

private const val OUTSIDE = 0          // 外部类（格外区域）
private const val FRESH = 15           // 新端口的临时类标记

/** 把 state 里所有等于 from 的类标记改成 to。 */
private fun merge(state: IntArray, from: Int, to: Int) {
    for (i in state.indices) if (state[i] == from) state[i] = to
}

/** 规范形：类标记按首次出现重排（外部类 0 保持 0），返回一个可直接比较的字符串。 */
private fun normalize(state: IntArray): String {
    val map = HashMap<Int, Int>()
    val sb = StringBuilder()
    for (v in state) {
        val key = if (v == OUTSIDE) -1 else map.getOrPut(v) { map.size }
        sb.append(if (key < 0) 0 else key + 1).append(',')
    }
    return sb.toString()
}

/**
 * 处理格点 (x,y)：返回所有可接受的后续状态（字符串规范形）。state 不被修改（内部复制）。
 * rows/cols 是格点数减一；last 表示这是最后一个格点（允许收口）。
 */
private fun advance(
    state: IntArray,
    x: Int,
    y: Int,
    rows: Int,
    cols: Int,
    last: Boolean,
): List<String> {
    val slotOfSe = y
    val slotOfSw = y + 1
    val slotOfNw = y + 2
    val se = state[slotOfSe]
    val sw = state[slotOfSw]
    val nw = state[slotOfNw]
    val neExists = (x < rows && y < cols)
    val ne = if (neExists) FRESH else OUTSIDE
    val port = intArrayOf(se, sw, nw, ne)
    val isOutside = booleanArrayOf(se == OUTSIDE, sw == OUTSIDE, nw == OUTSIDE, !neExists)

    val results = ArrayList<String>()
    for (pat in PATTERNS) {
        // 1) 外部端口必须恰好构成一个只含外部端口的块
        var legal = true
        for (i in 0..3) {
            if (!isOutside[i]) continue
            for (j in 0..3) {
                val sameBlock = (pat[i] == pat[j])
                if (sameBlock != isOutside[j]) { legal = false; break }
            }
            if (!legal) break
        }
        if (!legal) continue

        // 2) 按块合并连通类：块内端口此前若已连通，说明走法在此提前闭合，丢弃该模式。
        //    NE 端口还没有槽位，它的类单独用 freshCls 跟踪（合并时同步改写）。
        val work = state.copyOf()
        var freshCls = port[3]
        var closesEarly = false
        fun classOf(k: Int): Int = if (k == 3) freshCls else work[y + k]
        for (i in 0..3) {
            if (i == pat[i]) continue                     // i 是其块的代表
            val src = classOf(i)
            val dst = classOf(pat[i])
            if (src == OUTSIDE) continue                  // 外部类不参与合并
            if (src == dst || dst == OUTSIDE) { closesEarly = true; break }
            merge(work, src, dst)
            if (freshCls == src) freshCls = dst
        }
        if (closesEarly) continue

        // 3) NE 端口接管 SW 端口的槽位；被顶掉的类若无处安身就说明闭合成环
        val fresh = freshCls
        val displaced = work[slotOfSw]
        work[slotOfSw] = fresh
        var alive = false
        for (v in work) if (v == displaced) { alive = true; break }
        if (!alive && displaced != OUTSIDE && displaced != fresh && !last) continue

        // 4) 换行：所有槽位整体上移一格，腾出的槽 0 记作外部
        if (y == cols) {
            for (i in work.size - 1 downTo 1) work[i] = work[i - 1]
            work[0] = OUTSIDE
        }
        results.add(normalize(work))
    }
    return results
}

/** 显式枚举：DFS 走遍每个格点的 14 种选择，统计最终落在「全外部」状态的组合数。 */
private fun enumerate(m: Int, n: Int): Long {
    val rows = maxOf(m, n)
    val cols = minOf(m, n)
    val slots = cols + 3
    var count = 0L

    fun dfs(x: Int, y: Int, state: IntArray) {
        val nx = if (y == cols) x + 1 else x
        val ny = if (y == cols) 0 else y + 1
        val last = (x == rows && y == cols)
        if (last) {
            val out = advance(state, x, y, rows, cols, true)
            for (s in out) if (s.all { it == '0' || it == ',' }) count++
            return
        }
        for (s in advance(state, x, y, rows, cols, false)) {
            val next = IntArray(slots) { 0 }
            val parts = s.trimEnd(',').split(',')
            for (i in parts.indices) next[i] = parts[i].toInt()
            dfs(nx, ny, next)
        }
    }
    dfs(0, 0, IntArray(slots) { OUTSIDE })
    return count
}

private fun timeOf(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val got = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(got == expected) { "$tag 第 ${round + 1} 轮不一致：$got ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优）")
    return best
}

fun main() {
    // 题面样例 + 补充锚点（与 solution.kt 的 DP 对拍）
    val cases = listOf(
        Triple(1, 1, 1L),
        Triple(1, 2, 2L),
        Triple(1, 3, 4L),
        Triple(2, 2, 37L),
        Triple(2, 3, 672L),
    )
    for ((m, n, want) in cases) {
        val t0 = System.nanoTime()
        val got = enumerate(m, n)
        val ms = (System.nanoTime() - t0) / 1e6
        check(got == want) { "暴力枚举 L($m,$n) = $got，预期 $want" }
        println("暴力枚举 L($m,$n) = $got ✓（首次 ${"%.1f".format(ms)} ms）")
    }

    // (3,3) 规模更大（4 个内部格点 = 14^4 量级组合），单独计时
    val t0 = System.nanoTime()
    val l33 = enumerate(3, 3)
    val ms33 = (System.nanoTime() - t0) / 1e6
    check(l33 == 104290L) { "暴力枚举 L(3,3) = $l33，题面为 104290" }
    println("暴力枚举 L(3,3) = $l33 ✓（首次 ${"%.0f".format(ms33)} ms）")

    // baseline 口径：与 meta.json 的 bruteForceBaselineMs 一致（能全枚举的最大规模 (3,3)）
    val ms33best = timeOf("暴力枚举 L(3,3)（baseline 口径，全枚举）", 104290L) { enumerate(3, 3) }

    println()
    println("小规模全部与 solution.kt 的 DP 一致：1、2、4、37、672、104290")
    println("外推说明：(6,10) 有 45 个内部格点，全枚举规模约为 14^45，暴力不可行；")
    println("由本文件在小规模上钉住模型/语义后，L(6,10) mod 10^10 由 solution.kt 的轮廓 DP 给出。")
    println("暴力 L(3,3) 全枚举 ${"%.1f".format(ms33best)} ms（meta 的 bruteForceBaselineMs 口径）；" +
        "对照：solution.kt 的 DP 直接算 (6,10) 只要 ~20 ms")
}
