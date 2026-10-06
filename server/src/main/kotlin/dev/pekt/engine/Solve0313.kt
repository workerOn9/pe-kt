package dev.pekt.engine

/**
 * PE 313 — Sliding Game（滑块游戏）
 *
 * 步数闭式：S(n,n) = 8n − 11，S(m,n) = 6n + 2m − 13（m < n）。
 * 统计 S(m,n) = p²（p < 10⁶ 为素数）的网格总数。
 * 最终答案：2057774861813004。
 */
internal fun solve0313Impl(): Long {
    val limit = 999_999
    val sieve = BooleanArray(limit + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (sieve[i]) {
            var j = i.toLong() * i
            while (j <= limit) { sieve[j.toInt()] = false; j += i }
        }
        i++
    }

    var total = 0L
    for (p in 2..limit) {
        if (!sieve[p]) continue
        val target = p.toLong() * p
        if ((target + 11) % 8L == 0L) {
            val m = (target + 11) / 8
            if (m >= 2) total++
        }
        val k = (target + 13) / 2
        val lo = k / 4 + 1
        val hi = (k - 2) / 3
        if (hi >= lo) total += 2 * (hi - lo + 1)
    }
    return total
}
