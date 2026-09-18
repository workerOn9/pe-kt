/**
 * Project Euler 152 — Sums of Square Reciprocals（平方倒数之和）· 暴力对照解
 *
 * 思路：与 solution.kt 相同的子集枚举，但无剪枝、无后缀和表，逐状态枚举所有可能组合。
 * 用 Double 而非精确分数（牺牲精度，仅用于对比框架，仅验证 n=10 左右的小规模）。
 */

fun solveBruteForce(limit: Int = 80): Long {
    val nums = (2..limit).map { 1.0 / (it * it) }
    val target = 0.5
    
    var count = 0L
    
    fun dfs(idx: Int, currentSum: Double) {
        if (kotlin.math.abs(currentSum - target) < 1e-12) {
            count++
            return
        }
        if (idx >= nums.size) return
        if (currentSum > target + 1e-12) return
        
        // 选
        dfs(idx + 1, currentSum + nums[idx])
        // 不选
        dfs(idx + 1, currentSum)
    }
    
    dfs(0, 0.0)
    return count
}

fun main() {
    val start = System.nanoTime()
    val answer = solveBruteForce()
    val elapsed = (System.nanoTime() - start).toDouble() / 1e6
    System.err.printf("brute: %.4f ms%n", elapsed)
    println(answer)
}
