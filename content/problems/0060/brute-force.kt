/**
 * Project Euler 060 — 暴力解（教学对比用）
 *
 * 不做邻接表预计算：按素数递增顺序直接 DFS，每加入一个新素数就与已选素数逐个
 * 做双向拼接 + 试除判素，仅用「已选和 + 剩余个数 × 当前候选 >= 已知最优」剪枝。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private fun bfIsPrime(n: Long): Boolean {
    if (n < 2L) return false
    if (n % 2L == 0L) return n == 2L
    if (n % 3L == 0L) return n == 3L
    var d = 5L
    while (d <= n / d) {
        if (n % d == 0L || n % (d + 2L) == 0L) return false
        d += 6L
    }
    return true
}

private fun bfDigits(n: Int): Int = when {
    n < 10 -> 1
    n < 100 -> 2
    n < 1000 -> 3
    else -> 4
}

private fun bfConcat(a: Int, b: Int): Long {
    var p = 1L
    repeat(bfDigits(b)) { p *= 10L }
    return a * p + b
}

private fun bfCompatible(a: Int, b: Int): Boolean =
    bfIsPrime(bfConcat(a, b)) && bfIsPrime(bfConcat(b, a))

fun solveBruteForce(): Long {
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

    var best = Long.MAX_VALUE
    val chosen = IntArray(5)

    fun dfs(size: Int, startIdx: Int, sum: Long) {
        if (size == 5) {
            if (sum < best) best = sum
            return
        }
        for (i in startIdx until primes.size) {
            val p = primes[i]
            if (best != Long.MAX_VALUE && sum + (5 - size).toLong() * p >= best) break
            var ok = true
            for (k in 0 until size) if (!bfCompatible(chosen[k], p)) { ok = false; break }
            if (!ok) continue
            chosen[size] = p
            dfs(size + 1, i + 1, sum + p)
        }
    }

    dfs(0, 0, 0L)
    return best
}

fun main() {
    println(solveBruteForce())
}
