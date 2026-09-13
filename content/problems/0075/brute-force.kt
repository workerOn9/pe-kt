/**
 * Project Euler 075 — Singular Integer Right Triangles（暴力对照解）
 *
 * 暴力解：只用欧几里得公式「所有整数直角三角形都能写成 (m²−n², 2mn, m²+n²) 或其倍数」
 * 这一条最基本的事实，不加互素/奇偶剪枝，把所有 (m, n, k) 组合一股脑枚举出来，
 * 用 HashMap 记录每个周长上出现过的三元组：同一个三元组重复出现就忽略，
 * 出现第二个不同的三元组就把该周长标记为「多解」。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

fun solveBruteForce(): Long {
    val limit = 1_500_000
    val first = HashMap<Int, Long>()
    var m = 2
    while (2L * m * (m + 1) <= limit) {
        for (n in 1 until m) {
            val a = m * m - n * n
            val b = 2 * m * n
            val base = a + b + m * m + n * n
            if (base > limit) continue
            val lo = if (a < b) a else b
            val hi = if (a < b) b else a
            var k = 1L
            while (k * base <= limit) {
                val l = (k * base).toInt()
                val key = k * lo * 2_000_000L + k * hi
                val cur = first[l]
                if (cur == null) first[l] = key
                else if (cur != key) first[l] = -1L
                k++
            }
        }
        m++
    }
    var total = 0L
    for (v in first.values) if (v != -1L) total++
    return total
}

fun main() {
    println(solveBruteForce())
}
