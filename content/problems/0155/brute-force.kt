/**
 * Project Euler 155 — Counting Capacitor Circuits（计算电容器电路）· 暴力对照解
 *
 * 思路：与 solution.kt 相同的 Fraction DP，但逐状态打印所有电容值用于验证小规模。
 */

fun solveBruteForce(n: Int = 10): Long {
    val dp = Array(n + 1) { mutableSetOf<FractionBrute>() }
    dp[1].add(FractionBrute(1, 1))
    val all = mutableSetOf<FractionBrute>()
    all += dp[1]
    
    for (k in 2..n) {
        for (j in 1..k / 2) {
            val other = k - j
            for (a in dp[j]) {
                for (b in dp[other]) {
                    dp[k].add(a.parallel(b))
                    dp[k].add(a.series(b))
                }
            }
        }
        all += dp[k]
    }
    return all.size.toLong()
}

data class FractionBrute(val num: Long, val den: Long) {
    private fun g(): Long {
        var x = kotlin.math.abs(num), y = kotlin.math.abs(den)
        while (y > 0) {
            val r = x % y; x = y; y = r
        }
        return x
    }
    private fun s() = FractionBrute(num / g(), den / g())
    fun parallel(o: FractionBrute) = FractionBrute(num * o.den + o.num * den, den * o.den).s()
    fun series(o: FractionBrute) = FractionBrute(num * o.num, num * o.den + o.num * den).s()
    override fun toString() = "$num/$den"
    override fun equals(other: Any?) = other is FractionBrute && num == other.num && den == other.den
    override fun hashCode() = (num * 31 + den).toInt()
}

fun main() {
    val start = System.nanoTime()
    val answer = solveBruteForce(10)
    val elapsed = (System.nanoTime() - start).toDouble() / 1e6
    System.err.printf("brute: %.4f ms%n", elapsed)
    println("D(10) = $answer")
}