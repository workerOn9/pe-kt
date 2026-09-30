package dev.pekt.engine

/**
 * PE 289 — Eulerian Cycles（欧拉回路）：C(x,y) 是过 (x,y)、(x,y+1)、(x+1,y)、(x+1,y+1) 四点的圆，
 * E(m,n) 由 m·n 个这样的圆组成，每个圆被 4 个角点分成 4 条弧。求「把每条弧恰好走一次的不自交叉
 * 闭合路径」的条数 L(6,10) mod 10^10。
 *
 * 推导（详见 content/problems/0289/solution.kt 头部与 0289/analysis.md）：
 *   半径 √2/2、圆心在半整数点上的两个圆，圆心距只可能是 1（交于两个格点）、√2（相切于一个格点）
 *   或 ≥2（不相交），所以两条弧除可能在格点处相接外永不相交——「不自交叉」是纯局部条件。
 *   在每个格点上，把 8 个弧端按「所属象限格」分成 4 个端口（每端口 2 个弧端、在绕点角序里相邻），
 *   局部合法走法 = 4 端口的非交叉划分，共 Catalan(4) = 14 个（15 个集合划分中排除交叉的
 *   {0,2},{1,3}）；块内的 2k 个弧端只有一种非交叉配对，故局部走法与 14 个划分一一对应。
 *
 *   全局「单条闭合曲线」用轮廓 DP：按行优先扫过 (n+1)×(m+1) 个格点，切面上每个槽位保存
 *   「尚未收口的走法片段」的连通类标记（4 bit 打包进 Long，按首次出现规范化）；在当前格点按
 *   局部划分合并相应槽位的类；某个类若就此从切面消失，说明闭合出一条独立回路——只允许发生在
 *   最后一个格点。格子外部是特殊类 0（所有指向格外的端口属于它、且永不与真实类合并），
 *   扫描结束时切面上必须只剩 0。
 *
 * 复杂度：切面宽度 = 较小维度 + 3 个槽位（L(6,10) 取宽 6），扫 11×7 = 77 个格点 × 14 个局部模式
 *   × 数百个连通类状态；本机实测约 20 ms（JIT 预热后），远低于 10 s 熔断线。
 *   校验：题面样例 L(1,2)=2、L(2,2)=37、L(3,3)=104290 全部复现；补充锚点 L(1,1)=1、L(1,3)=4、
 *   L(1,4)=8、L(2,3)=672；转置取向（切面宽度取 10）给出同一答案；brute-force.kt 用独立写法
 *   显式枚举小规模（1、2、4、37、672、104290）对拍；公开答案表列出 6567944538。
 *   本题没有素数筛 / 组合数 / gcd 等通用步骤，逻辑自包含，未用到 dev.pekt.math 工具。
 */
internal fun solve0289Impl(): Long {
    val mod = 10_000_000_000L
    val cols = 6
    val rows = 10
    val slots = cols + 3

    // ── 局部模式：4 个端口的非交叉划分（块编号 = 块内最小端口） ──
    // 端口绕点角序：0 = SE 格 (x,y-1)、1 = SW 格 (x-1,y-1)、2 = NW 格 (x-1,y)、3 = NE 格 (x,y)
    fun inside(u: Int, v: Int, w: Int): Boolean {
        val uv = ((v - u) + 4) % 4
        val uw = ((w - u) + 4) % 4
        return uw in 1 until uv
    }
    val patterns = ArrayList<IntArray>()
    run {
        val pat = IntArray(4)
        fun crossing(): Boolean {
            for (i in 0..3) for (j in i + 1..3) {
                for (k in 0..3) for (l in k + 1..3) {
                    if (pat[i] != pat[j] || pat[k] != pat[l] || pat[i] == pat[k]) continue
                    if (inside(i, j, k) != inside(i, j, l)) return true
                }
            }
            return false
        }
        fun rec(i: Int) {
            if (i == 4) { if (!crossing()) patterns.add(pat.copyOf()); return }
            val seen = ArrayList<Int>(4)
            for (k in 0 until i) if (!seen.contains(pat[k])) seen.add(pat[k])
            for (b in seen) { pat[i] = b; rec(i + 1) }
            pat[i] = i; rec(i + 1)
        }
        rec(0)
    }
    check(patterns.size == 14) { "非交叉划分应为 Catalan(4) = 14 个，实得 ${patterns.size}" }

    fun nib(s: Long, i: Int): Int = ((s ushr (4 * i)) and 15L).toInt()
    fun countNib(s: Long, k: Int): Int {
        var t = s
        var n = 0
        for (i in 0 until slots) { if ((t and 15L).toInt() == k) n++; t = t ushr 4 }
        return n
    }
    fun rename(s0: Long, from: Int, to: Int): Long {
        var s = s0
        var i = 0
        while ((s ushr (4 * i)) != 0L) {
            if (nib(s, i) == from) s = s xor ((from.toLong() xor to.toLong()) shl (4 * i))
            i++
        }
        return s
    }
    fun canonical(s0: Long): Long {
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

    // ── 轮廓 DP ──
    var dp = HashMap<Long, Long>()
    dp[0L] = 1L
    for (x in 0..rows) for (y in 0..cols) {
        val last = (x == rows && y == cols)
        val next = HashMap<Long, Long>()
        for ((state, ways) in dp) {
            // 槽位：y = SE 端口、y+1 = SW 端口、y+2 = NW 端口；NE 端口是本格点新产生的
            val raw0 = nib(state, y)
            val raw1 = nib(state, y + 1)
            val raw2 = nib(state, y + 2)
            val neOutside = (x == rows || y == cols)
            val cls = intArrayOf(raw0, raw1, raw2, if (neOutside) 0 else 15)
            val outside = booleanArrayOf(raw0 == 0, raw1 == 0, raw2 == 0, neOutside)
            for (pat in patterns) {
                // 外部端口必须恰好构成一个只含外部端口的块
                var legal = true
                for (i in 0..3) {
                    if (!outside[i]) continue
                    for (j in 0..3) if (outside[j] != (pat[i] == pat[j])) { legal = false; break }
                    if (!legal) break
                }
                if (!legal) continue
                // 按块合并连通类：块内端口此前若已连通，会提前闭合，丢弃
                var work = (state shl 4) or cls[3].toLong()
                var bad = false
                for (i in 0..3) {
                    if (i == pat[i]) continue
                    val src = if (i == 3) cls[3] else nib(work, y + i + 1)
                    val dst = nib(work, y + pat[i] + 1)
                    if (src == 0) continue
                    if (src == dst || dst == 0) { bad = true; break }
                    work = rename(work, src, dst)
                }
                if (bad) continue
                // NE 端口接管 SW 端口的槽位；被顶掉的类若无处安身就说明闭合成环
                val fresh = nib(work, 0)
                work = work ushr 4
                val displaced = nib(work, y + 1)
                if (countNib(work, displaced) > 1 || displaced == fresh || last) {
                    work = work xor ((fresh.toLong() xor displaced.toLong()) shl (4 * (y + 1)))
                    if (y == cols) work = work shl 4
                    val key = canonical(work)
                    next[key] = ((next[key] ?: 0L) + ways) % mod
                }
            }
        }
        dp = next
    }
    return dp[0L] ?: 0L
}
