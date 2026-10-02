package dev.pekt.engine

import dev.pekt.math.sieve
import kotlin.math.sqrt

/**
 * PE 302 — Strong Achilles Numbers（强阿基里斯数）：S 是强大数（素因子指数全 ≥ 2）
 * 且非完全幂（指数 gcd = 1）为阿基里斯数；S 与 φ(S) 都是阿基里斯数为强阿基里斯数。
 * 求 S ≤ 10^18 的个数。
 *
 * 推导（详见 content/problems/0302/solution.kt 头部与 0302/analysis.md）：
 *   · 阿基里斯数必有指数 ≥ 3 ⇒ 最大素因子 p 满足 p³ | S ≤ 10^18 ⇒ p ≤ 10^6；
 *   · φ(S) = ∏ p^{e−1}(p−1)，指数表 = 自身 (e−1) + (p−1) 分解；
 *   · DFS 按素数表构造候选（指数从 2 起），剪枝：phiExp[q] < 2 且
 *     phiExp[q] + extraCap[q] < 2（extraCap 为未来 (p′−1) 的乐观总贡献）则剪；
 *   · 计数条件：cur > 1 && 指数 gcd = 1 && φ 指数全 ≥ 2 && φ 指数 gcd = 1；
 *   · 撤销用快照水位；一切乘积判断用除法防 Long 溢出。
 * 答案 = 1170060（全量本机实跑，与题面样例 1e4→7、1e8→656 及公开答案表一致）。
 */
internal fun solve0302Impl(): Long {
    val limit = 1_000_000_000_000_000_000L
    val maxPrime = minOf(sqrt(limit.toDouble()).toLong().toInt(), 1_000_000)

    val isP = sieve(maxPrime)
    val primes = (2..maxPrime).filter { isP[it] }.toIntArray()

    fun factor(n: Int): List<Pair<Int, Int>> {
        val out = ArrayList<Pair<Int, Int>>()
        var x = n
        for (q in primes) {
            if (q.toLong() * q > x) break
            if (x % q == 0) {
                var c = 0
                while (x % q == 0) { x /= q; c++ }
                out.add(q to c)
            }
        }
        if (x > 1) out.add(x to 1)
        return out
    }
    val factorP = HashMap<Int, List<Pair<Int, Int>>>()
    val extraCap = IntArray(maxPrime + 1)
    for (p in primes) {
        val f = factor(p - 1)
        factorP[p] = f
        for ((q, v) in f) if (q <= maxPrime) extraCap[q] += v
    }

    var answer = 0L
    val phiExp = HashMap<Int, Int>()
    val hist = ArrayList<Pair<Int, Int>>()

    fun addPhi(p: Int, e: Int): Int {
        val before = hist.size
        if (e >= 2) {
            val old = phiExp[p] ?: 0
            phiExp[p] = old + (e - 1)
            hist.add(p to old)
        }
        for ((q, v) in factorP[p]!!) {
            val old = phiExp[q] ?: 0
            phiExp[q] = old + v
            hist.add(q to old)
        }
        return before
    }

    fun undoPhi(before: Int) {
        while (hist.size > before) {
            val (k, old) = hist.removeAt(hist.size - 1)
            if (old == 0) phiExp.remove(k) else phiExp[k] = old
        }
    }

    fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    fun dfs(pi: Int, cur: Long, expGcd: Int) {
        if (cur > 1 && expGcd == 1) {
            var allGe2 = true
            var phiG = 0
            for ((q, v) in phiExp) {
                if (v < 2) { allGe2 = false; break }
                phiG = gcd(phiG, v)
            }
            if (allGe2 && phiG == 1) answer++
        }
        var j = pi
        while (j < primes.size) {
            val p = primes[j]
            if (cur > limit / p / p) break
            var pe = p.toLong() * p
            var e = 2
            while (true) {
                if (cur > limit / pe) break
                val nGcd = if (expGcd == 0) e else gcd(expGcd, e)
                val snap = addPhi(p, e)
                var prune = false
                for ((q, v) in phiExp) {
                    if (v < 2 && v + (if (q <= maxPrime) extraCap[q] else 0) < 2) { prune = true; break }
                }
                if (!prune) dfs(j + 1, cur * pe, nGcd)
                undoPhi(snap)
                if (pe > limit / cur / p) break
                pe *= p
                e++
            }
            j++
        }
    }

    dfs(0, 1L, 0)
    return answer
}
