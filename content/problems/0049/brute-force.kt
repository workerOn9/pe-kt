/**
 * Project Euler 049 — 暴力解（教学对比用）
 *
 * 枚举所有四位数素数对 (a,b) 并检查 2b−a 是否为同排列素数（不做分桶剪枝）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun sortedDigits(n: Int): String { return n.toString().toCharArray().sorted().joinToString("") }

fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) { if (n % d == 0L) return false; d += 2 }
    return true
}

fun solveBruteForce(): Long {
    val primes = (1000..9999).filter { isPrimeLong(it.toLong()) }
    var best = 0L
    for (a in primes) for (b in primes) {
        val c = 2 * b - a
        if (c > 9999 || c <= b) continue
        if (!isPrimeLong(c.toLong())) continue
        val key = sortedDigits(a)
        if (sortedDigits(b) == key && sortedDigits(c) == key) {
            val s = "" + a + b + c
            if (s == "148748178147") continue
            val v = s.toLong()
            if (v > best) best = v
        }
    }
    return best
}

fun main() {
    println(solveBruteForce())
}
