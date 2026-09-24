#!/usr/bin/env kotlin
// PE 220 — Heighway Dragon（海威龙曲线）暴力参照
// 思路：按重写规则老老实实把 D_n 展开成字符串（n ≤ 20 时 D_20 含 2^20 = 1048576 个 F），
//       再逐字符执行 F/L/R 指令、满 k 步即停，直接读出坐标与朝向。
//       用来校验 solution.kt 的「按层压缩 + 自顶向下导航」：题面锚点 D_10 第 500 步应为
//       (18,16)，另有 D_12 / D_16 / D_18 三组外推对照（三组坐标完全相同即为通过）。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

private val DX = longArrayOf(0, 1, 0, -1)
private val DY = longArrayOf(1, 0, -1, 0)

/** D_n 的完整展开（n ≤ 20 可用）：D_n = "F" + 展开 n 次后的 a。 */
private fun dragon(n: Int): String {
    var s = StringBuilder("a")
    repeat(n) {
        val b = StringBuilder(s.length * 3)
        for (ch in s) when (ch) {
            'a' -> b.append("aRbFR")
            'b' -> b.append("LFaLb")
            else -> b.append(ch)
        }
        s = b
    }
    return "F" + s
}

/** 走完 k 步后的 (x, y, 朝向)；朝向 0 = 上，R 顺时针。 */
private fun walk(n: Int, k: Long): LongArray {
    var x = 0L
    var y = 0L
    var o = 0
    var c = 0L
    for (ch in dragon(n)) {
        if (c >= k) break
        when (ch) {
            'F' -> {
                x += DX[o]
                y += DY[o]
                c++
            }
            'R' -> o = (o + 1) and 3
            'L' -> o = (o + 3) and 3
        }
    }
    return longArrayOf(x, y, o.toLong())
}

fun main() {
    val t0 = System.nanoTime()
    for (spec in listOf(longArrayOf(10, 500), longArrayOf(12, 3_000), longArrayOf(16, 40_000), longArrayOf(18, 200_000))) {
        val n = spec[0].toInt()
        val r = walk(n, spec[1])
        println("D_$n 第 ${spec[1]} 步：(${r[0]},${r[1]})  朝向 ${r[2]}")
    }
    val ms = (System.nanoTime() - t0) / 1_000_000
    System.err.println("暴力展开 n ≤ 18 wall = $ms ms")
}
