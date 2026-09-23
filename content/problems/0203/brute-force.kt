#!/usr/bin/env kotlin
// PE 203 暴力参照：对每个不同的二项式系数值 v，试除到 v 的完全平方根为止判无平方因子，
// 不做「除掉 p 缩小 v」的提前优化；规模同 solution.kt，仅作为交叉验证路径。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

fun main() {
    val n = 51
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
    val maxV = values.max()
    val lim = Math.sqrt(maxV.toDouble()).toInt() + 1
    val sieve = BooleanArray(lim + 1) { true }
    sieve[0] = false; sieve[1] = false
    var i = 2
    while (i * i <= lim) {
        if (sieve[i]) { var j = i * i; while (j <= lim) { sieve[j] = false; j += i } }
        i++
    }
    val primes = (2..lim).filter { sieve[it] }
    fun squarefreeNaive(x: Long): Boolean {
        for (p in primes) {
            val pl = p.toLong()
            if (pl * pl > x) break
            if (x % (pl * pl) == 0L) return false
        }
        return true
    }
    println(values.filter { squarefreeNaive(it) }.sum())
}
