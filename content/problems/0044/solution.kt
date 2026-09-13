/**
 * Project Euler 044 — Pentagon Numbers
 *
 * 优化解：按 k 递增枚举五边形数，只对更大的一侧做「和、差是否都是五边形数」判定；
 * 判定用 24n+1 是否为完全平方（且开方为 ≡5 mod 6）的 O(1) 公式。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPentagonal(n: Long): Boolean {
    val d = 1 + 24L * n
    val s = Math.sqrt(d.toDouble()).toLong()
    for (k in (s - 2)..(s + 2)) if (k * k == d && (k + 1) % 6 == 0L) return true
    return false
}

fun pentagonal(n: Int): Long { return n.toLong() * (3L * n - 1) / 2 }

fun solve(): Long {
    var i = 2
    while (true) {
        val pi = pentagonal(i)
        for (j in i - 1 downTo 1) {
            val pj = pentagonal(j)
            if (isPentagonal(pi - pj) && isPentagonal(pi + pj)) return pi - pj
        }
        i++
    }
}

fun main() {
    println(solve())
}
