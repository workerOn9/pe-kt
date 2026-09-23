package dev.pekt.problems

/**
 * PE 195 暴力解：枚举所有可能的三角形边长，检查是否满足条件。
 * 仅对小参数可行。
 */
fun bruteForce195(n: Long = 100L): Long {
    // 较小的 60° 邻边 b 的上界：固定 b 时 r 的极小值为 b*sqrt(3)/6（等边），故 b <= 2*sqrt(3)*n
    val bMax = (2.0 * Math.sqrt(3.0) * n).toLong() + 2
    val bInt = bMax.toInt()
    // 最小素因子筛，用于枚举 3b^2 的因子
    val spf = IntArray(bInt + 1) { it }
    var i = 2
    while (i.toLong() * i <= bMax) {
        if (spf[i] == i) {
            var j = i * i
            while (j <= bInt) { if (spf[j] == j) spf[j] = i; j += i }
        }
        i++
    }

    var count = 0L
    for (b in 1L..bMax) {
        val m = 3 * b * b
        // 3b^2 的因子：b 的素因子指数乘 2，再补一个 3
        val ex = HashMap<Int, Int>()
        var x = b
        while (x > 1) {
            val p = spf[x.toInt()]
            var e = 0
            while (x % p == 0L) { x /= p; e++ }
            ex[p] = (ex[p] ?: 0) + 2 * e
        }
        ex[3] = (ex[3] ?: 0) + 1
        val divs = ArrayList<Long>()
        divs.add(1L)
        for ((p, e) in ex) {
            val base = divs.toList()
            var mult = p.toLong()
            for (k in 1..e) {
                for (d in base) divs.add(d * mult)
                mult *= p
            }
        }
        for (P in divs) {
            val Q = m / P
            // (2c-u)(2c+u) = 3b^2，需要 u = (Q-P)/2、c = (P+Q)/4 均为正整数
            if ((Q - P) % 2 != 0L || (P + Q) % 4 != 0L) continue
            val u = (Q - P) / 2
            val c = (P + Q) / 4
            if (u <= 0 || c <= 0 || (u + b) % 2 != 0L) continue
            val a = (u + b) / 2
            if (a < b) continue                        // 交换 a、b 是同一个三角形
            if (a == b && b == c) continue             // 等边三角形有三个 60° 角
            if (a + b <= c || b + c <= a || a + c <= b) continue
            // r 是 sqrt(3) 的有理倍数，与整数 n 不可能恰好相等，用双精度比较安全
            val r = a * b * Math.sqrt(3.0) / (2.0 * (a + b + c))
            if (r <= n) count++
        }
    }
    return count
}

fun main() {
    println("T(100)  = ${bruteForce195(100)}  (题面 1234)")
    println("T(1000) = ${bruteForce195(1000)} (题面 22767)")
    println("T(10000) = ${bruteForce195(10000)} (题面 359912)")
}
