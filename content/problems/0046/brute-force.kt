/**
 * Project Euler 046 — 暴力解（教学对比用）
 *
 * 不使用任何预计算表，对每个奇合数现场试除判素、开方判平方，重复计算量大。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) { if (n % d == 0L) return false; d += 2 }
    return true
}

fun solveBruteForce(): Long {
    var n = 9
    while (true) {
        if (isPrimeLong(n.toLong())) { n += 2; continue }
        var found = false
        var p = 2
        while (p < n) {
            if (isPrimeLong(p.toLong())) {
                val rem = n - p
                if (rem % 2 == 0) {
                    val s = Math.sqrt((rem / 2).toDouble()).toLong()
                    if (s * s == (rem / 2).toLong()) { found = true; break }
                }
            }
            p++
        }
        if (!found) return n.toLong()
        n += 2
    }
}

fun main() {
    println(solveBruteForce())
}
