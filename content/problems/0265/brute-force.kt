#!/usr/bin/env kotlin
/**
 * Project Euler 265 — Binary Circles（二进制圆圈）· 直接暴力对照
 *
 * 完全按定义实现，不使用任何图论/欧拉回路结构：
 *   对每个「前 N 位为 0」的 2^N 位候选位串（共 2^{2^N−N} 个），
 *   检查它的 2^N 个循环 N 位窗口是否两两不同
 *   （第 j 个窗口 = 位串在 2^N 位宽度内循环左移 j 位后的最高 N 位）。
 *   全部不同即为一个合法圆排列，其数值就是位串本身（前 N 位为 0，等价于题目「从全零子序列
 *   开始拼接」的编码）。
 *
 * 前 N 位为 0 的限定保证：合法候选必然以唯一的全零窗口开头，所以每个圆排列恰好被数一次，
 * 无需再去重。这一个文件同时充当「暴力实现是否理解对了题意」的检验器。
 *
 * 运行：bash scripts/kotlinc-shim.sh content/problems/0265/brute-force.kt
 *       java -cp /tmp/kc-265-brute:… Brute_forceKt
 */

/** 返回 (合法圆排列个数, 编码和)。 */
private fun bruteForce(n: Int): LongArray {
    val windowBits = 1 shl n
    val freeBits = windowBits - n
    val total = 1L shl freeBits
    val fullMask = (1L shl windowBits) - 1
    var count = 0L
    var sum = 0L
    var v = 0L
    while (v < total) {
        var mask = 0L
        var j = 0
        var ok = true
        while (j < windowBits) {
            val r = ((v shl j) or (v ushr (windowBits - j))) and fullMask
            val w = (r ushr (windowBits - n)).toInt()
            val bit = 1L shl w
            if (mask and bit != 0L) {
                ok = false
                break
            }
            mask = mask or bit
            j++
        }
        if (ok) {
            count++
            sum += v
        }
        v++
    }
    return longArrayOf(count, sum)
}

/** 列出 N=3 的全部编码（用于与题面 23、29 对照）。 */
private fun listEncodings(n: Int): List<Long> {
    val windowBits = 1 shl n
    val total = 1L shl (windowBits - n)
    val fullMask = (1L shl windowBits) - 1
    val out = ArrayList<Long>()
    var v = 0L
    while (v < total) {
        var mask = 0L
        var j = 0
        var ok = true
        while (j < windowBits) {
            val r = ((v shl j) or (v ushr (windowBits - j))) and fullMask
            val w = (r ushr (windowBits - n)).toInt()
            val bit = 1L shl w
            if (mask and bit != 0L) { ok = false; break }
            mask = mask or bit
            j++
        }
        if (ok) out.add(v)
        v++
    }
    return out
}

fun main() {
    println("直接暴力（按定义逐候选检查循环窗口）")
    println("N=3：编码 ${listEncodings(3)} → S(3) = ${bruteForce(3).toList()}")
    for (n in intArrayOf(4, 5)) {
        val t0 = System.nanoTime()
        val res = bruteForce(n)
        val ms = (System.nanoTime() - t0) / 1e6
        println("N=$n：候选 ${1L shl (1 shl n) - n} 个，合法 ${res[0]} 个，S($n) = ${res[1]}（${"%.1f".format(ms)} ms）")
    }
    // N=5 完整规模的 JIT 预热后 3 轮最优
    bruteForce(3)
    bruteForce(4)
    bruteForce(5)
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val res = bruteForce(5)
        val ms = (System.nanoTime() - t0) / 1e6
        check(res[1] == 209_110_240_768L) { "N=5 暴力结果漂移：${res.toList()}" }
        if (ms < best) best = ms
        println("N=5 第 ${round + 1} 轮：${res.toList()}（${"%.1f".format(ms)} ms）")
    }
    println("N=5 直接暴力：${"%.1f".format(best)} ms（JIT 预热后 3 轮最优）")
}
