/**
 * Project Euler 070 — Totient Permutation（暴力对照）
 *
 * 暴力解：不做任何「最优解是半素数」的假设。
 * 用筛法一次性求出 1..10^7 的全部 $\phi(n)$（对每个素数 $p$ 更新其所有倍数），
 * 然后对每个 $n$ 检查 $\phi(n)$ 是否为 $n$ 的数字排列，并交叉相乘比较 $n/\phi(n)$。
 *
 * 复杂度：O(N log log N)，N = 10^7；空间 O(N)。
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

fun solveBruteForce(): Long {
    val limit = 10_000_000
    val phi = IntArray(limit) { it }
    for (p in 2 until limit) {
        if (phi[p] == p) {
            var m = p
            while (m < limit) {
                phi[m] -= phi[m] / p
                m += p
            }
        }
    }
    var bestN = 0L
    var bestPhi = 0L
    for (n in 2 until limit) {
        val pn = phi[n].toLong()
        if (!sameDigits(n.toLong(), pn)) continue
        if (bestN == 0L || n.toLong() * bestPhi < bestN * pn) {
            bestN = n.toLong()
            bestPhi = pn
        }
    }
    return bestN
}

fun main() {
    println(solveBruteForce())
}
