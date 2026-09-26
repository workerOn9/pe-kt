#!/usr/bin/env kotlin
/**
 * Project Euler 230 — Fibonacci Words · 暴力对照版
 *
 * 思路：完全照题面定义显式构造整个词 —— 题面说 D_{A,B}(n) 是「F 中第一个位数 ≥ n 的项」
 *       的第 n 位，所以暴力就是：按 T_k = T_{k-2} + T_{k-1} 一路拼到长度 ≥ target，
 *       然后直接按下标取字符。用来对拍 O(log n) 的自顶向下定位解法。
 *       完整项长 1.3×10^17 显然建不出来，但拼到 1e6 位足以校验下标定位与拼接顺序。
 *
 *       注意拼接顺序（本题唯一的坑）：题面 (A, B, AB, BAB, ABBAB, …) ⇒ T_k = T_{k-2} + T_{k-1}。
 *       写成 T_{k-1} + T_{k-2} 会让题面示例 D(35) 变成 8 而不是 9。
 *
 * 复杂度：构造 O(target) 位串，查询 O(1)。
 * 构建：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 * 运行：java -jar bf.jar
 */

private val A = (
    "14159265358979323846264338327950288419716939937510" +
    "58209749445923078164062862089986280348253421170679"
    )
private val B = (
    "82148086513282306647093844609550582231725359408128" +
    "48111745028410270193852110555964462294895493038196"
    )

private fun Long.pow(n: Int): Long { var r = 1L; repeat(n) { r *= this }; return r }

/** 显式拼出 F 中第一个长度 ≥ target 的项（T_k = T_{k-2} + T_{k-1}）。 */
fun buildWord(target: Int): String {
    var t0 = A
    var t1 = B
    while (t1.length < target) {
        val t = t0 + t1
        t0 = t1
        t1 = t
    }
    return t1
}

/** 小例子用：显式列出 T_0 … T_k。 */
fun termOf(a: String, b: String, k: Int): String {
    val t = mutableListOf(a, b)
    while (t.size <= k) t.add(t[t.size - 2] + t[t.size - 1])
    return t[k]
}

/** 小例子用：拼出第一个长度 ≥ target 的项。 */
fun buildWordSmall(a: String, b: String, target: Int): String {
    var t0 = a
    var t1 = b
    while (t1.length < target) { val t = t0 + t1; t0 = t1; t1 = t }
    return t1
}

fun main() {
    // 题面示例：A = 1415926535, B = 8979323846，第一个 ≥ 35 位的项应为 50 位的 T_4，第 35 位 = 9
    val a0 = "1415926535"
    val b0 = "8979323846"
    val w0 = buildWordSmall(a0, b0, 35)
    println("example terms:")
    for (i in 0 until 5) println("  T$i = ${termOf(a0, b0, i)}")
    val exDig = w0[34]
    println("  first term with >=35 digits has length ${w0.length}, its 35th digit = $exDig "
        + "(题面示例为 9) ${if (exDig == '9') "OK" else "MISMATCH"}")

    // 真词：拼到 1e6 位，逐位对拍
    val target = 1_000_000
    val t0 = System.nanoTime()
    val word = buildWord(target)
    val buildMs = (System.nanoTime() - t0) / 1_000_000
    println()
    println("built explicit term of ${word.length} digits in $buildMs ms")

    println("positions 7^n*(127+19n) that fit inside the explicit term:")
    var n = 0
    while (true) {
        val p = (127 + 19 * n) * 7L.pow(n)
        if (p > word.length) break
        println("  n=$n pos=$p digit=${word[(p - 1).toInt()]}")
        n++
    }
    println("（n ≥ 5 的位置超出 1e6 位，暴力法覆盖不到，必须用 O(log n) 的自顶向下定位）")
    val big = (127 + 19 * 17) * 7L.pow(17)
    println("max queried index = $big  vs explicit term length ${word.length}")
}
