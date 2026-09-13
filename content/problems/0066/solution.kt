import java.math.BigInteger

/**
 * Project Euler 066 — Diophantine Equation
 *
 * 思路：x² − Dy² = 1 是 Pell 方程。D 为非完全平方数时，其最小正整数解就是 √D 的连分数
 * 展开 [a₀; (a₁, …, a_L)] 的某个渐近分数 pₙ/qₙ：这些渐近分数满足 pₙ² − Dqₙ² = ±1，
 * 取首个使右侧等于 +1 者即为最小 x。用 (m, d, a) 整数递推展开连分数，
 * 渐近分数用 BigInteger（D = 661 的 x 有 38 位，Long 装不下）。
 * 复杂度：O(D · L) 次大整数乘加，L 为周期长度（D ≤ 1000 时 L ≤ 2√D）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isqrt66(n: Int): Int {
    var r = Math.sqrt(n.toDouble()).toInt()
    while (r.toLong() * r > n) r--
    while ((r + 1).toLong() * (r + 1) <= n) r++
    return r
}

fun solve(): Long {
    var bestD = 0L
    var bestX = BigInteger.ZERO
    for (d in 2..1000) {
        val a0 = isqrt66(d)
        if (a0 * a0 == d) continue
        val db = BigInteger.valueOf(d.toLong())
        var m = 0
        var den = 1
        var a = a0
        var pPrev = BigInteger.ONE                    // p_{n-1}
        var p = BigInteger.valueOf(a0.toLong())       // p_0
        var qPrev = BigInteger.ZERO                   // q_{n-1}
        var q = BigInteger.ONE                        // q_0
        while (true) {
            if (p.multiply(p).subtract(db.multiply(q).multiply(q)) == BigInteger.ONE) break
            m = den * a - m
            den = (d - m * m) / den
            a = (a0 + m) / den
            val ab = BigInteger.valueOf(a.toLong())
            val pNext = p.multiply(ab).add(pPrev)
            val qNext = q.multiply(ab).add(qPrev)
            pPrev = p; p = pNext
            qPrev = q; q = qNext
        }
        if (p > bestX) {
            bestX = p
            bestD = d.toLong()
        }
    }
    return bestD
}

fun main() {
    println(solve())
}
