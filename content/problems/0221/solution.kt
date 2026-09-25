/**
 * Project Euler 221 — Alexandrian Integers（亚历山大整数）
 *
 * 参数化：设 p = x > 0，q = -m，r = -n（m, n > x），恒等式 1/A = 1/p+1/q+1/r
 * 化为 (m - x)(n - x) = x^2 + 1。记 s = m - x、t = n - x（s·t = x^2 + 1，s < t），
 * 则 A = x(x+s)(x+t)，x >= 1。每个亚历山大整数唯一对应生成元 (x, s)，
 * 按 x 递增枚举 x^2+1 的小因子 s（s*f < x^2+1）即可按序不重不漏地生成全部解
 * （Python 精确有理数验证 + 官方样例 6/42/120/156/420/630 复现核实）。
 * 例：x=1, x^2+1=2, s=1: A = 1·2·3 = 6；x=2, x^2+1=5, s=1: 2·3·7 = 42。
 *
 * 加速 —— 只把 x^2+1 的素因子标出来：素数 q ≡ 1 (mod 4) 时 x^2 ≡ -1 (mod q)
 * 有两个根 ±r，把 [1, X] 中满足 x ≡ ±r (mod q) 的 x 全部打上「q | x^2+1」标记，
 * 逐 x 用除法提指数；去掉全部素因子后剩余部分若 > 1 必是素数
 * （两个 > X 的素因子相乘会超过 x^2+1），1 (mod 4) 之外的素数不可能整除 x^2+1。
 *
 * 完备性（不动点）：A >= 3x^3 + x（s+t >= 2·sqrt(x^2+1) >= 2x），扫描 X = 310000
 * 后 3X^3 ≈ 8.94e17 > 候选答案 1.884e15，故 [1, 150000] 内全部项已生成，无遗漏。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 素数筛，返回 0..limit 的素性布尔表。 */
private fun sieve(limit: Int): BooleanArray {
    val isP = BooleanArray(limit + 1) { true }
    isP[0] = false; isP[1] = false
    var i = 2
    while (i * i <= limit) {
        if (isP[i]) {
            var j = i * i
            while (j <= limit) { isP[j] = false; j += i }
        }
        i++
    }
    return isP
}

private tailrec fun powMod(base: Long, exp: Long, mod: Long): Long = when {
    exp == 0L -> 1L
    exp and 1L == 0L -> { val h = powMod(base, exp / 2, mod); h * h % mod }
    else -> base % mod * powMod(base, exp - 1, mod) % mod
}

/** 模 p 的 -1 平方根：取最小非剩余 a，w = a^((p-1)/4) 满足 w^2 ≡ -1（p ≡ 1 mod 4）。 */
private fun rootNeg1(p: Long): Long {
    var a = 2L
    while (powMod(a, (p - 1) / 2, p) != p - 1) a++
    val w = powMod(a, (p - 1) / 4, p)
    check(w * w % p == p - 1) { "sqrt(-1) check failed for p=$p" }
    return w
}

/** 小数字试除判素（剩余 cofactor <= x^2+1 <= 9.7e10，sqrt 不到 3.2e5，立即可判）。 */
private fun isPrimeSmall(n: Long): Boolean {
    if (n < 2L) return false
    var d = 2L
    while (d * d <= n) {
        if (n % d == 0L) return false
        d++
    }
    return true
}

/** 把素因子 (p, e) 的全部倍幂追加进因数清单。 */
private fun extendDivs(divs: MutableList<Long>, p: Long, e: Int) {
    val n0 = divs.size
    var pe = 1L
    repeat(e) {
        pe *= p
        for (k in 0 until n0) divs.add(divs[k] * pe)
    }
}

/** 求第 target 个亚历山大整数。 */
fun solve(target: Int = 150000): Long {
    val xHard = 310000
    val primeFlags = sieve(xHard)

    // 预先 rootNeg1 标出每个 q = 1 (mod 4) 素数（q <= xHard）对应的 x 集合：
    // 用「计数排序式」的映射表 x -> 素因子标记列表。
    val marks = Array(xHard + 1) { ArrayList<Long>() }
    var q = 5L
    while (q <= xHard) {
        if (primeFlags[q.toInt()] && q % 4 == 1L) {
            val r = rootNeg1(q)
            for (base in longArrayOf(r, q - r)) {
                var x = base % q
                if (x == 0L) x = q
                while (x <= xHard) {
                    marks[x.toInt()].add(q)
                    x += q
                }
            }
        }
        q++
    }

    val answers = ArrayList<Long>()
    answers.add(6L)   // x = 1 项（x^2+1 = 2，因子 s=1/t=2；下面循环从 x=2 开始）
    var x = 2L
    while (x <= xHard) {
        val xsq1 = x * x + 1
        val facs = LinkedHashMap<Long, Int>()
        var n = xsq1
        if (x % 2L == 1L) {
            facs[2L] = 1; n /= 2
        }
        for (prime in marks[x.toInt()]) {
            if (n % prime != 0L) continue
            var e = 0
            while (n % prime == 0L) { n /= prime; e++ }
            val old = facs[prime] ?: 0
            facs[prime] = old + e
        }
        if (n > 1L) {
            check(isPrimeSmall(n)) { "剩余部分不是素数：x=$x, n=$n" }
            facs[n] = 1
        }
        val divs = ArrayList<Long>()
        divs.add(1L)
        for ((prime, exp) in facs) extendDivs(divs, prime, exp)
        divs.sort()
        // 生成上限：第 150000 项约 1.884e15，第 150001 项略高于它；
        // 截断在 3e15 既排除 Long 溢出，又保证目标项之前全部生成。
        // 注意 A(s) 随 s 增大先减后不变（s < sqrt 时单调递减，最小值在 s=sqrt 附近），
        // 且 s=1 时 A ≈ x^4 巨大 —— 所以大 s 的一端只能「跳过」不能「break」，
        // 否则中段所有合法小 A 全部漏掉。
        val cap = 3_000_000_000_000_000L
        for (s in divs) {
            if (s * s >= xsq1) break
            val t = xsq1 / s
            // 先算安全的小部分，再超出前跳过（防 Long 溢出）：
            // m1 = x·(x+s) <= 3.1e5 · 9.1e10 < 2.9e16，安全；仅当
            // m1 <= cap/(x+t) 时乘积 a = m1·(x+t) 才可无溢出地计算。
            val m1 = x * (x + s)
            if (m1 > cap / (x + t)) continue
            val a = m1 * (x + t)   // 此处必然 <= cap
            answers.add(a)
        }
        x++
    }

    answers.sort()
    check(answers.size >= target) { "裕量不足：只生成 ${answers.size} 项" }
    // 完备性断言：3·X^3 必须超过答案（否则扫描尚未到不动点）
    val ans = answers[target - 1]
    check(3.0 * xHard * xHard * xHard > ans) { "扫描上限未达不动点：ans=$ans" }
    return ans
}

fun main() {
    println("answer #150000 = ${solve()}")
}
