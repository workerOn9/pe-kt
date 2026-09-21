/**
 * PE 171 — 数位平方和为完全平方数，n < 10^20，求和的后九位。
 * 数位 DP：按位推进，状态为「数位平方和 s」，同时维护方案数 c[s] 与这些数的和 v[s]：
 *   新值 = 10*旧值 + d  =>  c'[s+d^2] += c[s];  v'[s+d^2] += 10*v[s] + d*c[s]
 * 用 20 位（含前导零）的唯一表示覆盖 1..10^20-1；最后对 s 为完全平方数的桶求和（s=0 对应 n=0，排除）。
 * 已由 Python 实算 = 142989277。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
const val MOD = 1_000_000_000L
const val DIGITS = 20
const val MAX_S = 81 * DIGITS

fun solve(): Long {
    var cnt = LongArray(MAX_S + 1)
    var sum = LongArray(MAX_S + 1)
    cnt[0] = 1L
    repeat(DIGITS) {
        val nc = LongArray(MAX_S + 1)
        val ns = LongArray(MAX_S + 1)
        for (s in 0..MAX_S) {
            val c = cnt[s]
            val v = sum[s]
            if (c == 0L && v == 0L) continue
            for (d in 0..9) {
                val t = s + d * d
                nc[t] = (nc[t] + c) % MOD
                ns[t] = (ns[t] + 10 * v + d * c) % MOD
            }
        }
        cnt = nc
        sum = ns
    }
    var total = 0L
    var k = 1
    while (k * k <= MAX_S) {
        total = (total + sum[k * k]) % MOD
        k++
    }
    return total
}

fun main() {
    println(solve())
}
