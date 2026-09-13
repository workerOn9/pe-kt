/**
 * Project Euler 070 — Totient Permutation
 *
 * 优化解：由 $\dfrac{n}{\phi(n)} = \prod_{p\mid n}\dfrac{p}{p-1}$，
 * 比值要小就得让 $n$ 的**不同素因子尽量少**：
 *   若 $n$ 含三个不同素因子 $p<q<r$，则 $p^3 < n < 10^7 \Rightarrow p \le 211$，
 *   于是 $n/\phi(n) \ge p/(p-1) \ge 211/210 \approx 1.004762$，
 *   而两个素因子的候选能把比值压到 $1.001$ 以下（见下），三素因子情形必然出局；
 *   单个素因子 $n=p^a$ 是 $1 \Rightarrow$ 唯一的例外情形，单独扫描。
 * 主搜索：枚举素数对 $n = pq$（$p<q$），此时 $\phi(n) = (p-1)(q-1)$；
 * 筛出 $10^7/2$ 以内的素数表（$p \ge 2 \Rightarrow q < 10^7/2$），
 * 对每个 $p < \sqrt{10^7}$ 向后取 $q$，一旦 $pq \ge 10^7$ 即截断；
 * 用数位计数判断 $n$ 与 $\phi(n)$ 是否互为排列，交叉相乘比较 $n/\phi(n)$ 取最小。
 *
 * 复杂度：筛 $O(N\log\log N)$（$N = 5\times10^6$），配对约 $2\times10^6$ 组。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun sameDigits(a: Long, b: Long): Boolean {
    val cnt = IntArray(10)
    var x = a
    while (x > 0L) {
        cnt[(x % 10L).toInt()]++
        x /= 10L
    }
    var y = b
    while (y > 0L) {
        cnt[(y % 10L).toInt()]--
        y /= 10L
    }
    for (c in cnt) if (c != 0) return false
    return true
}

fun solve(): Long {
    val limit = 10_000_000
    val half = limit / 2
    val isPrime = BooleanArray(half + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= half) {
        if (isPrime[i]) {
            var j = i * i
            while (j <= half) {
                isPrime[j] = false
                j += i
            }
        }
        i++
    }
    val primes = ArrayList<Int>()
    for (k in 2..half) if (isPrime[k]) primes.add(k)

    var bestN = 0L
    var bestPhi = 0L

    // 两个素因子：n = p·q
    for (ai in primes.indices) {
        val p = primes[ai]
        if (p.toLong() * p >= limit) break
        for (bi in ai + 1 until primes.size) {
            val q = primes[bi]
            val n = p.toLong() * q
            if (n >= limit) break
            val phi = (p - 1).toLong() * (q - 1)
            if (!sameDigits(n, phi)) continue
            if (bestN == 0L || n * bestPhi < bestN * phi) {
                bestN = n
                bestPhi = phi
            }
        }
    }

    // 单个素因子：n = p^a（a ≥ 2；a = 1 时 n 与 φ(n)=n-1 数位和相差 1，
    // 不可能互为排列）。p ≥ 3163 时 p^2 ≥ 10^7，故只需枚举到 sqrt。
    for (p in primes) {
        if (p.toLong() * p >= limit) break
        var n = p.toLong() * p
        while (n < limit) {
            val phi = n / p * (p - 1)
            if (sameDigits(n, phi) && (bestN == 0L || n * bestPhi < bestN * phi)) {
                bestN = n
                bestPhi = phi
            }
            n *= p
        }
    }
    return bestN
}

fun main() {
    println(solve())
}
