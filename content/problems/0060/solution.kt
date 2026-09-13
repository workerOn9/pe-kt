/**
 * Project Euler 060 — Prime Pair Sets
 *
 * 思路：把每个素数看作顶点，若 p、q 两个方向拼接出的数都是素数，就在两者之间连一条边；
 * 题目要的 5 个素数正是这张图上的一个 5-团（5-clique），求权和最小的那个团。
 * 拼接数最大不到 10^8，用 12 个基的确定性 Miller–Rabin 判素足够稳；
 * 10000 以内共 pi(10^4)=1229 个素数，先把边全部建好（位集存邻接表，只存比自身大的顶点），
 * 再按顶点序号递增做带下界剪枝的 DFS 枚举 5-团：当前已选权和 + 剩余顶点数 × 当前最小候选
 * 一旦不小于已知最优就整体剪掉。
 * 复杂度：建边 O(m^2 log)，m = 1229；团搜索只在稀疏图上展开，实测毫秒级。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private val MR_BASES = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)

private fun modPowL(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var r = 1L
    while (e > 0) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return r
}

/** n < 10^8，底数集合 {2..37} 下 Miller–Rabin 为确定性判素。 */
private fun isPrime64(n: Long): Boolean {
    if (n < 2L) return false
    for (p in MR_BASES) if (n % p == 0L) return n == p
    var d = n - 1
    var s = 0
    while (d and 1L == 0L) { d = d shr 1; s++ }
    outer@ for (a in MR_BASES) {
        var x = modPowL(a, d, n)
        if (x == 1L || x == n - 1) continue
        var r = 1
        while (r < s) {
            x = x * x % n
            if (x == n - 1) continue@outer
            r++
        }
        return false
    }
    return true
}

private fun digitsCount(n: Int): Int = when {
    n < 10 -> 1
    n < 100 -> 2
    n < 1000 -> 3
    else -> 4
}

private fun concat(a: Int, b: Int): Long {
    var p = 1L
    repeat(digitsCount(b)) { p *= 10L }
    return a * p + b
}

fun solve(): Long {
    val limit = 10000
    val composite = BooleanArray(limit)
    val primes = ArrayList<Int>()
    for (i in 2 until limit) {
        if (!composite[i]) {
            primes.add(i)
            var j = i.toLong() * i
            while (j < limit) { composite[j.toInt()] = true; j += i }
        }
    }

    val m = primes.size
    val adj = Array(m) { java.util.BitSet(m) }
    for (i in 0 until m) {
        val a = primes[i]
        for (j in i + 1 until m) {
            val b = primes[j]
            if (isPrime64(concat(a, b)) && isPrime64(concat(b, a))) adj[i].set(j)
        }
    }

    var best = Long.MAX_VALUE
    fun dfs(size: Int, cand: java.util.BitSet, sum: Long) {
        if (size == 5) {
            if (sum < best) best = sum
            return
        }
        var i = cand.nextSetBit(0)
        while (i >= 0) {
            if (best != Long.MAX_VALUE && sum + (5 - size).toLong() * primes[i] >= best) return
            val next = cand.clone() as java.util.BitSet
            next.and(adj[i])
            dfs(size + 1, next, sum + primes[i])
            i = cand.nextSetBit(i + 1)
        }
    }

    val all = java.util.BitSet(m)
    all.set(0, m)
    dfs(0, all, 0L)
    return best
}

fun main() {
    println(solve())
}
