/**
 * Project Euler 031 — 暴力解（教学对比用）
 *
 * 按面额从大到小递归枚举每种硬币取几枚，直接数出所有组合（无记忆化）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val coins = intArrayOf(200, 100, 50, 20, 10, 5, 2, 1)
    fun count(idx: Int, rest: Int): Long {
        if (rest == 0) return 1L
        if (idx == coins.size) return 0L
        var total = 0L; var k = 0
        while (k * coins[idx] <= rest) { total += count(idx + 1, rest - k * coins[idx]); k++ }
        return total
    }
    return count(0, 200)
}

fun main() {
    println(solveBruteForce())
}
