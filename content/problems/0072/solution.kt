/**
 * Project Euler 072 — Counting Fractions
 *
 * 优化解：分母恰为 d 的既约真分数个数就是 Euler 函数 φ(d)（1 ≤ n < d 且 gcd(n,d)=1 的 n 的个数），
 * 于是答案为 Σ_{d=2}^{N} φ(d)（d=1 只给出分数 0/1，不在题面集合中）。
 * 用线性筛在 O(N) 内一次求出所有 φ：每个合数只被其最小素因子标记一次。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val n = 1_000_000
    val phi = IntArray(n + 1)
    val composite = BooleanArray(n + 1)
    val primes = IntArray(n / 10 + 64)
    var pc = 0
    phi[1] = 1
    for (i in 2..n) {
        if (!composite[i]) {
            primes[pc++] = i
            phi[i] = i - 1
        }
        var j = 0
        while (j < pc) {
            val p = primes[j]
            val ip = i.toLong() * p
            if (ip > n) break
            val m = ip.toInt()
            composite[m] = true
            if (i % p == 0) {
                phi[m] = phi[i] * p
                break
            }
            phi[m] = phi[i] * (p - 1)
            j++
        }
    }
    var total = 0L
    for (d in 2..n) total += phi[d]
    return total
}

fun main() {
    println(solve())
}
