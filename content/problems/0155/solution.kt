/**
 * Project Euler 155 — Counting Capacitor Circuits（计算电容器电路）
 *
 * 思路：关键观察是——对任意两个电容集合 A、B（A 含 m 个电容，B 含 n-m 个），
 * 并联得到 C_A + C_B，串联得到 1/(1/C_A + 1/C_B) = (C_A·C_B)/(C_A+C_B)。
 *
 * 状态：用 Fraction 表示电容值。对每个容量 k（1..n），枚举分割点 j=1..k/2，
 * 用前 j 个电容得到的所有值与用后 k-j 个电容得到的所有值组合。
 *
 * 核心技巧：用分母分子表示有理数避免浮点误差。设 base = 1 μF = 1（归一化单位）。
 * 电容值为正有理数 p/q。
 * - 并联：p1/q1 + p2/q2 = (p1q2 + p2q1)/(q1q2)
 * - 串联：(p1/q1 · p2/q2) / (p1/q1 + p2/q2) = (p1p2)/(q1q2) / ((p1q2+q1p2)/q1q2) = (p1p2)/(p1q2+q1p2)
 *
 * 动态规划：
 *   dp[k] = set of all capacitances achievable with exactly k capacitors
 *   for k from 1 to n:
 *     for j from 1 to k/2:
 *       combine dp[j] and dp[k-j] via parallel and series
 *
 * 答案 = |dp[1] ∪ dp[2] ∪ ... ∪ dp[18]|
 */

data class Fraction(val num: Long, val den: Long) : Comparable<Fraction> {
    constructor(x: Int) : this(x.toLong(), 1)
    
    init {
        require(den > 0) { "denominator must be positive" }
        if (num == 0L) {
            return // Zero normalized
        }
        val g = gcd(num, den)
    }
    
    private fun gcd(a: Long, b: Long): Long {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y > 0) {
            val r = x % y
            x = y
            y = r
        }
        return x
    }
    
    private fun simplify(): Fraction {
        val g = kotlin.math.max(gcd(num, den), 1)
        return Fraction(num / g, den / g)
    }
    
    fun plus(other: Fraction): Fraction {
        return Fraction(num * other.den + other.num * den, den * other.den).simplify()
    }
    
    fun times(other: Fraction): Fraction {
        return Fraction(num * other.num, den * other.den).simplify()
    }
    
    override fun toString(): String = "$num/$den"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Fraction) return false
        return num == other.num && den == other.den
    }
    override fun hashCode(): Int = num.hashCode() * 31 + den.hashCode()
}

fun solve(): Long {
    val n = 18
    
    // dp[k] = all capacitances achievable with exactly k capacitors
    val dp = Array(n + 1) { mutableSetOf<Fraction>() }
    dp[1].add(Fraction(1)) // Base unit = 1μF normalized
    
    // Precompute union sets for partial answers
    val allCapacitances = mutableSetOf<Fraction>()
    allCapacitances += dp[1]
    
    for (k in 2..n) {
        for (j in 1..k / 2) {
            val other = k - j
            val left = dp[j]
            val right = dp[other]
            
            // Parallel and series combinations
            for (a in left) {
                for (b in right) {
                    dp[k].add(a + b)      // Parallel
                    dp[k].add(series(a, b)) // Series
                }
            }
            
            // Also include single capacitor combinations (same capacity split)
            if (j == other) {
                // We already paired all combinations above
            }
        }
        
        // Union into global set
        allCapacitances += dp[k]
    }
    
    return allCapacitances.size.toLong()
}

private fun series(a: Fraction, b: Fraction): Fraction {
    return Fraction(a.num * b.num, a.num * b.den + b.num * a.den).simplify()
}

fun main() {
    repeat(3) { solve() }  // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}