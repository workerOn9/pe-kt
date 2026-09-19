/**
 * Project Euler 156 — Counting Digits（统计数字）
 *
 * 思路：f(n,d) 是 [0,n] 中数字 d 出现的总次数。求所有 f(n,d)=n 的解之和 s(d)，再求 Σs(d)。
 *
 * 数位计数公式：对每一位 t（权重 10^t），设 high=n/10^{t+1}, cur=(n/10^t)%10, low=n%10^t：
 *   - cur > d: 贡献 (high+1)·10^t
 *   - cur == d: 贡献 high·10^t + low + 1
 *   - cur < d:  贡献 high·10^t
 *
 * f(n,d) 单调不减。对每个 d ∈ {1..9}，用前缀递归分治找所有解：
 * 将区间 [lo, hi] 按 mid 二分，若两端 f(n,d)-n 同号且超出 ±maxDigits，则整段无解。
 * 上界 10^12 足够（已知 s(1)=22786974071）。
 */
fun solve(): Long {
    fun countDigit(n: Long, d: Int): Long {
        if (n <= 0L || d <= 0) return 0L
        var count = 0L
        var pow10 = 1L
        while (pow10 <= n) {
            val high = n / (pow10 * 10)
            val cur = (n / pow10) % 10
            val low = n % pow10
            count += high * pow10
            if (cur > d) count += pow10
            else if (cur == d) count += low + 1
            pow10 *= 10
        }
        return count
    }

    // 递归找 [lo, hi] 内所有 f(n,d)=n 的解
    fun find(d: Int, lo: Long, hi: Long): List<Long> {
        if (lo > hi) return emptyList()
        val fLo = countDigit(lo, d) - lo
        val fHi = countDigit(hi, d) - hi
        if (fLo > 12 && fHi > 12) return emptyList()
        if (fLo < -12 && fHi < -12) return emptyList()
        if (lo == hi) return if (countDigit(lo, d) == lo) listOf(lo) else emptyList()
        val mid = lo + (hi - lo) / 2
        return find(d, lo, mid) + find(d, mid + 1, hi)
    }

    val MAX = 1_000_000_000_000L  // 10^12
    var total = 0L
    for (d in 1..9) {
        val sols = find(d, 0L, MAX)
        total += sols.sumOf { it }
    }
    return total
}

fun main() {
    repeat(3) { solve() }
    val start = System.nanoTime()
    val ans = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(ans)
}