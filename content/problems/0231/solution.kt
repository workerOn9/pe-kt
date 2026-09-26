#!/usr/bin/env kotlin
/**
 * Project Euler 231 — Prime Factorisation of Binomial Coefficients（二项式系数的素因数分解）
 *
 * 思路：绝不能真的算出 C(2e7, 1.5e7)（它有约 1.8e6 位十进制数）。
 *       只需要每个素数 p 在该二项式系数中的**指数**。由 Legendre 公式
 *
 *           v_p(n!) = Σ_{q = p, p², p³, ...} ⌊n / q⌋,
 *
 *       得指数 e_p = v_p(n!) - v_p(k!) - v_p((n-k)!) = Σ_{q} (⌊n/q⌋ - ⌊k/q⌋ - ⌊(n-k)/q⌋)。
 *       答案 = Σ_p p · e_p。
 *
 *       筛出 2e7 以内全部素数（ByteArray 埃氏筛，20 MB）后逐个累加 e_p。
 *       观察：p > k = 1.5e7 时只有 q = p 一项，且 ⌊n/p⌋ = 1、⌊k/p⌋ = 0、⌊(n-k)/p⌋ = 0，
 *       故 e_p = 1，这部分贡献 = 1.5e7 与 2e7 之间的素数之和（可单独核对）。
 *
 * 旁证：小规模逐个对照。C(10,3) = 120 的素因子和 = 14（题面示例）；
 *       C(20,10)、C(100,40)、C(1000,500)、C(5000,2500)、C(20000,15000)
 *       的「Legendre 公式」与「直接大整数分解」结果完全一致。
 * 答案：7526965179680
 *       （其中素数 p > 1.5e7 的贡献 5246155169981，p <= 1.5e7 的贡献 2280810009699）
 * 复杂度：O(N log log N) 筛 + O(π(N) log N) 累加，N = 2e7，实测约 70 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val N = 20_000_000
private const val K = 15_000_000

fun primeFactorSum(n: Int, k: Int): Pair<Long, Long> {
    val m = n - k
    val sieve = ByteArray(n + 1)
    var i = 2
    while (i * i <= n) {
        if (sieve[i].toInt() == 0) {
            var j = i * i
            while (j <= n) { sieve[j] = 1; j += i }
        }
        i++
    }
    var total = 0L
    var fromLarge = 0L
    var p = 2
    while (p <= n) {
        if (sieve[p].toInt() == 0) {
            var e = 0
            var q = p.toLong()
            while (q <= n) {
                e += (n / q).toInt() - (k / q).toInt() - (m / q).toInt()
                q *= p
            }
            if (e != 0) {
                total += p.toLong() * e
                if (p > k) fromLarge += p.toLong() * e
            }
        }
        p++
    }
    return total to fromLarge
}

/** 小规模旁证：Kummer（数字和）形式的 Legendre 公式——与跳幂商式是两套独立实现。 */
fun kummerFactorSum(n: Int, k: Int, sieve: ByteArray? = null): Long {
    fun digitSum(x: Long, base: Int): Long {
        var s = 0L
        var v = x
        while (v > 0) { s += v % base; v /= base }
        return s
    }
    val lim = n.toLong()
    val m = (n - k).toLong()
    var sum = 0L
    // 复用调用方的筛表可省去重筛；小规模直接重筛也便宜（注意 i++ 必须放在 if 外，否则死循环）
    val sv = sieve ?: run {
        val s = ByteArray(n + 1)
        var i = 2
        while (i * i <= n) {
            if (s[i].toInt() == 0) {
                var j = i * i
                while (j <= n) { s[j] = 1; j += i }
            }
            i++
        }
        s
    }
    var p = 2
    while (p <= n) {
        if (sv[p].toInt() == 0) {
            val e = (digitSum(k.toLong(), p) + digitSum(m, p) - digitSum(lim, p)) / (p - 1)
            if (e > 0) sum += p.toLong() * e
        }
        p++
    }
    return sum
}

/** 真正的小规模旁证：把 C(n,k) 的分子分母各自素因子分解后逐指数相减（完全不经除法链）。 */
fun trialFactorSum(n: Int, k: Int): Long {
    fun factor(x: Int, sign: Int, exps: HashMap<Int, Int>) {
        var v = x
        var d = 2
        while (d * d <= v) {
            while (v % d == 0) { exps[d] = (exps[d] ?: 0) + sign; v /= d }
            d++
        }
        if (v > 1) exps[v] = (exps[v] ?: 0) + sign
    }
    val exps = HashMap<Int, Int>()
    for (i in n - k + 1..n) factor(i, +1, exps)
    for (i in 1..k) factor(i, -1, exps)
    return exps.entries.sumOf { (p, e) -> if (e > 0) p.toLong() * e else 0L }
}

fun main() {
    println("== 旁证：小规模公式 vs 直接分解 ==")
    for ((a, b) in listOf(10 to 3, 20 to 10, 100 to 40, 1000 to 500, 5000 to 2500, 20000 to 15000)) {
        val f = primeFactorSum(a, b).first          // 商式 Legendre（跳幂）
        val g = kummerFactorSum(a, b)               // 数字和 Kummer 形式
        val h = trialFactorSum(a, b)                // 试除分解（严格小规模才可行）
        println("C($a,$b): legendre=$f kummer=$g trial=$h  ${if (f == g && g == h) "OK" else "MISMATCH"}")
    }
    println()
    val t0 = System.nanoTime()
    val (total, large) = primeFactorSum(N, K)
    val ms = (System.nanoTime() - t0) / 1_000_000
    println("C(2e7, 1.5e7): answer=$total  (p>1.5e7 part=$large, rest=${total - large})  ${ms} ms")
}
