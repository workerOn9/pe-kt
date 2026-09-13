/**
 * Project Euler 071 — Ordered Fractions（暴力对照）
 *
 * 暴力解：不使用 $3q \equiv 1 \pmod 7$ 这一数论捷径，而是逐个分母穷举：
 * 对每个 $d \le 10^6$，该分母下小于 $3/7$ 的最大分子是 $n_d = \lfloor (3d-1)/7 \rfloor$，
 * 于是所有候选值就是 $n_d/d$。用长整型交叉相乘比较候选大小（避免浮点误差），
 * 最后把最优分数约分，返回其分子。
 *
 * 复杂度：O(N) 次整数运算，N = 10^6。
 */

fun gcdLong(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

fun solveBruteForce(): Long {
    var bestN = 0L
    var bestD = 1L
    for (d in 2L..1_000_000L) {
        val n = (3 * d - 1) / 7
        if (n <= 0L) continue
        if (n * bestD > bestN * d) {
            bestN = n
            bestD = d
        }
    }
    return bestN / gcdLong(bestN, bestD)
}

fun main() {
    println(solveBruteForce())
}
