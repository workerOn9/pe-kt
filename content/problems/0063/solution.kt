/**
 * Project Euler 063 — Powerful Digit Counts
 *
 * 思路：a^n 恰有 n 位十进制数 ⟺ 10^(n-1) <= a^n < 10^n。
 * 右半边对 a <= 9 恒成立（9^n < 10^n），所以只需 a = 1..9 逐个判定左半边。
 * 比值 9^n / 10^(n-1) = 9·(9/10)^(n-1) 随 n 递减，一旦 9^n 位数不足 n 位，
 * 更大的 n 也不会再有解，于是从小到大枚举 n，判到 9^n < 10^(n-1) 即停。
 * 判定用 BigInteger 与 10 的幂做阈值比较，避免转换字符串。
 * 复杂度：O(n_max · 9) 次 BigInteger 幂运算，n_max = 22。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

fun solve(): Long {
    val ten = BigInteger.TEN
    val nine = BigInteger.valueOf(9L)
    var count = 0L
    var n = 1
    while (true) {
        val lo = ten.pow(n - 1)
        if (nine.pow(n).compareTo(lo) < 0) break
        val hi = ten.pow(n)
        for (a in 1..9) {
            val p = BigInteger.valueOf(a.toLong()).pow(n)
            if (p.compareTo(lo) >= 0 && p.compareTo(hi) < 0) count++
        }
        n++
    }
    return count
}

fun main() {
    println(solve())
}
