/**
 * Project Euler 099 — Largest Exponential（暴力解）
 *
 * 思路：不引入高精度库，直接用 double 计算 e·ln b 并取最大——也就是题面里
 * 「任何计算器都能做到」的做法。对本题数据而言，最大项与次大项的对数值相差远大于
 * double 的相对误差，因此它同样给出正确答案；但它原则上没有任何精度保证。
 * 需从题目目录运行（读取同目录 base_exp.txt）。
 * 复杂度：O(N) 次浮点对数与乘法。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(path: String = "base_exp.txt"): Long {
    val lines = java.io.File(path).readLines().map { it.trim() }.filter { it.isNotEmpty() }
    var bestIndex = -1L
    var bestValue = Double.NEGATIVE_INFINITY
    for ((idx, line) in lines.withIndex()) {
        val parts = line.split(',')
        val base = parts[0].trim().toDouble()
        val exp = parts[1].trim().toDouble()
        val value = exp * Math.log(base)
        if (value > bestValue) {
            bestValue = value
            bestIndex = (idx + 1).toLong()
        }
    }
    return bestIndex
}

fun main() {
    println(solveBruteForce())
}
