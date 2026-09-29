#!/usr/bin/env kotlin
/**
 * Project Euler 270 — Cutting Squares：直接暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 两条独立的暴力路径：
 *
 *   路径 1（N ≤ 3，完全按题面定义）：把正方形边界的 4N 个整数点编号，合法割线 = 两点边掩码
 *   不相交；用「最小可加割线分含/不含两支」的规范枚举列出全部极大非交叉割线集，到叶子再验证
 *   极大性。复现题面锚点 C(1) = 2、C(2) = 30，并给出 C(3) = 604。
 *
 *   路径 2（N ≤ 7，直接枚举三角剖分）：不加任何记忆化，递归枚举 M = 4N 边形的全部三角剖分，
 *   只保留对角线合法的那些（等价于极大割线集，见 solution.kt 的结构定理）。给出
 *   C(4) = 12168、C(5) = 238848、C(6) = 4569624、C(7) = 85553528。
 *
 *   路径 2 的规模增长约每 N 乘 18.7 倍（叶子数），N = 30 时需要枚举 ~10^38 个剖分，不可行；
 *   可行路径是 solution.kt 的 O(M³) 区间 DP。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private fun sideMasks(n: Int): IntArray {
    val m = 4 * n
    return IntArray(m) { p ->
        var b = 0
        if (p <= n) b = b or 1
        if (p in n..2 * n) b = b or 2
        if (p in 2 * n..3 * n) b = b or 4
        if (p >= 3 * n || p == 0) b = b or 8
        b
    }
}

private fun legal(mask: IntArray, i: Int, j: Int): Boolean = (mask[i] and mask[j]) == 0

private fun crosses(p: Int, q: Int, r: Int, s: Int, m: Int): Boolean {
    if (p == r || p == s || q == r || q == s) return false
    val arc = ((q - p) % m + m) % m
    fun inside(x: Int): Boolean {
        val d = ((x - p) % m + m) % m
        return d > 0 && d < arc
    }
    return inside(r) != inside(s)
}

/** 路径 1：按题面定义枚举极大非交叉合法割线集（含/不含规范分支 + 叶子极大性校验）。 */
private fun maximalCutSets(n: Int): Long {
    val m = 4 * n
    val mask = sideMasks(n)
    val chords = ArrayList<IntArray>()
    for (i in 0 until m) for (j in i + 1 until m) if (legal(mask, i, j)) chords.add(intArrayOf(i, j))
    val c = chords.size
    check(c <= 62)
    val cr = LongArray(c)
    for (x in 0 until c) for (y in x + 1 until c) {
        if (crosses(chords[x][0], chords[x][1], chords[y][0], chords[y][1], m)) {
            cr[x] = cr[x] or (1L shl y)
            cr[y] = cr[y] or (1L shl x)
        }
    }
    var count = 0L
    fun dfs(inc: Long, exc: Long) {
        var inc2 = inc
        var exc2 = exc
        for (i in 0 until c) {
            val bit = 1L shl i
            if ((inc2 and bit) != 0L || (exc2 and bit) != 0L) continue
            if ((cr[i] and inc2) != 0L) { exc2 = exc2 or bit; continue }
            dfs(inc2 or bit, exc2)
            dfs(inc2, exc2 or bit)
            return
        }
        for (i in 0 until c) if ((inc2 and (1L shl i)) == 0L && (cr[i] and inc2) == 0L) return
        count++
    }
    dfs(0L, 0L)
    return count
}

/** 路径 2：直接枚举三角剖分（无记忆化），只保留合法对角线。 */
private fun triangulations(n: Int): Long {
    val m = 4 * n
    val mask = sideMasks(n)
    fun rec(poly: IntArray): Long {
        val k = poly.size
        if (k <= 3) return 1L
        val v0 = poly[0]
        val v1 = poly[1]
        var total = 0L
        for (t in 2 until k) {
            val vt = poly[t]
            if (!(t == 2 || legal(mask, v1, vt))) continue
            if (!(t == k - 1 || legal(mask, v0, vt))) continue
            val p1 = poly.copyOfRange(1, t + 1)
            val p2 = IntArray(k - t + 1)
            p2[0] = v0
            for (u in t until k) p2[1 + u - t] = poly[u]
            total += rec(p1) * rec(p2)
        }
        return total
    }
    return rec(IntArray(m) { it })
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 路径 1：题面定义（N ≤ 3） ----------
    for (n in 1..3) {
        val v = maximalCutSets(n)
        println("路径 1（按定义枚举极大割线集）N=$n：C($n) = $v")
    }
    check(maximalCutSets(1) == 2L && maximalCutSets(2) == 30L) { "题面锚点 C(1)=2、C(2)=30 未复现" }
    println("题面锚点 C(1) = 2、C(2) = 30 复现；C(3) = 604")

    // ---------- 路径 2：直接枚举三角剖分（N ≤ 7） ----------
    val expected = mapOf(4 to 12168L, 5 to 238848L, 6 to 4569624L, 7 to 85553528L)
    for ((n, exp) in expected.entries.sortedBy { it.key }) {
        val v = triangulations(n)
        check(v == exp) { "N=$n：枚举三角剖分 $v ≠ 期望 $exp" }
        println("路径 2（直接枚举三角剖分）N=$n：C($n) = $v")
    }

    // ---------- 计时 ----------
    maximalCutSets(3)
    triangulations(5)
    val msLit = bestOf3("路径 1：按定义枚举（N=3）", 604L) { maximalCutSets(3) }
    val ms6 = bestOf3("路径 2：三角剖分枚举（N=6，4.57×10⁶ 个剖分）", 4569624L) { triangulations(6) }
    triangulations(7)
    val ms7 = bestOf3("路径 2：三角剖分枚举（N=7，8.56×10⁷ 个剖分）", 85553528L) { triangulations(7) }

    println()
    println("汇总：路径 1 N=3 ${"%.1f".format(msLit)} ms；路径 2 N=6 ${"%.1f".format(ms6)} ms；" +
        "路径 2 N=7 ${"%.1f".format(ms7)} ms（3 轮最优，JIT 预热后）")
    println("外推：剖分数每 N 约乘 18.7，N=30 需枚举 ~10^38 个剖分，不可行；" +
        "这次暴力只用于把 DP 的正确性钉到 N=7（8790 万）的规模")
    println("check() 全部通过")
}
