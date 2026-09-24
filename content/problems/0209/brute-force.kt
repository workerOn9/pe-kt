#!/usr/bin/env kotlin
// PE 209 暴力参照：2^64 个真值表无法枚举，改为「σ 置换环分解 + 环上逐点 DP」的朴素实现
// ——不使用闭式 Lucas 数 F_{L-1}+F_{L+1}，而是对每个环直接枚举首点选/不选后线性 DP，
// 与 solution.kt 的公式互为交叉验证。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

fun main() {
    fun sigma(s: Int): Int {
        val a = s shr 5 and 1
        val b = s shr 4 and 1
        val c = s shr 3 and 1
        val d = s shr 2 and 1
        val e = s shr 1 and 1
        val f = s and 1
        return (b shl 5) or (c shl 4) or (d shl 3) or (e shl 2) or (f shl 1) or (a xor (b and c))
    }
    val seen = BooleanArray(64)
    val lens = ArrayList<Int>()
    var ans = 1L
    for (s in 0..63) {
        if (seen[s]) continue
        var x = s
        var len = 0
        while (!seen[x]) { seen[x] = true; x = sigma(x); len++ }
        // 环 C_L 的独立集数：固定首点状态后线性扫一圈（f0=当前点不选, f1=当前点选）
        var count = 0L
        for (first in 0..1) {
            var f0 = if (first == 0) 1L else 0L
            var f1 = if (first == 1) 1L else 0L
            for (i in 1 until len) {
                val n0 = f0 + f1
                val n1 = f0                       // 当前点选 ⇒ 前一点必须不选
                f0 = n0; f1 = n1
            }
            count += if (first == 1) f0 else f0 + f1     // 首点选 ⇒ 末点必须不选
        }
        lens.add(len)
        ans *= count
    }
    println("cycle lengths = ${lens.sorted()}")
    println("answer = $ans")
}
