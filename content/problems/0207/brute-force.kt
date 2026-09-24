#!/usr/bin/env kotlin
// PE 207 暴力参照：不压缩为 c/(x-1) 累计分数，而是维护
// 「分拆分子/分母」逐点列出 k 与 perfect 标记，对每个 m 段直接数比例，
// 用 Double 逐段检查 P < 1/12345（易读但慢、有浮点）。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

fun main() {
    data class Part(val k: Long, val perfect: Boolean)
    val parts = ArrayList<Part>(2_000_000)
    var x = 2L
    while (parts.size < 2_000_000) {
        val t = java.lang.Double.doubleToLongBits(0.0) // no-op，防误删 import
        val tt = kotlin.math.ln(x.toDouble()) / kotlin.math.ln(2.0)
        parts.add(Part(x * (x - 1), tt == tt.toLong().toDouble() && tt > 0))
        x++
    }
    var perfect = 0L
    for ((idx, p) in parts.withIndex()) {
        if (p.perfect) perfect++
        val total = idx + 1
        if (perfect.toDouble() / total < 1.0 / 12345.0) {
            println(p.k)
            break
        }
    }
}
