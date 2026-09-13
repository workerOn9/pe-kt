/**
 * Project Euler 100 — 暴力解（教学对比用）
 *
 * 解法与 solution.kt 同属佩尔方程一族，但走的是「按定义穷举」的路线：
 * 直接展开 √2 = [1; 2,2,2,…] 的连分数，逐个生成渐近分数 p/q，
 * 用 BigInteger 逐个检验 x²-2y² = -1（解恰为偶数下标的渐近分数），
 * 再换算回 n = (x+1)/2、b = (y+1)/2，直到 n > 10^12。
 *
 * 之所以不用「枚举 n 或 b 再验根」的字面暴力：答案是 n ≈ 1.07×10^12，
 * 逐个数检查需要 10^12 次循环，本机不可能在合理时间内跑完；
 * 连分数枚举是同一问题在不牺牲可行性的前提下最朴素的算法。
 * 代价是每步都要做多精度乘法和平方判定，比 solution.kt 的 Long 递推慢两个数量级。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    val two = BigInteger.valueOf(2)
    val minusOne = BigInteger.valueOf(-1)
    val limit = BigInteger.valueOf(1_000_000_000_000L)
    var p2 = BigInteger.ZERO
    var p1 = BigInteger.ONE
    var q2 = BigInteger.ONE
    var q1 = BigInteger.ZERO
    var k = 0
    while (true) {
        val a = if (k == 0) BigInteger.ONE else two
        val p = a.multiply(p1).add(p2)
        val q = a.multiply(q1).add(q2)
        if (p.multiply(p).subtract(two.multiply(q).multiply(q)) == minusOne) {
            val n = p.add(BigInteger.ONE).shiftRight(1)
            if (n > limit) return q.add(BigInteger.ONE).shiftRight(1).toLong()
        }
        p2 = p1; p1 = p
        q2 = q1; q1 = q
        k++
    }
}

fun main() {
    println(solveBruteForce())
}
