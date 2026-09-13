/**
 * Project Euler 046 — Goldbach's Other Conjecture
 *
 * 优化解：筛出素数表并预存所有 2k² 平方数（k≤70 即可覆盖搜索范围）；
 * 从 9 起枚举奇合数，检查是否存在素数 p 使 n−p 落在 2k² 集合中。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun sieveBool(limit: Int): BooleanArray {
    val composite = BooleanArray(limit + 1)
    val result = BooleanArray(limit + 1) { it >= 2 }
    if (limit < 2) return result
    var p = 2
    while (p.toLong() * p <= limit) {
        if (!composite[p]) {
            var m = p * p
            while (m <= limit) { composite[m] = true; m += p }
        }
        p++
    }
    for (i in 2..limit) result[i] = !composite[i]
    return result
}

fun solve(): Long {
    val N = 10_000
    val isP = sieveBool(N)
    val twiceSquares = HashSet<Int>()
    var k = 1
    while (2 * k * k <= N) { twiceSquares.add(2 * k * k); k++ }
    var n = 9
    while (true) {
        if (n % 2 == 1 && !isP[n]) {
            var found = false
            for (p in 2 until n) {
                if (isP[p] && twiceSquares.contains(n - p)) { found = true; break }
            }
            if (!found) return n.toLong()
        }
        n += 2
    }
}

fun main() {
    println(solve())
}
