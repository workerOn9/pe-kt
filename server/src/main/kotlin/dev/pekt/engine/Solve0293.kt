package dev.pekt.engine

import dev.pekt.math.modPow

/**
 * PE 293 — 伪幸运数（Pseudo-Fortunate Numbers）：N < 10^9 的不同 pseudo-Fortunate 数之和 = 2209。
 *
 * 推导（详见 content/problems/0293/solution.kt 头部与 0293/analysis.md）：
 *   N 偶数 ⇒ 2 必为其素因子 ⇒ 「不同素因子为连续素数」只能是从 2 开始的素数前缀。
 *   admissible 数统一为 DFS：2^a 打底，按 3,5,7,… 逐个决定是否引入（指数至少 1），
 *   乘积 ≥ 10^9 剪枝。前缀只到 23：
 *     2·3·5·7·11·13·17·19·23 = 223092870 < 10^9 < 223092870·29。
 *   N 偶 ⇒ N+2 偶合数 ⇒ M 从 3 起逐个奇数判定（N+M 为大于 N+1 的最小素数）；
 *   判定对象 < 10^9+131，底数 {2,3,5,7} 的确定性 Miller–Rabin 已足够。
 *   全部 M 去重求和。
 *
 * 复杂度：admissible 数 6656 个 × 期望 O(log N) 个候选 × MR 常数次模乘 ≈ 10 ms 量级。
 */
internal fun solve0293Impl(): Long {
    val limit = 1_000_000_000L
    val oddPrimes = longArrayOf(3, 5, 7, 11, 13, 17, 19, 23)

    fun isPrime(n: Long): Boolean {
        if (n < 2L) return false
        for (p in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
            if (n % p == 0L) return n == p
        }
        var d = n - 1
        var r = 0
        while (d and 1L == 0L) {
            d = d shr 1
            r++
        }
        for (a in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
            var x = modPow(a, d, n)
            if (x == 1L || x == n - 1L) continue
            var composite = true
            for (i in 1 until r) {
                x = x * x % n
                if (x == n - 1L) {
                    composite = false
                    break
                }
            }
            if (composite) return false
        }
        return true
    }

    fun minM(n: Long): Int {
        var m = 3
        while (!isPrime(n + m)) m += 2
        return m
    }

    val ms = HashSet<Int>()

    fun extend(idx: Int, cur: Long) {
        if (idx == oddPrimes.size) return
        val p = oddPrimes[idx]
        var v = cur
        while (v <= (limit - 1) / p) {
            v *= p
            ms.add(minM(v))
            extend(idx + 1, v)
        }
    }

    var two = 2L
    while (two < limit) {
        ms.add(minM(two))                       // 分支 ①：纯 2 的幂
        extend(0, two)                          // 分支 ②：素数前缀
        if (two > (limit - 1) / 2) break
        two *= 2
    }
    return ms.sumOf { it.toLong() }
}
