package dev.pekt.problems

/**
 * PE 196 暴力解：对较小行号直接建表，逐点核对该点的 8 邻域与三元组归属。
 *
 * 与 solution.kt 的分段筛不同，这里把第 1..n+2 行整段筛出来，
 * 用 (行,列) 坐标做朴素邻居判断，用于交叉验证 S(8)=60、S(9)=37、S(10)=47。
 *
 * 仅用于小规模验证（n <= 200 左右）。
 */
fun bruteForce196(n: Int): Long {
    val t = triB(n + 2)
    if (t < 2) return 0L
    val isPrime = sieveB(t)
    fun isP(v: Long) = v in 2..t && isPrime[v.toInt()]

    fun neighbours(y: Int, x: Int): List<Pair<Int, Int>> {
        val out = mutableListOf<Pair<Int, Int>>()
        for (dy in -1..1) {
            val yy = y + dy
            if (yy < 1) continue
            for (dx in -1..1) {
                if (dy == 0 && dx == 0) continue
                val xx = x + dx
                if (xx < 1 || xx > yy) continue
                out.add(yy to xx)
            }
        }
        return out
    }

    fun value(y: Int, x: Int): Long = triB(y - 1) + x

    // good：自身素数邻居数 >= 2 的素数
    val good = HashSet<Long>()
    for (y in 1..(n + 1)) {
        for (x in 1..y) {
            val v = value(y, x)
            if (!isP(v)) continue
            if (neighbours(y, x).count { (yy, xx) -> isP(value(yy, xx)) } >= 2) good.add(v)
        }
    }

    var sum = 0L
    for (x in 1..n) {
        val v = value(n, x)
        if (!isP(v)) continue
        val inTriplet = good.contains(v) ||
            neighbours(n, x).any { (yy, xx) -> val u = value(yy, xx); isP(u) && good.contains(u) }
        if (inTriplet) sum += v
    }
    return sum
}

private fun triB(k: Int): Long = k.toLong() * (k + 1) / 2

private fun sieveB(n: Long): BooleanArray {
    val isPrime = BooleanArray(n.toInt() + 1) { true }
    isPrime[0] = false
    isPrime[1] = false
    var i = 2
    while (i.toLong() * i <= n) {
        if (isPrime[i]) {
            var j = i * i
            while (j <= n) { isPrime[j] = false; j += i }
        }
        i++
    }
    return isPrime
}

fun main() {
    println("S(8)  = ${bruteForce196(8)}  (题面 60)")
    println("S(9)  = ${bruteForce196(9)}  (题面 37)")
    println("S(10) = ${bruteForce196(10)} (与优化解一致：47)")
    println("S(100) = ${bruteForce196(100)} (与优化解一致：9938)")
    println("S(1000) = ${bruteForce196(1000)}")
}
