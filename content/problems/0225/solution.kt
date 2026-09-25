#!/usr/bin/env kotlin
/**
 * Project Euler 225 — Tribonacci Non-divisors（Tribonacci 非因子）
 *
 * 思路：判断奇数 n 是否整除序列中的某一项，只需在模 n 下把三项状态 (x,y,z) 迭代下去。
 *       递推的转移矩阵 [[0,1,0],[0,0,1],[1,1,1]] 行列式为 1，故模 n 可逆，
 *       轨道是纯周期的（不会进入尾巴）。从初态 (T_1,T_2,T_3) = (1,1,1) 出发，
 *         某一步出现 0  -> n 整除某一项，n 不是所求；
 *         状态回到 (1,1,1) -> 整个周期内没有 0，n 就是所求的非因子。
 *       从小到大扫奇数（从 3 起，1 整除一切不做考虑），收集到第 124 个即答案。
 *
 * 复杂度：单个 n 的判定是 O(周期)，周期以 n^2 为上界（本题扫描范围内总和只有几十万步），
 *         总体远小于 1 ms 量级；答案 2009。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

/** n 是否整除 Tribonacci 序列的某一项。 */
private fun dividesSome(n: Int): Boolean {
    var x = 1 % n
    var y = x
    var z = x
    if (x == 0) return true
    while (true) {
        val t = (x + y + z) % n
        x = y; y = z; z = t
        if (x == 0 || y == 0 || z == 0) return true
        if (x == 1 && y == 1 && z == 1) return false
    }
}

/** 第 target 个不整除序列任何一项的奇数。 */
fun solve(target: Int = 124): Int {
    var found = 0
    var n = 3
    while (true) {
        if (!dividesSome(n)) {
            found++
            if (found == target) return n
        }
        n += 2
    }
}

fun main() {
    val answer = solve()
    println("PE 225 answer = $answer")
    check(answer == 2009) { "225 mismatch: $answer" }
}
