package dev.pekt.problems

/**
 * PE 197 暴力解：直接模拟迭代过程。
 * 用于验证收敛行为。
 */
fun bruteForce197(steps: Int = 100): List<Double> {
    var u = -1.0
    val history = mutableListOf(u)
    for (_ in 1..steps) {
        u = f(u)
        history.add(u)
    }
    return history
}

private fun f(x: Double): Double {
    return Math.floor(Math.pow(2.0, 30.403243784 - x * x)) * 1e-9
}

fun main() {
    val h = bruteForce197(20)
    println("First 20 terms:")
    h.forEachIndexed { i, v -> println("  u[$i] = $v") }
    
    // 检查 2-cycle
    val last = h.last()
    val next = f(last)
    println("\nLast: $last, next: $next, sum: ${last + next}")
}
