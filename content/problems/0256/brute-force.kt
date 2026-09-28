#!/usr/bin/env kotlin
/**
 * Project Euler 256 — Tatami-Free Rooms（无榻榻米铺法的房间）：小规模暴力对照
 *
 * 与 solution.kt 不共享核心代码路径，本文件独立验证「tatami-free」的数学刻画：
 *   设 a ≤ b、面积偶数，q = ⌊b/a⌋，则 tatami-free ⟺ (a+1)q + 2 ≤ b ≤ (a-1)(q+1) - 2。
 *
 * 暴力方法一（字面规则）：枚举小棋盘 a×b 的全部 1×2 铺法（DFS，无剪枝），对每个
 *   铺法在每个内部格点统计「以该点为角的垫子块数」（垫子的角 = 其外接矩形的四个
 *   格点），任一点 ≥ 4 即违规。这是对题面规则最直白的翻译，用于 a ≤ 6、b ≤ 8。
 *
 * 暴力方法二（行轮廓 DP）：把「无四角相遇」等价改写为「每个内部 2×2 都含一块完整
 *   垫子」；按行扫描，状态 = (本行被上方竖垫占据的格集合 occ, 上一行横垫左端集合
 *   hleft)，枚举本行铺法 (D, hl) 时校验行间条件
 *       (M | (occ >> 1)) & (2^(b-1)-1) == 2^(b-1)-1,  M = hleft | occ | hl。
 *   对每个偶数面积对 a ≤ b ≤ 16 给出精确的真值表。
 *
 * 另外复现题面数据：T(70)=1（唯一自由房间 7×10）、T(1320)=5（20×66 等五个），
 * 并扫描确认「最小 T(s)=5 的 s 是 1320」。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private const val SHIFT = 32

private fun tfFree(a: Long, b: Long): Boolean {
    val q = b / a
    if (q == 0L) return false
    return b >= (a + 1) * q + 2 && b <= (a - 1) * (q + 1) - 2
}

// ---------------------------------------------------------------- 字面暴力：枚举全部铺法

/** 一块垫子：覆盖格 (r1,c1) 与 (r2,c2)，两者相邻 */
private data class Dom(val r1: Int, val c1: Int, val r2: Int, val c2: Int)

/** 枚举 a×b 的全部 1×2 铺法（用格子 DFS，无任何剪枝） */
private fun allTilings(a: Int, b: Int): List<List<Dom>> {
    val cover = Array(a) { BooleanArray(b) }
    val out = ArrayList<List<Dom>>()
    val cur = ArrayList<Dom>()
    fun rec(idx: Int) {
        if (idx == a * b) { out.add(ArrayList(cur)); return }
        val i = idx / b
        val j = idx % b
        if (cover[i][j]) { rec(idx + 1); return }
        if (j + 1 < b && !cover[i][j + 1]) {
            cover[i][j] = true; cover[i][j + 1] = true
            cur.add(Dom(i, j, i, j + 1)); rec(idx + 1); cur.removeAt(cur.size - 1)
            cover[i][j] = false; cover[i][j + 1] = false
        }
        if (i + 1 < a && !cover[i + 1][j]) {
            cover[i][j] = true; cover[i + 1][j] = true
            cur.add(Dom(i, j, i + 1, j)); rec(idx + 1); cur.removeAt(cur.size - 1)
            cover[i][j] = false; cover[i + 1][j] = false
        }
    }
    rec(0)
    return out
}

/** 一块垫子有角在格点 (i,j) ⟺ i ∈ {r1, r2+1} 且 j ∈ {c1, c2+1} */
private fun literalTileable(a: Int, b: Int): Boolean {
    if (a.toLong() * b % 2L != 0L) return false
    for (t in allTilings(a, b)) {
        val cnt = Array(a + 1) { IntArray(b + 1) }
        for (d in t) {
            cnt[d.r1][d.c1]++
            cnt[d.r1][d.c2 + 1]++
            cnt[d.r2 + 1][d.c1]++
            cnt[d.r2 + 1][d.c2 + 1]++
        }
        var ok = true
        for (i in 1 until a) { for (j in 1 until b) if (cnt[i][j] >= 4) { ok = false; break }; if (!ok) break }
        if (ok) return true
    }
    return false
}

// ---------------------------------------------------------------- 行轮廓 DP 精确判定

private fun enumerateRows(
    occ: Long,
    hleft: Long,
    b: Int,
    maskBm1: Long,
    checkRow: Boolean,
): LongArray {
    val out = ArrayList<Long>()
    fun rec(j: Int, d: Long, hl: Long) {
        if (j >= b) {
            if (checkRow) {
                val m = hleft or occ or hl
                if ((m or (occ ushr 1)) and maskBm1 != maskBm1) return
            }
            out.add(d or (hl shl SHIFT))
            return
        }
        if ((occ ushr j) and 1L == 1L) { rec(j + 1, d, hl); return }
        rec(j + 1, d or (1L shl j), hl)                                   // 竖垫向下
        if (j + 1 < b && (occ ushr (j + 1)) and 1L == 0L)                  // 横垫 (j, j+1)
            rec(j + 2, d, hl or (1L shl j))
    }
    rec(0, 0L, 0L)
    return out.toLongArray()
}

fun dpTileable(a: Int, b: Int): Boolean {
    if (a.toLong() * b % 2L != 0L) return false
    val full = (1L shl b) - 1L
    val maskBm1 = (1L shl (b - 1)) - 1L
    var states = HashSet<Long>()
    states.add(0L)
    for (i in 0 until a) {
        val next = HashSet<Long>()
        for (st in states) {
            val occ = st and full
            val hleft = (st ushr SHIFT) and full
            for (ns in enumerateRows(occ, hleft, b, maskBm1, i > 0)) next.add(ns)
        }
        states = next
        if (states.isEmpty()) return false
    }
    return states.any { it and full == 0L }
}

// ---------------------------------------------------------------- 小尺度 T(s)

/** 用刻画式数 T(s)：s 较小时试除到 √s 即可 */
private fun tOf(s: Long): Int {
    var cnt = 0
    var a = 1L
    while (a * a <= s) {
        if (s % a == 0L && tfFree(a, s / a)) cnt++
        a++
    }
    return cnt
}

// ---------------------------------------------------------------- 主验证流程

private fun verify(verbose: Boolean): Triple<Int, Int, Int> {
    // 1) 字面暴力 vs 刻画式（全部偶数面积对，a ≤ 6、b ≤ 8）
    var literalChecked = 0
    var literalMismatch = 0
    for (a in 1..6) for (b in a..8) {
        if (a.toLong() * b % 2L != 0L) continue
        literalChecked++
        val exactFree = !literalTileable(a, b)
        if (exactFree != tfFree(a.toLong(), b.toLong())) {
            literalMismatch++
            if (verbose) println("字面暴力与刻画式不一致：$a x $b")
        }
    }

    // 2) 行轮廓 DP 真值表 vs 刻画式（a ≤ b ≤ 16）
    var dpChecked = 0
    var dpMismatch = 0
    for (a in 1..16) for (b in a..16) {
        if (a.toLong() * b % 2L != 0L) continue
        dpChecked++
        val exactFree = !dpTileable(a, b)
        if (exactFree != tfFree(a.toLong(), b.toLong())) {
            dpMismatch++
            if (verbose) println("行轮廓 DP 与刻画式不一致：$a x $b")
        }
    }

    // 3) 题面数据
    var stmtBad = 0
    if (tOf(70) != 1) stmtBad++
    if (tOf(1320) != 5) stmtBad++
    var minT5 = -1L
    var s = 2L
    while (s <= 20_000L) { if (tOf(s) == 5) { minT5 = s; break }; s += 2 }
    if (minT5 != 1320L) stmtBad++
    if (verbose) {
        println("字面暴力（枚举全部铺法）：a ≤ 6、b ≤ 8 的 $literalChecked 个偶数面积对，不一致 $literalMismatch 处")
        println("行轮廓 DP 真值表：a ≤ b ≤ 16 的 $dpChecked 个偶数面积对，不一致 $dpMismatch 处")
        println("题面数据：T(70)=${tOf(70)}，T(1320)=${tOf(1320)}，最小 T(s)=5 的 s=$minT5（不一致 $stmtBad 处）")
    }
    return Triple(literalMismatch + dpMismatch, literalChecked + dpChecked, stmtBad)
}

fun main() {
    val (mismatch, checked, stmtBad) = verify(verbose = true)
    check(mismatch == 0 && stmtBad == 0) { "暴力对照失败" }
    println("暴力对照全部通过：$checked 个偶数面积对与刻画式零分歧，题面三个数据全部复现")

    // 计时：JIT 预热后 3 轮取最优（主验证流程，含字面枚举与 DP 真值表）
    verify(verbose = false)
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        verify(verbose = false)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
        println("  第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("暴力对照主流程（字面枚举 + DP 真值表 + 题面数据）：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    println("check() 全部通过")
}
