/**
 * Project Euler 069 — Totient Maximum
 *
 * 优化解：利用欧拉函数的素数分解公式
 *   $\dfrac{n}{\phi(n)} = \prod_{p \mid n} \dfrac{p}{p-1}$。
 * 每个因子 $p/(p-1)$ 随 $p$ 增大而单调减小（$1 + 1/(p-1)$），
 * 且多乘一个因子总让乘积变大，所以 $n/\phi(n)$ 的最大值在
 * 「从 2 开始、尽可能长的素数前缀积」处取得——即不断乘以最小素数，
 * 直到再乘一个就会超过 $10^6$。得到的乘积是 $2\cdot3\cdot5\cdot7\cdot11\cdot13\cdot17$。
 *
 * 复杂度：O(π) 次试除判素，实际只需检查 7 个素数，常数极小。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPrime(n: Int): Boolean {
    if (n < 2) return false
    var d = 2
    while (d.toLong() * d <= n) {
        if (n % d == 0) return false
        d++
    }
    return true
}

fun solve(): Long {
    var best = 1L
    var p = 2
    while (best * p <= 1_000_000L) {
        best *= p
        p++
        while (!isPrime(p)) p++
    }
    return best
}

fun main() {
    println(solve())
}
