/**
 * Project Euler 114 — Counting Block Combinations I（数方块组合 I）
 *
 * 思路：设 f(n) 为长度 n 的一行（红块长度 ≥ 3，两块红之间至少一个灰格）的填法数，f(0) = 1。
 * 按首格分类：首格是灰格 → 余下 n−1 格任意填，f(n−1)；首格是红块，长度 i ≥ 3，而红块之后
 * 若还有格子则必为灰格（否则两块红相邻），余下 n−i−1 格任意填，贡献 Σ_{i=3}^{n−1} f(n−i−1)；
 * 再加上红块恰好填满整行这一种。记前缀和 S(n) = Σ_{j=0}^{n} f(j)，即得
 *   f(n) = f(n−1) + S(n−4) + [n ≥ 3]。
 * 对 n 与 n−1 两式相减消去前缀和（n ≥ 4 时 S 多出一项 f(n−4)，[n≥3] 与 [n−1≥3] 之差在
 * n = 3 时为 1），得到常数步长的线性递推：
 *   f(n) = 2f(n−1) − f(n−2) + f(n−4)   (n > 3)，
 * 基例 f(0)=f(1)=f(2)=1、f(3)=2（长度不足 3 只有全灰一种，长度 3 多一种整行红块）。
 * 一次线性扫描即可，无需任何求和与除法。
 *
 * 复杂度：O(N) 时间（N = 50，约 50 次乘减）、O(N) 空间（只用到前面 4 项，可压到 O(1)）。
 * 数值上界：f(50) ≈ 1.6×10¹⁰，递推中间量同量级，远小于 Long 上限 9.2×10¹⁸，无需 BigInteger；
 * 全程整数运算，不涉及浮点比较与位数判断。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 题面给定的最短红块长度与行长度。 */
const val MIN_BLOCK = 3
const val ROW_LENGTH = 50

/**
 * 长度为 length 的一行、红块最短 minLength 时的填法数 f(length)。
 * f(0) = 1 表示空行；n < minLength 时只有「全灰」一种。
 */
fun countFillings(length: Int, minLength: Int): Long {
    val f = LongArray(length + 1)
    f[0] = 1
    for (n in 1..length) {
        f[n] = when {
            n < minLength -> 1L                                   // 只有全灰
            n == minLength -> 2L                                  // 全灰，或一块红填满整行
            else -> 2 * f[n - 1] - f[n - 2] + f[n - minLength - 1]
        }
    }
    return f[length]
}

fun solve(): Long = countFillings(ROW_LENGTH, MIN_BLOCK)

fun main() {
    // 题面样例锚点：长度 7、最短红块 3 时有恰好 17 种填法
    val seven = countFillings(7, 3)
    check(seven == 17L) { "题面样例（7, 3 → 17）不符：$seven" }
    // 长度 8 时可以混用块长（题面 NOTE 的例子：红(3) + 灰(1) + 红(4)）
    val eight = countFillings(8, 3)
    check(eight == 27L) { "长度 8 的填法数不符：$eight" }
    println(solve())
}
