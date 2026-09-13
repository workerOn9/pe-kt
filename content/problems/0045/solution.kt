/**
 * Project Euler 045 — Triangular, Pentagonal, and Hexagonal
 *
 * 优化解：六边形数 H_n = n(2n−1) 恒为三角形数（T_{2n−1} = H_n），故只需从 n=144
 * 起枚举六边形数并检查是否五边形数，一次命中即可。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPentagonalN(n: Long): Boolean {
    val d = 1 + 24L * n
    val s = Math.sqrt(d.toDouble()).toLong()
    for (k in (s - 2)..(s + 2)) if (k * k == d && (k + 1) % 6 == 0L) return true
    return false
}

fun solve(): Long {
    var n = 144
    while (true) {
        val h = n.toLong() * (2L * n - 1)
        if (isPentagonalN(h)) return h
        n++
    }
}

fun main() {
    println(solve())
}
