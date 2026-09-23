package dev.pekt.problems

/**
 * PE 194 暴力解：直接枚举所有染色方案并验证合法性。
 * 仅对小参数测试，对 (25, 75, 1984) 完全不可行。
 * 枚举 a+b 个单元的排列（二项式系数种），对每种排列暴力枚举顶点颜色组合。
 * 由于图结构固定，可以按顺序逐顶点染色并回溯计数。
 */
fun bruteForce194(a: Int = 2, b: Int = 2, c: Int = 3): Long {
    // 对很小参数验证公式正确性
    return solve194_brute(a, b, c)
}

private fun solve194_brute(a: Int, b: Int, c: Int): Long {
    // 简化：仅验证 N(a,b,c) 小例子
    // 实际完整暴力需枚举所有 c^(a+b+...)^? 种颜色赋值
    // 这里用容斥/生成函数替代暴力
    TODO("需要构建具体图结构的暴力，留作验证")
}

fun main() {
    println("验证 N(1,0,3) = ${solve194_brute(1, 0, 3)}")
    println("验证 N(0,2,4) = ${solve194_brute(0, 2, 4)}")
    println("验证 N(2,2,3) = ${solve194_brute(2, 2, 3)}")
}
