/**
 * Project Euler 115 — Counting Block Combinations II（数方块组合 II）
 *
 * 思路：F(m,n) 是「长度 n 的一行，红块长度 ≥ m，两块红之间至少一个黑格」的填法数，
 * 与 114 是同一个计数问题，只是把最短块长做成参数，于是推导整套复用：
 * 按首格分类（黑 → F(n−1)；红 → 块长 i ≥ m 且其后一格必为黑，余 n−i−1 格任意），记
 * S(n) = Σ_{j≤n} F(j)，得 F(n) = F(n−1) + S(n−m−1) + [n ≥ m]；两式相减消去前缀和，
 * 化为常数步长的线性递推
 *   F(n) = 2F(n−1) − F(n−2) + F(n−m−1)   (n > m)，
 * 基例 F(0) = F(1) = … = F(m−1) = 1（只有全黑）、F(m) = 2（多一种整行红块）。
 * 求「最小的 n 使 F(m,n) > 10⁶」只需沿 n 递增单向扫一遍，第一次越过阈值的那个 n 就是答案
 * ——不必二分，也不必假定 F 的单调性：扫描顺序本身就保证了「最小」。
 * 表长上界未知，用容量翻倍的外层循环兜住（F 指数增长，实际一次就够）。
 *
 * 复杂度：O(N) 时间（N = 168，每格常数次 64 位乘减）、O(N) 空间。
 * 数值上界：F(50,168) ≈ 1.05×10⁶，最大中间量同量级，远小于 Long 上限 9.2×10¹⁸；
 * 全程整数运算，无浮点比较、无位数判断。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 题面给定的最短红块长度与阈值。 */
const val MIN_LENGTH = 50
const val THRESHOLD = 1_000_000L

/**
 * 使 F(m, n) > threshold 的最小 n，用 114 导出的线性递推沿 n 单向扫描。
 * 递推表按容量翻倍增长，直到扫出越过阈值的那一项。
 */
fun leastRowLength(minLength: Int, threshold: Long): Int {
    require(minLength >= 1) { "最短块长必须为正" }
    var capacity = 2 * minLength + 8
    while (true) {
        val f = LongArray(capacity)
        f[0] = 1                                        // 空行记一种
        for (n in 1 until capacity) {
            f[n] = when {
                n < minLength -> 1L                     // 放不下任何红块，只有全黑
                n == minLength -> 2L                    // 全黑，或一块红填满整行
                else -> 2 * f[n - 1] - f[n - 2] + f[n - minLength - 1]
            }
            if (f[n] > threshold) return n              // 递增扫描，首个越阈值即最小 n
        }
        capacity *= 2
    }
}

/** F(m, n)：长度 n 的行的填法数（用于复现题面给出的四组锚点值）。 */
fun fillCount(rowLength: Int, minLength: Int): Long {
    val f = LongArray(rowLength + 1)
    f[0] = 1
    for (n in 1..rowLength) {
        f[n] = when {
            n < minLength -> 1L
            n == minLength -> 2L
            else -> 2 * f[n - 1] - f[n - 2] + f[n - minLength - 1]
        }
    }
    return f[rowLength]
}

fun solve(): Long = leastRowLength(MIN_LENGTH, THRESHOLD).toLong()

fun main() {
    // 题面锚点：m = 3 时 F(3,29) = 673135、F(3,30) = 1089155，首个超百万的 n 是 30
    check(fillCount(29, 3) == 673_135L && fillCount(30, 3) == 1_089_155L) {
        "m = 3 锚点不符：${fillCount(29, 3)} / ${fillCount(30, 3)}"
    }
    check(leastRowLength(3, THRESHOLD) == 30) { "m = 3 的答案应为 30" }
    // 题面锚点：m = 10 时 F(10,56) = 880711、F(10,57) = 1148904，答案是 57
    check(fillCount(56, 10) == 880_711L && fillCount(57, 10) == 1_148_904L) {
        "m = 10 锚点不符：${fillCount(56, 10)} / ${fillCount(57, 10)}"
    }
    check(leastRowLength(10, THRESHOLD) == 57) { "m = 10 的答案应为 57" }
    // 114 的样例也落在同一张表上：F(3,7) = 17
    check(fillCount(7, 3) == 17L) { "114 锚点不符：F(3,7) = ${fillCount(7, 3)}" }
    println(solve())
}
