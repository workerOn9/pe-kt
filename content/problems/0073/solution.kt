/**
 * Project Euler 073 — Counting Fractions in a Range
 *
 * 优化解：既约真分数要求 gcd(n,d)=1，逐个 gcd 判定太贵。改走筛法路线：
 *   1. 用最小素因子筛（SPF）得到每个 d 的全部不同素因子 p1..pk（d ≤ 12000 时 k ≤ 5）；
 *   2. 区间约束化成整数窗口 lo = ⌊d/3⌋+1, hi = ⌊(d−1)/2⌋（因为 n/d > 1/3 ⟺ 3n > d，
 *      n/d < 1/2 ⟺ 2n < d）；
 *   3. 窗口内与 d 互素的个数用容斥原理算：
 *      #{n ∈ [lo,hi] : gcd(n,d)=1} = Σ_{S ⊆ {p_i}} (−1)^{|S|} ⌊hi/∏S⌋ − ⌊(lo−1)/∏S⌋。
 * 每个 d 只需 O(2^k) ≤ 32 次算术运算，整体降到亚二次规模。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val n = 12000
    val spf = IntArray(n + 1)
    for (i in 2..n) {
        if (spf[i] == 0) {
            var j = i
            while (j <= n) {
                if (spf[j] == 0) spf[j] = i
                j += i
            }
        }
    }
    val factors = IntArray(8)
    var total = 0L
    for (d in 2..n) {
        var x = d
        var k = 0
        while (x > 1) {
            val p = spf[x]
            factors[k++] = p
            while (x % p == 0) x /= p
        }
        val lo = d / 3 + 1
        val hi = (d - 1) / 2
        if (lo > hi) continue
        var coprime = 0L
        for (mask in 0 until (1 shl k)) {
            var prod = 1L
            var bits = 0
            for (i in 0 until k) {
                if ((mask shr i) and 1 == 1) {
                    prod *= factors[i]
                    bits++
                }
            }
            val inWindow = hi / prod - (lo - 1) / prod
            coprime += if (bits and 1 == 0) inWindow else -inWindow
        }
        total += coprime
    }
    return total
}

fun main() {
    println(solve())
}
