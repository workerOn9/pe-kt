#!/usr/bin/env kotlin
/**
 * Project Euler 230 — Fibonacci Words（斐波那契词）
 *
 * 思路：第 k 项长度按斐波那契增长，L[0]=L[1]=100，L[k]=L[k-1]+L[k-2]。
 *       要访问的下标最大 (127+19*17)*7^17 = 1.0468e17，只能 O(log) 定位：
 *       找最小的 k 使 L[k] ≥ n（斐波那契性质保证二分/线性找都只有 74 步），
 *       然后**自顶向下递降**：T_k = T_{k-2} + T_{k-1}（注意顺序：第 3 项是 AB = T_1+T_2，
 *       与题面例子一致），若 n ≤ L[k-2] 降到 k-2，否则 n -= L[k-2] 并降到 k-1。
 *       最终落到 T_0 = A 或 T_1 = B，直接取第 n 个字符。
 *
 *       拼接顺序是本题的坑：题面写的是 (A, B, AB, BAB, ABBAB, …)，即 T_k = T_{k-2}+T_{k-1}。
 *       写成 T_{k-1}+T_{k-2} 会得到 D(35) = 8 而非题面示例的 9。
 *
 * 旁证：用 10 位小例子 A=1415926535, B=8979323846 复现题面示例 D(35) = 9；
 *       再用显式拼出 7.5×10^6 长的字符串逐位对拍 11 个下标，全部一致。
 * 答案：Σ 10^n · D((127+19n)·7^n) = 850481152593119296
 *       （各位从低到高：6,9,2,9,1,1,3,9,5,2,5,1,1,8,4,0,5,8）
 * 复杂度：每次查询 O(log n_max) ≈ 74 步，18 次查询共约 1300 步，< 1 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private val A = (
    "14159265358979323846264338327950288419716939937510" +
    "58209749445923078164062862089986280348253421170679"
    )

private val B = (
    "82148086513282306647093844609550582231725359408128" +
    "48111745028410270193852110555964462294895493038196"
    )

private fun lengths(maxIndex: Long): LongArray {
    val l = ArrayList<Long>()
    l.add(A.length.toLong())
    l.add(B.length.toLong())
    while (l[l.size - 1] < maxIndex) l.add(l[l.size - 1] + l[l.size - 2])
    return l.toLongArray()
}

/** D(n)：第 n 个数字（1-based）。l 需覆盖 maxIndex。 */
fun digitAt(n: Long, l: LongArray): Char {
    var k = 0
    while (l[k] < n) k++
    var pos = n
    while (k > 1) {
        if (pos <= l[k - 2]) {
            k -= 2
        } else {
            pos -= l[k - 2]
            k -= 1
        }
    }
    return if (k == 0) A[(pos - 1).toInt()] else B[(pos - 1).toInt()]
}

fun main() {
    // 题面示例自检
    val a0 = "1415926535"
    val b0 = "8979323846"
    val ex = ArrayList<Long>()
    ex.add(10L)
    ex.add(10L)
    while (ex[ex.size - 1] < 40) ex.add(ex[ex.size - 1] + ex[ex.size - 2])
    var k = 0
    while (ex[k] < 35) k++
    var pos = 35L
    var kk = k
    while (kk > 1) {
        if (pos <= ex[kk - 2]) kk -= 2 else { pos -= ex[kk - 2]; kk -= 1 }
    }
    val ch = if (kk == 0) a0[(pos - 1).toInt()] else b0[(pos - 1).toInt()]
    println("example D(35) = $ch (statement says 9)  ${if (ch == '9') "OK" else "MISMATCH"}")

    // 显式对拍：用真实的 100 位 A / B 展开最后一项 T_k
    var t0 = A
    var t1 = B
    while (t1.length < 5_000_000) {
        val t = t0 + t1   // T_k = T_{k-2} + T_{k-1}
        t0 = t1
        t1 = t
    }
    val big = t1
    val l = lengths((127 + 19 * 17) * 7L.pow(17))
    val bad = listOf(1L, 2L, 35L, 100L, 101L, 200L, 999L, 5000L, 25_000L, 100_000L, 1_000_000L)
        .filter { it <= big.length }
        .filter { digitAt(it, l) != big[(it - 1).toInt()] }
    println("cross-check bad indices: $bad  (explicit string length ${big.length})")

    var total = 0L
    val digits = StringBuilder()
    for (n in 0..17) {
        val p = (127 + 19 * n) * 7L.pow(n)
        val d = digitAt(p, l) - '0'
        digits.append(d)
        total += 10L.pow(n) * d
    }
    println("digits low->high: $digits")
    println("L[-1] = ${l[l.size - 1]}, steps = ${l.size}")
    println("answer = $total")
}

/** Long 的非负整数幂（n ≤ 17，结果 < 2^63）。 */
private fun Long.pow(n: Int): Long {
    var r = 1L
    repeat(n) { r *= this }
    return r
}
