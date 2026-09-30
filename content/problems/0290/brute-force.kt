#!/usr/bin/env kotlin
/**
 * Project Euler 290 — Digital Signature（数字签名）：暴力 / 独立对照
 *
 * 独立实现，与 solution.kt 不共享核心代码。solution.kt 的两条路径分别用「逐位 DP」与
 * 「低 6 位枚举 + 高 12 位反向 DP」；本文件换成
 *   ① 定义级穷举：n < 10^8 用分块数字和表逐值判定（10^8 次判定 ≈ 秒级，已属暴力极限；
 *      10^18 需要约 10^12 年，物理上不可行）；
 *   ② 小规模独立实现：HashMap 版 (进位, 数字和之差) DP，负责 k = 1..8 的整段对拍；
 *   ③ 全尺寸第三套解：把 ① 的 10^8 穷举结果做成「低半直方图」，高 10 位用反向 DP（从最高位
 *      向分界倒推，进位由 (c_out, 乘积位 p, 数字 d) 唯一确定），在分界进位处配对 —— 与
 *      solution.kt 的路径 A（整体逐位 DP）、路径 B（6/12 折半、反向 DP 从 17 位走到第 6 位）
 *      在切分点、低半的获取方式（穷举 vs DP）上都不同。
 *
 * 三条路径的答案：20444710234716473（= solution.kt 两条路径的结果），n < 10^8 的计数为
 * 1934036（见运行输出），k = 1..8 的小规模计数与 HashMap DP 全等。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0290/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

private const val MULT = 137L
private const val CARRY_MAX = 137
private const val OFF = 200
private const val SIZE = 2 * OFF + 1

// ─────────────── 4 位分块数字和表（把逐值判定压到常数次查表） ───────────────

private val chunkSum = IntArray(10001).also { t ->
    for (x in 1..10000) t[x] = t[x / 10] + x % 10
}

/** digitSum(x)，x < 10^16，按 4 位分块查表。 */
private fun ds(x: Long): Int = chunkSum[(x % 10000).toInt()] +
    chunkSum[((x / 10000) % 10000).toInt()] +
    chunkSum[((x / 100_000_000) % 10000).toInt()] +
    chunkSum[(x / 1_000_000_000_000L).toInt()]

// ─────────────── ① 定义级穷举 n < 10^8（同时收集低 8 位直方图） ───────────────

private class LowHalf(val count8: Long, val hist: LongArray)   // hist[c * SIZE + (δ + OFF)]

private fun enumerateLow8(): LowHalf {
    val limit = 100_000_000L
    val hist = LongArray((CARRY_MAX + 1) * SIZE)
    var count = 0L
    var n = 0L
    while (n < limit) {
        val prod = MULT * n
        val carry = (prod / limit).toInt()
        val delta = ds(prod % limit) - ds(n)
        if (delta + chunkSum[carry] == 0) count++          // 高 10 位全为 0 时：δ_lo + digitSum(进位) = 0
        hist[carry * SIZE + delta + OFF]++
        n++
    }
    return LowHalf(count, hist)
}

/** 小规模穷举（10^6，用于校验 HashMap DP）。 */
private fun enumerateSmall(k: Int): Long {
    var limit = 1L
    repeat(k) { limit *= 10L }
    var count = 0L
    var n = 0L
    while (n < limit) {
        val prod = MULT * n
        if (ds(prod) == ds(n)) count++
        n++
    }
    return count
}

// ─────────────── ② 小规模独立实现：HashMap 版 DP ───────────────

/** dp[(c, δ)] = 方案数；从最低位逐位推进，最后补 3 个排空位后要求 (0, 0)。 */
private fun countByHashMapDP(k: Int): Long {
    var cur = HashMap<Int, Long>()
    fun key(c: Int, d: Int) = c * SIZE + d
    cur[key(0, OFF)] = 1L
    val positions = k + 3
    for (pos in 0 until positions) {
        val maxD = if (pos < k) 9 else 0
        val nxt = HashMap<Int, Long>()
        for ((kk, cnt) in cur) {
            val c = kk / SIZE
            val d = kk % SIZE - OFF
            for (digit in 0..maxD) {
                val v = MULT * digit + c
                val p = (v % 10).toInt()
                val c2 = (v / 10).toInt()
                val k2 = key(c2, d + p - digit + OFF)
                nxt[k2] = (nxt[k2] ?: 0L) + cnt
            }
        }
        cur = nxt
    }
    return cur[key(0, OFF)] ?: 0L
}

// ─────────────── ③ 全尺寸第三套解：低 8 位穷举 × 高 10 位反向 DP ───────────────

/**
 * 高 10 位（第 8..17 位）的反向 DP：初值 = 排空段（进位 c 贡献 digitSum(c) 位数字），
 * 反向转移 c_in = 10·c_out + p − 137·d（要求 c_in ∈ [0, 137]）。
 * 返回 high[c_8][δ_hi + OFF]，c_8 是进入第 8 位的进位。
 */
private fun highHalfReverse(): Array<LongArray> {
    var dp = Array(CARRY_MAX + 1) { LongArray(SIZE) }
    for (c in 0..CARRY_MAX) dp[c][chunkSum[c] + OFF] = 1L
    for (pos in 17 downTo 8) {
        val next = Array(CARRY_MAX + 1) { LongArray(SIZE) }
        for (cOut in 0..CARRY_MAX) {
            val row = dp[cOut]
            var touched = false
            for (i in 0 until SIZE) if (row[i] != 0L) { touched = true; break }
            if (!touched) continue
            for (d in 0..9) {
                for (p in 0..9) {
                    val cIn = 10 * cOut + p - 137 * d
                    if (cIn < 0 || cIn > CARRY_MAX) continue
                    val delta = p - d
                    val dst = next[cIn]
                    for (i in 0 until SIZE) {
                        val v = row[i]
                        if (v != 0L) dst[i + delta] += v
                    }
                }
            }
        }
        dp = next
    }
    return dp
}

/** 全量暴力路径：10^8 穷举直方图 + 高 10 位反向 DP + 进位配对。 */
private fun fullBruteAnswer(): Long {
    val low = enumerateLow8()
    val high = highHalfReverse()
    var total = 0L
    for (c in 0..CARRY_MAX) {
        for (i in 0 until SIZE) {
            val a = low.hist[c * SIZE + i]
            if (a == 0L) continue
            val b = high[c][SIZE - 1 - i]
            if (b != 0L) total += a * b
        }
    }
    return total
}

private fun best(tag: String, expected: Long, rounds: Int = 3, f: () -> Long): Double {
    var bestMs = Double.MAX_VALUE
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${r + 1} 轮漂移：$out ≠ $expected" }
        if (ms < bestMs) bestMs = ms
    }
    println("$tag：${"%.1f".format(bestMs)} ms（$rounds 轮最优）")
    return bestMs
}

fun main() {
    println("== ① 定义级穷举 n < 10^8（每值一次乘法 + 两次分块数字和）==")
    val low = enumerateLow8()
    println("n < 10^8 的计数 = ${low.count8}")

    println()
    println("== ② HashMap 版独立 DP：k = 1..8 对拍 ==")
    for (k in 1..6) {
        val brute = enumerateSmall(k)
        val dp = countByHashMapDP(k)
        check(brute == dp) { "k = $k：穷举 $brute ≠ HashMap DP $dp" }
        println("k = $k：10^$k 穷举 = HashMap DP = $brute")
    }
    val dp8 = countByHashMapDP(8)
    check(dp8 == low.count8) { "k = 8：10^8 穷举 ${low.count8} ≠ HashMap DP $dp8" }
    println("k = 7, 8：HashMap DP = ${countByHashMapDP(7)}, $dp8（与 10^8 穷举一致）")

    println()
    println("== ③ 全尺寸：低 8 位直方图（穷举）× 高 10 位反向 DP + 进位匹配 ==")
    val high = highHalfReverse()
    var total = 0L
    for (c in 0..CARRY_MAX) {
        for (i in 0 until SIZE) {
            val a = low.hist[c * SIZE + i]
            if (a == 0L) continue
            val b = high[c][SIZE - 1 - i]
            if (b != 0L) total += a * b
        }
    }
    println("第三套解 = $total")
    check(total == 20444710234716473L) { "与公开答案表不一致" }

    println()
    println("== 计时 ==")
    val msFull = best("全量暴力路径（10^8 穷举 + 高 10 位反向 DP + 配对）", 20444710234716473L, rounds = 1) {
        fullBruteAnswer()
    }
    val msEnum = best("其中 10^8 穷举（含直方图收集）", low.count8, rounds = 1) { enumerateLow8().count8 }
    val msHigh = best("其中高 10 位反向 DP", 0L) { if (highHalfReverse().size == CARRY_MAX + 1) 0L else 1L }
    val msSmall = best("HashMap DP k = 8", dp8) { countByHashMapDP(8) }
    println()
    println("暴力全量答案 = $total（= ${fullBruteAnswer()}）")
    println("分解耗时：全量 ${"%.1f".format(msFull)} ms【穷举 ${"%.1f".format(msEnum)} + 反向 DP ${"%.1f".format(msHigh)}】；小规模 DP ${"%.1f".format(msSmall)} ms")
}
