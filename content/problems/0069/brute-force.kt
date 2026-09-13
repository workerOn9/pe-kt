/**
 * Project Euler 069 — Totient Maximum（暴力对照）
 *
 * 暴力解：先用筛法一次性求出 1..10^6 的全部 $\phi(n)$——
 * 对每个素数 $p$，把所有 $p$ 的倍数 $m$ 更新为 $\phi(m) \leftarrow \phi(m) - \phi(m)/p$，
 * 初始值为 $\phi(m) = m$，这样每个素因子恰好被除掉一次。
 * 然后线性扫描所有 $n$，用长整型交叉相乘（$n \cdot \phi(best) > best \cdot \phi(n)$）
 * 比较 $n/\phi(n)$ 的大小，避免浮点误差。
 *
 * 复杂度：O(N log log N)，N = 10^6；空间 O(N)。
 */

fun solveBruteForce(): Long {
    val n = 1_000_000
    val phi = IntArray(n + 1) { it }
    for (p in 2..n) {
        if (phi[p] == p) { // p 是素数
            var m = p
            while (m <= n) {
                phi[m] -= phi[m] / p
                m += p
            }
        }
    }
    var best = 1
    for (i in 2..n) {
        if (i.toLong() * phi[best] > best.toLong() * phi[i]) best = i
    }
    return best.toLong()
}

fun main() {
    println(solveBruteForce())
}
