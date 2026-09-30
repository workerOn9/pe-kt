#!/usr/bin/env kotlin
/**
 * Project Euler 289 — Eulerian Cycles（欧拉回路）
 *
 * 题目：C(x,y) 是过 (x,y)、(x,y+1)、(x+1,y)、(x+1,y+1) 四点的圆。E(m,n) 由 m·n 个这样的圆组成；
 * 每个圆被它的 4 个角点分成 4 条弧（每条弧连接一对相邻格点），全配置共 4mn 条弧。E(m,n) 上的
 * 欧拉回路 = 把每条弧恰好走一次的不自交叉闭合路径。记 L(m,n) 为这种路径的条数：题面给出
 * L(1,2)=2、L(2,2)=37、L(3,3)=104290，要求 L(6,10) mod 10^10。
 *
 * 思路
 * ────
 * 每个圆的半径是 √2/2，圆心在半整数点上。两个这样的圆，圆心距离只可能是 1（交于两个格点）、
 * √2（相切于一个格点）或 ≥2（不相交），所以两条弧除了可能在格点处相接外永远不会在内部相交。
 * 于是「路径不自交叉」是一个**纯局部**条件：在每个格点上，把经过该点的 8 个弧端按要求配成
 * 4 对，要求配对在绕点角序上非交叉；再加上整条走法是一条闭合曲线，就得到全部合法路径。
 *
 * 局部归约：格点周围最多 4 个格（象限格）。每个象限格的圆在该点给出 2 个弧端，分别朝该象限的
 * 两条对角线方向（正是「该格点与同一个邻居之间」的两条弧）。把 8 个弧端按「所属象限格」归成
 * 4 组（4 个**端口**）后，局部合法走法 = 这 4 个端口的**非交叉划分**：同一块里的端口在遍历中
 * 依次相接，不同块互不相连。
 * 4 个元素在环上的非交叉划分共 Catalan(4) = 14 个（15 个集合划分中恰好排除交叉的 {0,2},{1,3}）。
 * 一个块内的弧端在绕点角序上只能有互不交错的那一种接法，因此局部走法与这 14 个划分一一对应；
 * 这就是「不自交叉」在每个格点上的全部约束。
 *
 * 全局条件：路径必须是一条闭合曲线。轮廓 DP：按行优先扫过全部 (n+1)×(m+1) 个格点，维护切面上
 * 还没收口的连线（每个连通类 = 一条尚未闭合的走法片段）；在当前格点按局部划分把相应端口所属
 * 的类合并。若某个类从切面上消失，说明一段独立的闭合曲线已经成形——除非它是最后一个格点，
 * 否则一律禁止（这正是「单条闭合曲线」的判据）。格子外面的区域记作特殊类 0：所有指向格外的
 * 端口都属于它、且它与真实类永不合并；扫描结束时切面上应当只剩 0。
 *
 * 复杂度
 * ──────
 * 扫描 (m+1)(n+1) 个格点；每个格点对每个状态试 14 个局部模式。切面宽度 = 较小维 + 3 个槽位，
 * 状态数 = 切面上端口的非交叉类结构数（较小维为 6 时最多 429 个状态），L(6,10) 实跑约 20 ms。
 * 对照暴力：显式枚举全部局部模式组合（14^interior 级别），只在小规模可行（见 brute-force.kt）。
 *
 * 验证
 * ────
 * 1. 题面样例：L(1,2)=2、L(2,2)=37、L(3,3)=104290 全部复现；
 * 2. 补充锚点：L(1,1)=1（单圆）、L(1,3)=4、L(1,4)=8（1×n 条带恰为 2^{n-1}）、L(2,3)=672、L(2,4)=12182；
 * 3. 转置互证：把切面宽度取较大的那一维（(6,10) 的转置取向）再算一遍，结果必须一致；
 * 4. brute-force.kt：完全独立写法的显式枚举对照（独立状态表示 + 手工列出的 14 个模式）；
 * 5. 公开答案表（Nayuki 的 Answers.txt 等）列出 L(6,10) mod 10^10 = 6567944538，与本机实跑一致
 *    （仅作旁证，meta.answer 取自本机运行输出）。
 *
 * 答案
 * ────
 * L(6,10) mod 10^10 = 6567944538
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0289/solution.kt -d /tmp/kc-0289
 * java -cp /tmp/kc-0289:<kotlin-stdlib> SolutionKt
 */

import java.util.HashMap

// ─────────────────────────── 局部模式：4 端口的非交叉划分 ───────────────────────────
//
// 端口的绕点角序固定为 0 = SE 象限格 (x,y−1)、1 = SW 象限格 (x−1,y−1)、
// 2 = NW 象限格 (x−1,y)、3 = NE 象限格 (x,y)（顺时针一圈，方向本身无所谓）。
// 每个模式的编码是块编号数组，块编号 = 该块中最小端口的编号（于是 pat[i] == i ⟺ i 是块代表）。

private fun localPatterns(): List<IntArray> {
    val out = ArrayList<IntArray>()
    fun crossing(pat: IntArray): Boolean {
        val blocks = HashMap<Int, ArrayList<Int>>()
        for (i in 0..3) blocks.getOrPut(pat[i]) { ArrayList() }.add(i)
        val list = blocks.values.toList()
        fun inside(u: Int, v: Int, w: Int): Boolean {
            val uv = ((v - u) + 4) % 4
            val uw = ((w - u) + 4) % 4
            return uw in 1 until uv
        }
        for (i in list.indices) for (j in i + 1 until list.size) {
            for (p in list[i]) for (q in list[i]) {
                if (p == q) continue
                for (r in list[j]) for (s in list[j]) {
                    if (r == s) continue
                    if (inside(p, q, r) != inside(p, q, s)) return true
                }
            }
        }
        return false
    }
    val pat = IntArray(4)
    fun rec(i: Int) {
        if (i == 4) { if (!crossing(pat)) out.add(pat.copyOf()); return }
        val seen = HashSet<Int>()
        for (k in 0 until i) seen.add(pat[k])
        for (b in seen) { pat[i] = b; rec(i + 1) }   // 并入已有块
        pat[i] = i; rec(i + 1)                        // 开一个新块（编号 = 首元素下标）
    }
    rec(0)
    return out
}

private val PATTERNS: List<IntArray> = localPatterns()

// ─────────────────────────── 切面状态（4 bit 一类，打包进 Long） ───────────────────────────

private fun nib(s: Long, i: Int): Int = ((s ushr (4 * i)) and 15L).toInt()

private fun countNib(s: Long, k: Int, slots: Int): Int {
    var t = s
    var n = 0
    for (i in 0 until slots) { if ((t and 15L).toInt() == k) n++; t = t ushr 4 }
    return n
}

/** 把类标记 from 全部改写成 to（合并两个连通类）。 */
private fun rename(s0: Long, from: Int, to: Int): Long {
    var s = s0
    var i = 0
    while ((s ushr (4 * i)) != 0L) {
        if (nib(s, i) == from) s = s xor ((from.toLong() xor to.toLong()) shl (4 * i))
        i++
    }
    return s
}

/** 类标记按首次出现重新编号（0 保持为「外部」类），作为状态的规范形。 */
private fun canonical(s0: Long): Long {
    var s = s0
    var out = 0L
    var next = 0
    val seen = IntArray(16) { -1 }
    var i = 0
    while ((s ushr (4 * i)) != 0L) {
        val x = nib(s, i)
        if (seen[x] < 0) seen[x] = if (x == 0) 0 else ++next
        out = out or (seen[x].toLong() shl (4 * i))
        i++
    }
    return out
}

// ─────────────────────────── 单步：处理格点 (x,y) ───────────────────────────
//
// 切面槽位含义（处理 (x,y) 之前）：槽 y = SE 端口、槽 y+1 = SW 端口、槽 y+2 = NW 端口；
// NE 端口是本次新产生的。四个端口就是 (x,y) 周围的四个象限格：
//   SE = 格 (x, y−1)（存在 ⟺ y ≥ 1）、SW = 格 (x−1, y−1)（⟺ x,y ≥ 1）、
//   NW = 格 (x−1, y)（⟺ x ≥ 1）、NE = 格 (x, y)（⟺ x < rows 且 y < cols）。
// 不存在的格的端口属于「外部」类 0；它们必须整体处在一个只含外部端口的块里。
// SW 端口在本格点之后不再出现（它是格 (x−1,y−1) 的最后一个角），所以它所在的类如果就此
// 从切面上消失，就说明闭合了一条独立回路——只在最后一个格点允许。

private fun step(state: Long, x: Int, y: Int, rows: Int, cols: Int, last: Boolean): List<Long> {
    val raw = intArrayOf(nib(state, y), nib(state, y + 1), nib(state, y + 2))
    val newIsOutside = (x == rows || y == cols)
    val outside = booleanArrayOf(raw[0] == 0, raw[1] == 0, raw[2] == 0, newIsOutside)
    val cls = intArrayOf(raw[0], raw[1], raw[2], if (newIsOutside) 0 else 15)
    val results = ArrayList<Long>()
    for (pat in PATTERNS) {
        // 外部端口的块必须恰好由外部端口组成（所有外部端口在同一块）
        var ok = true
        for (i in 0..3) {
            if (!outside[i]) continue
            for (j in 0..3) if (outside[j] != (pat[i] == pat[j])) { ok = false; break }
            if (!ok) break
        }
        if (!ok) continue
        // 按块合并连通类；把新端口放在临时队列前端，便于复用槽位读写
        var work = (state shl 4) or cls[3].toLong()
        var bad = false
        for (i in 0..3) {
            if (i == pat[i]) continue                       // i 是所属块的代表
            val src = if (i == 3) cls[3] else nib(work, y + i + 1)
            val dst = nib(work, y + pat[i] + 1)
            if (src == 0) continue                          // 外部端口不参与合并
            if (src == dst || dst == 0) { bad = true; break } // 已是同一类：会提前闭合
            work = rename(work, src, dst)
        }
        if (bad) continue
        // NE 端口接管 SW 端口的槽位；检查被顶掉的类是否还有别的槽位
        val fresh = nib(work, 0)
        work = work ushr 4
        val displaced = nib(work, y + 1)
        if (countNib(work, displaced, cols + 4) > 1 || displaced == fresh || last) {
            work = work xor ((fresh.toLong() xor displaced.toLong()) shl (4 * (y + 1)))
            if (y == cols) work = work shl 4               // 换行：整体挪一个槽位
            results.add(canonical(work))
        }
    }
    return results
}

// ─────────────────────────── 扫描 DP ───────────────────────────
//
// rows = 行数（外层扫描方向），cols = 列数（切面宽度）。L 关于两个维度对称，
// 但切面宽度取较小维时状态数最少。

private fun countOriented(rows: Int, cols: Int, mod: Long): Long {
    var dp = HashMap<Long, Long>()
    dp[0L] = 1L
    for (x in 0..rows) for (y in 0..cols) {
        val next = HashMap<Long, Long>()
        val last = (x == rows && y == cols)
        for ((state, ways) in dp) {
            for (ns in step(state, x, y, rows, cols, last)) {
                next[ns] = ((next[ns] ?: 0L) + ways) % mod
            }
        }
        dp = next
    }
    return dp[0L] ?: 0L
}

private fun count(m: Int, n: Int, mod: Long): Long =
    countOriented(maxOf(m, n), minOf(m, n), mod)

// ─────────────────────────── 主程序 ───────────────────────────

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val got = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(got == expected) { "$tag 第 ${round + 1} 轮不一致：$got ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面样例 ----------
    val samples = listOf(1 to 2 to 2L, 2 to 2 to 37L, 3 to 3 to 104290L)
    for ((mn, want) in samples) {
        val (m, n) = mn
        val got = count(m, n, Long.MAX_VALUE)
        check(got == want) { "L($m,$n) = $got，题面为 $want" }
        println("样例：L($m,$n) = $got ✓")
    }

    // ---------- 2. 补充锚点 ----------
    val anchors = listOf(1 to 1 to 1L, 1 to 3 to 4L, 1 to 4 to 8L, 2 to 3 to 672L, 2 to 4 to 12182L)
    for ((mn, want) in anchors) {
        val (m, n) = mn
        val got = count(m, n, Long.MAX_VALUE)
        check(got == want) { "锚点 L($m,$n) = $got，预期 $want" }
        println("锚点：L($m,$n) = $got ✓")
    }

    // ---------- 3. 答案 ----------
    val MOD = 10_000_000_000L
    val answer = count(6, 10, MOD)
    println()
    println("L(6,10) mod 10^10 = $answer")

    // ---------- 4. 转置互证（切面宽度取较大维的另一条计算路径） ----------
    val transposed = countOriented(6, 10, MOD)   // 切面宽度 10 而不是 6
    check(transposed == answer) { "转置取向结果 $transposed ≠ $answer" }
    println("转置取向（切面宽度取 10）：$transposed ✓")

    // ---------- 5. 计时 ----------
    count(6, 10, MOD)
    val msSmall = bestOf3("主路径（切面宽度 6，模 10^10）", answer) { count(6, 10, MOD) }
    val t1 = System.nanoTime()
    check(countOriented(6, 10, MOD) == answer)
    val msLarge = (System.nanoTime() - t1) / 1e6
    println("转置取向（切面宽度 10）：${"%.1f".format(msLarge)} ms（单次；状态空间大得多，仅作互证不计入 baseline）")
    println("汇总：主路径 ${"%.1f".format(msSmall)} ms / 转置 ${"%.1f".format(msLarge)} ms")
    println("check 全部通过")
}
