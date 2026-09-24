#!/usr/bin/env kotlin
// PE 213 — Flea Circus（跳蚤马戏团）暴力参照
// 思路：蒙特卡洛模拟。反复模拟 50 次响铃，统计每次的空格数并取平均。
//       单次模拟的方差约为 900·p(1-p)，5000 次模拟的标准误约 0.2，
//       只能把期望值锚定到 ±0.5 量级，用于确认解析解没算错数量级。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

private const val SIDE = 30
private const val CELLS = SIDE * SIDE
private const val RINGS = 50
private const val TRIALS = 5000

fun main() {
    val deg = IntArray(CELLS)
    val nbr = Array(CELLS) { IntArray(4) }
    for (i in 0 until SIDE) {
        for (j in 0 until SIDE) {
            val c = i * SIDE + j
            var dcount = 0
            if (i > 0) nbr[c][dcount++] = c - SIDE
            if (i < SIDE - 1) nbr[c][dcount++] = c + SIDE
            if (j > 0) nbr[c][dcount++] = c - 1
            if (j < SIDE - 1) nbr[c][dcount++] = c + 1
            deg[c] = dcount
        }
    }

    val rnd = java.util.Random(20260925L)
    val occupied = BooleanArray(CELLS)
    var totalEmpty = 0L
    for (trial in 0 until TRIALS) {
        val fleas = IntArray(CELLS) { it }  // 初始每格一只
        repeat(RINGS) {
            for (f in 0 until CELLS) {
                val c = fleas[f]
                fleas[f] = nbr[c][rnd.nextInt(deg[c])]
            }
        }
        java.util.Arrays.fill(occupied, false)
        for (f in 0 until CELLS) occupied[fleas[f]] = true
        var empty = 0
        for (c in 0 until CELLS) if (!occupied[c]) empty++
        totalEmpty += empty
    }
    println("空格数期望 ≈ " + totalEmpty.toDouble() / TRIALS)
}
