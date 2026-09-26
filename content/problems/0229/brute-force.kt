#!/usr/bin/env kotlin
/**
 * Project Euler 229 — Four Representations Using Squares · 暴力对照版
 *
 * 思路：不分块、不用位图，直接用 BooleanArray 标记 + 逐个统计，
 *       只在 N = 1e7（题面自带的校验值）与 N = 3e7 上跑，用来验证计数逻辑与耗时对比。
 *       N = 2e9 需要 2 GB 的 BooleanArray，本机内存放不下 —— 这正是分块位图解法存在的原因。
 *
 * 复杂度：O(N·Σ1/√k) 次置位 + O(N) 次统计。
 * 构建：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 * 运行：java -jar bf.jar
 */

private val KS = intArrayOf(1, 2, 3, 7)

/** 朴素版：4 张 BooleanArray 求交。limit ≤ 1e8 才放得下。 */
fun bruteCount(limit: Int): Int {
    val n = limit + 1
    val ok = Array(4) { BooleanArray(n) }
    for (ki in KS.indices) {
        val k = KS[ki]
        val arr = ok[ki]
        var b = 1
        val bmax = Math.sqrt((limit - 1.0) / k).toInt()
        while (b <= bmax) {
            val base = k * b * b
            var aMax = Math.sqrt((limit - base).toDouble()).toInt()
            while ((aMax + 1) * (aMax + 1) <= limit - base) aMax++
            while (aMax * aMax > limit - base) aMax--
            for (a in 1..aMax) arr[base + a * a] = true
            b++
        }
    }
    var count = 0
    for (i in 1..limit) {
        if (ok[0][i] && ok[1][i] && ok[2][i] && ok[3][i]) count++
    }
    return count
}

fun main() {
    for (lim in intArrayOf(1_000_000, 10_000_000, 30_000_000)) {
        val t0 = System.nanoTime()
        val c = bruteCount(lim)
        val ms = (System.nanoTime() - t0) / 1_000_000
        println("limit=%-11d count=%-10d %6d ms".format(lim, c, ms))
    }
    println("（题面校验：limit=1e7 应为 75373）")
}
