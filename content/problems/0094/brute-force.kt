/**
 * Project Euler 094 — Almost Equilateral Triangles
 *
 * 暴力解：直接扫描腰长 n，对底为 n+1 与 n−1 的两种三角形分别算 3n²∓2n−1，
 * 用完全平方判定面积是否为整数，命中则累加周长 3n∓1；上界取周长 ≤ 10^9。
 *
 * 复杂度：时间 O(P)，P = 10^9（实际扫描 n ≤ 3.3·10^8），空间 O(1)
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isSquareBrute(n: Long): Boolean {
    if (n < 0) return false
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r * r == n
}

fun solveBruteForce(): Long {
    val limit = 1_000_000_000L
    var sum = 0L
    var n = 2L
    while (3 * n + 1 <= limit) {
        if (isSquareBrute(3 * n * n - 2 * n - 1)) sum += 3 * n + 1
        if (isSquareBrute(3 * n * n + 2 * n - 1)) sum += 3 * n - 1
        n++
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
