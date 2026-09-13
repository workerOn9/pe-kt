/**
 * Project Euler 029 — Distinct Powers
 *
 * 优化解：把 a 写成 p^k（p 不是完全幂），则 a^b = p^(kb)：只需记录规范化的 (p, kb)
 * 二元组，用 64 位整数编码进 HashSet，避免 BigInteger 大数运算。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

fun gcdL(a: Long, b: Long): Long {
    var x = kotlin.math.abs(a); var y = kotlin.math.abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

fun minBaseExp(a: Int): Pair<Int, Int> {
    var n = a; val exps = ArrayList<Int>()
    var d = 2
    while (d * d <= n) { if (n % d == 0) { var e = 0; while (n % d == 0) { n /= d; e++ }; exps.add(e) } ; d++ }
    if (n > 1) exps.add(1)
    var g = exps[0]; for (e in exps) g = gcdL(g.toLong(), e.toLong()).toInt()
    var p = Math.round(Math.pow(a.toDouble(), 1.0 / g)).toInt()
    while (Math.pow(p.toDouble(), g.toDouble()) < a - 1e-6) p++
    while (Math.pow(p.toDouble(), g.toDouble()) > a + 1e-6) p--
    return p to g
}

fun solve(): Long {
    val seen = HashSet<Long>()
    for (a in 2..100) {
        val (p, g) = minBaseExp(a)
        for (b in 2..100) seen.add(p * 1000L + g.toLong() * b)
    }
    return seen.size.toLong()
}

fun main() {
    println(solve())
}
