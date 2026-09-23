#!/usr/bin/env kotlin
// PE 203 — Squarefree Binomial Coefficients（无平方因子二项式系数）
// 思路：递推生成前 51 行帕斯卡三角形（最大 C(50,25)=126410606437752，远在 Long 内），
//       去重后对每个数用「试除到 √v 的素数」判定无平方因子（找到 p|v 后立刻查 p²|v，
//       并除掉 p 缩小剩余值），求和。
// 复杂度：O(51²) 生成 + O(P·√max) 判定（P=不同值个数，提前终止后实际很快）。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    val n = 51
    // 生成帕斯卡三角形
    var prev = longArrayOf(1)
    val values = HashSet<Long>()
    values.add(1)
    for (row in 1 until n) {
        val cur = LongArray(row + 1)
        cur[0] = 1; cur[row] = 1
        for (k in 1 until row) cur[k] = prev[k - 1] + prev[k]
        for (v in cur) values.add(v)
        prev = cur
    }

    // 素数筛到 sqrt(max)
    val maxV = values.max()
    val lim = Math.sqrt(maxV.toDouble()).toInt() + 1
    val sieve = BooleanArray(lim + 1) { true }
    sieve[0] = false; sieve[1] = false
    var i = 2
    while (i * i <= lim) {
        if (sieve[i]) {
            var j = i * i
            while (j <= lim) { sieve[j] = false; j += i }
        }
        i++
    }
    val primes = (2..lim).filter { sieve[it] }

    fun isSquarefree(x0: Long): Boolean {
        var v = x0
        for (p in primes) {
            val pl = p.toLong()
            if (pl * pl > v) return true
            if (v % pl == 0L) {
                if (v % (pl * pl) == 0L) return false
                v /= pl
            }
        }
        return true
    }

    val ans = values.filter { isSquarefree(it) }.sum()
    println(ans)
}
