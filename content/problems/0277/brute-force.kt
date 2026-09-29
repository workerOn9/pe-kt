#!/usr/bin/env kotlin
/**
 * Project Euler 277 — A Modified Collatz Sequence：直接暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 三条路：
 *
 *   路径 1（题面锚点，逐位模拟）：231 的步串 = "DdDddUUdDD"；1004064 的完整步串；并从 10^6+1
 *   开始逐个 a_1 正向模拟，直到步串以 "DdDddUUdDD" 开头——复现「1004064 是 > 10^6 的最小解」。
 *
 *   路径 2（穷举验证「剩余类结构」，brute 口径）：在 [1, 10^7] 上逐个 a_1 模拟，记录它最多匹配
 *   目标串前 j 个字符（j ≤ 12），得到 M_j = {a : 步串以目标串前 j 位开头}。对每个 j 验证 M_j
 *   恰好是「a ≡ base_j (mod 3^j)」的一个剩余类：正向（每个元素都在类里）+ 反向（区间内每个类
 *   成员都被直接模拟确认命中）+ 计数相等，并检查 base_j 是 base_{j−1} 的提升。这正是 solution.kt
 *   主路径依赖的结构，在 10^7 规模上被穷举钉死。
 *
 *   路径 3（规模外推）：实测「逐候选 + 逐字符早退」的扫描速率，并说明直接暴力要走到 a_1 ~ 10^15
 *   需要跨过 10^15 量级的候选（见输出里的天数），所以必须做剩余类提升。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 */

private const val TARGET = "UDDDUdddDDUDDddDdDddDDUDDdUUDd"

/** 走一步（独立实现）：返回 (步字符, 下一项)。 */
private fun advance(a: Long): Pair<Char, Long> {
    val r = a % 3L
    return when {
        r == 0L -> 'D' to a / 3
        r == 1L -> 'U' to (4 * a + 2) / 3
        else -> 'd' to (2 * a - 1) / 3
    }
}

/** 从 a 出发的步串（最多 len 个字符；到 1 就停）。 */
private fun steps(a: Long, len: Int): String {
    val sb = StringBuilder()
    var x = a
    while (sb.length < len && x != 1L) {
        val (c, y) = advance(x)
        sb.append(c)
        x = y
    }
    return sb.toString()
}

/** 步串是否以 s 开头（不建串、逐字符早退；a = 1 立即终止不算匹配）。 */
private fun startsWith(a: Long, s: String): Boolean {
    var x = a
    for (i in s.indices) {
        if (x == 1L) return false
        val (c, y) = advance(x)
        if (c != s[i]) return false
        x = y
    }
    return true
}

/** 从 a 出发最多匹配 s 的前 cap 个字符，返回匹配长度。 */
private fun matchLen(a: Long, s: String, cap: Int): Int {
    var x = a
    var n = 0
    while (n < cap && x != 1L) {
        val (c, y) = advance(x)
        if (c != s[n]) break
        n++
        x = y
    }
    return n
}

/** 在 [1, limit] 上穷举，按「至少匹配前 j 位」分组；不打印。 */
private fun scanMatches(limit: Long, cap: Int): Array<ArrayList<Long>> {
    val byLevel = Array(cap + 1) { ArrayList<Long>() }
    var a = 1L
    while (a <= limit) {
        val n = matchLen(a, TARGET, cap)
        for (j in 1..n) byLevel[j].add(a)
        a++
    }
    return byLevel
}

/** 逐层核对剩余类结构；verbose 时打印明细。返回最后一层的命中数。 */
private fun verifyLevels(byLevel: Array<ArrayList<Long>>, limit: Long, cap: Int, verbose: Boolean): Long {
    var mod = 1L
    var prevBase = -1L
    for (j in 1..cap) {
        mod *= 3
        val list = byLevel[j]
        check(list.isNotEmpty()) { "第 $j 层在 [1,$limit] 上没有元素" }
        val base = list[0]
        for (v in list) check(v % mod == base % mod) { "第 $j 层元素 $v 不在类 $base (mod $mod) 中" }
        var cnt = 0L
        var y = base
        while (y <= limit) {
            check(startsWith(y, TARGET.substring(0, j))) { "类成员 $y 未命中前 $j 位" }
            cnt++
            y += mod
        }
        check(cnt == list.size.toLong()) { "第 $j 层：类成员数 $cnt ≠ 穷举命中数 ${list.size}" }
        if (j > 1) check(base % (mod / 3) == prevBase % (mod / 3)) { "第 $j 层 base 不是上一层 base 的提升" }
        prevBase = base
        if (verbose) {
            println("  第 $j 位：命中 ${list.size} 个 = [1,$limit] 中类 $base mod $mod 的全部元素" +
                "（正向覆盖 + 逐一模拟复核 + 计数相等）")
        }
    }
    return byLevel[cap].size.toLong()
}

private fun timed(tag: String, rounds: Int, body: () -> Unit): Double {
    body()
    var best = Double.MAX_VALUE
    repeat(rounds) { round ->
        val t0 = System.nanoTime()
        body()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（$rounds 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 路径 1：题面锚点 ----------
    val anchor = "DdDddUUdDD"
    val s231 = steps(231L, 10)
    check(s231 == anchor) { "231 的步串应为 $anchor，实际 $s231" }
    val s1004064 = steps(1004064L, 30)
    check(s1004064 == "DdDddUUdDDDdUDUUUdDdUUDDDUdDD") { "1004064 的步串不符：$s1004064" }
    var x = 1_000_001L
    while (!startsWith(x, anchor)) x++
    check(x == 1004064L) { "> 10^6 的最小解应为 1004064，扫描得到 $x" }
    println("路径 1：231 → $s231；1004064 → $s1004064；扫描得 >10^6 最小解 = $x")

    // ---------- 路径 2：穷举剩余类结构（10^7 规模） ----------
    val cap = 12
    val levels = scanMatches(10_000_000L, cap)
    val hits = verifyLevels(levels, 10_000_000L, cap, verbose = true)
    println("路径 2：上表即 [1,10^7] 的穷举结果，M_j 与类 base_j mod 3^j 完全重合（第 $cap 层共 $hits 个）")

    // ---------- 路径 3：扫描速率与外推 ----------
    var cnt = 0L
    var a = 1L
    val t0 = System.nanoTime()
    while (a <= 100_000_000L) {
        if (startsWith(a, TARGET)) cnt++
        a++
    }
    val rateMs = (System.nanoTime() - t0) / 1e6
    val perSec = 100_000_000.0 / rateMs * 1000.0
    check(cnt == 0L) { "在 [1,10^8] 内不该出现全串命中" }
    println("路径 3：全串扫描 [1,10^8]（命中 $cnt 个）用时 ${"%.1f".format(rateMs)} ms，" +
        "≈ ${"%.1f".format(perSec / 1e6)} 百万候选/秒")

    // ---------- 计时：路径 2 作为 brute 口径 ----------
    scanMatches(1_000_000L, 8)
    val msPath2 = timed("路径 2：穷举 [1,10^7]（brute 口径）", 3) {
        val out = scanMatches(10_000_000L, cap)
        verifyLevels(out, 10_000_000L, cap, verbose = false)
    }

    println()
    println("汇总：路径 2（brute 口径，含逐层核对）${"%.1f".format(msPath2)} ms；" +
        "扫描速率 ${"%.2e".format(perSec)} 候选/秒")
    println("外推：直接暴力要从 1 扫到答案 1125977393124310，按本机速率需约 " +
        "${"%.1f".format(1_125_977_393_124_310.0 / perSec / 86400)} 天" +
        "（先跨过类中最小元素 96521732651065 也要约 ${"%.1f".format(96_521_732_651_065.0 / perSec / 86400)} 天）；" +
        "所以 10^15 规模必须做剩余类提升")
    println("check() 全部通过")
}
