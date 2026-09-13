/**
 * Project Euler 078 — 暴力解（教学对比用）
 *
 * 思路：同样用欧拉五边形数递推，但不做任何取模压缩，用 BigInteger 保存 p(n) 的精确值，
 * 再对每个 n 判断 p(n) 能否被 10^6 整除。数值很快增长到数百位，位运算成本随之上升。
 * 它对应「最直白地照搬定义与递推」的写法，用作与取模版本的耗时对比。
 * 复杂度：O(n√n) 次数论运算，但每次大整数加减的代价为 O(位数)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    val limit = 100_000
    val million = BigInteger.valueOf(1_000_000L)
    val p = arrayOfNulls<BigInteger>(limit + 1)
    p[0] = BigInteger.ONE

    for (n in 1..limit) {
        var total = BigInteger.ZERO
        var k = 1
        while (true) {
            val g1 = k * (3 * k - 1) / 2
            if (g1 > n) break
            val add = k % 2 == 1
            val t1 = p[n - g1]!!
            total = if (add) total.add(t1) else total.subtract(t1)
            val g2 = k * (3 * k + 1) / 2
            if (g2 <= n) {
                val t2 = p[n - g2]!!
                total = if (add) total.add(t2) else total.subtract(t2)
            }
            k++
        }
        p[n] = total
        if (total.mod(million) == BigInteger.ZERO) return n.toLong()
    }
    error("limit $limit too small")
}

fun main() {
    println(solveBruteForce())
}
