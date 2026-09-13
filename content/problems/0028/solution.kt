/**
 * Project Euler 028 — Number Spiral Diagonals
 *
 * 优化解：只看对角线：第 k 层（边长 2k+1）的四个角为 (2k+1)²、减 2k、4k、6k，
 * 四角之和 = 16k²+4k+4；总和对 k=1..500 求和再加中心的 1，O(1) 出解。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    var sum = 1L
    for (k in 1..500) sum += 16L * k * k + 4L * k + 4L
    return sum
}

fun main() {
    println(solve())
}
