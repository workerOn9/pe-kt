/**
 * Project Euler 113 — Non-bouncy Numbers（非弹跳数）
 *
 * 思路：隔板法分别计算递增、递减数，再扣掉重复的常数位数。
 * 小于 10^D 的递增正整数有 C(D+9,9)-1 个；递减正整数有 C(D+10,10)-D-1 个；
 * 全相同非零数字组成的数重复计数 9D 次。合计 C(D+9,9)+C(D+10,10)-10D-2。
 * 组合数逐次乘除，每一步整除；本实现限定 D≤100，中间乘积也在 Long 范围内。
 * 复杂度：固定十进制下 O(1) 次 64 位乘除、O(1) 空间（两项共 19 次迭代）。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun countNonBouncy(digits: Int): Long {
    require(digits in 1..100)
    fun choose(n: Int, k: Int): Long {
        var value = 1L
        for (i in 1..k) value = value * (n - k + i) / i
        return value
    }
    return choose(digits + 9, 9) + choose(digits + 10, 10) - 10L * digits - 2L
}

fun solve(): Long {
    check(countNonBouncy(1) == 9L)
    check(countNonBouncy(6) == 12951L)
    check(countNonBouncy(10) == 277032L)
    return countNonBouncy(100)
}

fun main() { println(solve()) }
