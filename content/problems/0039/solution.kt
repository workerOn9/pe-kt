/**
 * Project Euler 039 — Integer Right Triangles
 *
 * 优化解：用欧几里得公式 a=k(m²−n²)、b=2kmn、c=k(m²+n²) 生成所有本原勾股数，
 * 再按倍数 k 平移到周长 ≤1000 上累加计数，只枚举 m,n 而非所有边长。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun gcdL(a: Long, b: Long): Long {
    var x = kotlin.math.abs(a); var y = kotlin.math.abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

fun solve(): Long {
    val count = IntArray(1001)
    for (m in 2..31) for (n in 1 until m) {
        if ((m - n) % 2 == 0 || gcdL(m.toLong(), n.toLong()) != 1L) continue
        val p0 = m * m - n * n + 2 * m * n + m * m + n * n
        for (k in 1..1000 / p0) count[k * p0]++
    }
    var best = 0; var bestP = 0
    for (p in 1..1000) if (count[p] > best) { best = count[p]; bestP = p }
    return bestP.toLong()
}

fun main() {
    println(solve())
}
