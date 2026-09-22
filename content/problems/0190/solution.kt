package dev.pekt.problems

import java.math.BigDecimal
import java.math.MathContext

/**
 * Problem 190: Maximising a Weighted Product
 *
 * 思路：
 * 对目标函数 P_m = prod_{i=1}^m x_i^i 取对数：
 * ln(P_m) = sum_{i=1}^m i * ln(x_i)
 * 引入拉格朗日乘子法构造拉格朗日函数：
 * L(x_1, ..., x_m, lambda) = sum_{i=1}^m i * ln(x_i) - lambda * (sum_{i=1}^m x_i - m)
 * 对各 x_i 求偏导并令其为 0：
 * dL / dx_i = i / x_i - lambda = 0 => x_i = i / lambda
 * 代入线性约束条件：
 * sum_{i=1}^m x_i = (1 / lambda) * sum_{i=1}^m i = (1 / lambda) * [m(m+1)/2] = m
 * 解得：
 * lambda = (m + 1) / 2
 * 从而最优点为：
 * x_i = 2 * i / (m + 1)
 *
 * 将 x_i 代入 P_m：
 * P_m = prod_{i=1}^m ( 2 * i / (m + 1) )^i
 * 使用 BigDecimal 高精度浮点计算并下取整（toBigInteger），避免双精度浮点数在大数时可能的精度损失。
 *
 * 复杂度：
 * 时间复杂度 O(m^2)，极快（< 1 ms）。
 * 空间复杂度 O(1)。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve0190(): Long {
    val mc = MathContext(60)

    fun computePmFloor(m: Int): Long {
        val mDec = BigDecimal(m)
        val denom = BigDecimal(m + 1)
        var prod = BigDecimal.ONE

        for (i in 1..m) {
            val num = BigDecimal(2 * i)
            val base = num.divide(denom, mc)
            val term = base.pow(i, mc)
            prod = prod.multiply(term, mc)
        }

        return prod.toBigInteger().toLong()
    }

    var totalSum = 0L
    for (m in 2..15) {
        totalSum += computePmFloor(m)
    }

    return totalSum
}

fun main() {
    println(solve0190())
}
