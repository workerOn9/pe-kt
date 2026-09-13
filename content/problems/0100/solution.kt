/**
 * Project Euler 100 — Arranged Probability
 *
 * 设盒中共 n 个盘、其中 b 个为蓝盘。从中取两盘都是蓝盘的概率为
 *   b(b-1) / (n(n-1)) = 1/2   ⇔   n(n-1) = 2b(b-1)。
 * 令 x = 2n-1、y = 2b-1（两者必为奇数），代入得
 *   x² - 2y² = -1，
 * 即负佩尔方程。它的全部正整数解由 (1+√2)(3+2√2)^k 给出，等价的整数递推是
 *   x' = 3x + 4y,   y' = 2x + 3y,
 * 从 (x,y) = (1,1) 出发得到的解按 n = (x+1)/2 严格递增。
 * 于是从最小的解开始逐个生成，返回第一个 n > 10^12 的解所对应的蓝色盘数 b = (y+1)/2。
 *
 * 复杂度：O(log 上限) 个数级，实际只递推约 17 步；x ≈ 2.14×10^12，全程 Long 不溢出。
 * 由 (x+y√2) 的量级可直接看出第 k 个解约为 (3+2√2)^k，故解的个数对数级增长。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val limit = 1_000_000_000_000L
    var x = 1L
    var y = 1L
    while ((x + 1) / 2 <= limit) {
        val nx = 3 * x + 4 * y
        val ny = 2 * x + 3 * y
        x = nx
        y = ny
    }
    return (y + 1) / 2
}

fun main() {
    println(solve())
}
