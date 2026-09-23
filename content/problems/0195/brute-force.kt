package dev.pekt.problems

/**
 * PE 195 暴力解：枚举所有可能的三角形边长，检查是否满足条件。
 * 仅对小参数可行。
 */
fun bruteForce195(n: Long = 100L): Long {
    var count = 0L
    val limit = (n * 4.0).toInt() // 宽松上界
    for (a in 1..limit) {
        for (b in a..limit) {
            val cSq = a * a + b * b - a * b
            val c = Math.sqrt(cSq.toDouble()).toInt()
            if (c * c != cSq) continue
            // 验证三角形不等式
            if (a + b <= c || a + c <= b || b + c <= a) continue
            // 恰好一个角为 60°（排除等边三角形）
            val hasAngleA60 = (b * b + c * c - a * a) == b * c
            val hasAngleB60 = (a * a + c * c - b * b) == a * c
            val num60 = 1 + (if (hasAngleA60) 1 else 0) + (if (hasAngleB60) 1 else 0)
            if (num60 != 1) continue
            // 计算内切圆半径 r
            val s = (a + b + c) / 2.0
            val areaSq = s * (s - a) * (s - b) * (s - c)
            if (areaSq <= 0) continue
            val r = Math.sqrt(areaSq) / s
            if (r <= n) count++
        }
    }
    return count
}

fun main() {
    println("T(100) = ${bruteForce195(100)} (expected 1234)")
    println("T(1000) = ${bruteForce195(1000)} (expected 22767)")
}
