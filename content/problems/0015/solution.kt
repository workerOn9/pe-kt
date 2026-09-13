/**
 * Project Euler 015 — Lattice Paths
 *
 * 优化解：组合数公式，O(n)。
 * 20×20 网格从左上到右下，只能向右/向下，总共走 40 步，
 * 其中恰有 20 步向右——任选位置即可：C(40, 20)。
 * 结果 137846528820 > 2^37，超过 Int 范围；C(40,20) 本身仍在 Long 内，
 * 但中间乘法可能溢出，故用 BigInteger 逐步乘除（每一步都是精确整数）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

/** 组合数 C(n, k)，逐步乘除保证中间结果始终是整数 */
fun binomial(n: Int, k: Int): BigInteger {
    var result = BigInteger.ONE
    for (i in 1..k) {
        result = result.multiply(BigInteger.valueOf((n - k + i).toLong()))
                       .divide(BigInteger.valueOf(i.toLong()))
    }
    return result
}

fun solve(gridSize: Int = 20): BigInteger = binomial(2 * gridSize, gridSize)

fun main() {
    println(solve())
}
