/**
 * Project Euler 114 — Counting Block Combinations I（独立对照解）
 *
 * 与 solution.kt 算同一个量 f(50)，但走完全独立的第三条路子：不按「首格是灰还是红」做
 * 长度递推，而是**按红块个数 k 分类**，用隔板法给出闭式
 *
 *   f(n) = Σ_{k≥0} C(n − (m−1)k + 1, 2k)，  m = 3，k 上限 ⌊(n+1)/(m+1)⌋。
 *
 * 推导：k 块红、每块至少 m 格，块与块之间至少 1 个灰格，行首行尾的灰格数不限。令
 * 块长 L_i = m + a_i（a_i ≥ 0）、内部间隔 gap_i = 1 + b_i（b_i ≥ 0）、首尾灰格 g_0,g_k ≥ 0，
 * 则 Σ L + Σ gap + g_0 + g_k = n 化为 2k+1 个非负变量之和等于 n − (m+1)k + 1，
 * 隔板法给出 C(n − (m+1)k + 1 + 2k, 2k) = C(n − (m−1)k + 1, 2k)；k = 0 即全灰的一种。
 *
 * 组合数用 BigInteger 逐步乘除（第 i 步的部分积恰为 C(·, i)，整除恒精确），与 solution.kt
 * 的 64 位线性递推是两条互不相干的数值路径。另外还带一个真正的穷举枚举器 `enumerateFillings`
 * ——不记忆化、逐个布局数——用来在小规模上给闭式当锚点（规模 50 时它有 1.6×10¹⁰ 个叶节点，
 * 跑不完，只能当小规模交叉检查）。
 *
 * 复杂度：闭式 O(N²) 次多精度乘除（N = 50 时约 12 项 × ≤ 25 步）；穷举枚举 O(f(n)) 个布局。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 按红块个数分类的闭式计数：f(n) = Σ_k C(n − (m−1)k + 1, 2k)。 */
fun countByBlockClass(length: Int, minLength: Int): Long {
    var total = BigInteger.ONE                      // k = 0：全灰
    for (blocks in 1..(length + 1) / (minLength + 1)) {
        val top = length - (minLength - 1) * blocks + 1
        val choose = minOf(2L * blocks, (top - 2L * blocks).toLong()).toInt()
        var term = BigInteger.ONE
        for (i in 1..choose) {                      // C(top, choose)，每步都是精确整除
            term = term.multiply(BigInteger.valueOf((top - choose + i).toLong()))
                .divide(BigInteger.valueOf(i.toLong()))
        }
        total = total.add(term)
    }
    return total.longValueExact()
}

/**
 * 真·穷举：不记忆化，从行首开始逐个布局数出来（只用于小规模交叉检查）。
 * 每次决策是「本格放灰」或「本格起一块长度 block ≥ m 的红」；后者若没顶到行尾，
 * 紧随其后的那一格必须是灰格，一并消费掉再递归。
 */
fun enumerateFillings(remaining: Int, minLength: Int): Long {
    if (remaining == 0) return 1L
    var total = enumerateFillings(remaining - 1, minLength)          // 本格放灰
    for (block in minLength..remaining) {
        if (block == remaining) total++                              // 红块正好填满行尾
        else total += enumerateFillings(remaining - block - 1, minLength)
    }
    return total
}

fun solveBruteForce(): Long = countByBlockClass(50, 3)

fun main() {
    // 锚点一：题面样例，长度 7 恰好 17 种
    val statement = enumerateFillings(7, 3)
    check(statement == 17L && countByBlockClass(7, 3) == 17L) { "题面样例不符：穷举 $statement" }
    // 锚点二：小规模上穷举 = 闭式，逐长度比对 n = 0..25
    for (n in 0..25) {
        val byList = enumerateFillings(n, 3)
        val byFormula = countByBlockClass(n, 3)
        check(byList == byFormula) { "n = $n 两条路径不一致：穷举 $byList vs 闭式 $byFormula" }
    }
    println("锚点：f(7) = 17（题面），穷举与闭式在 n = 0..25 全部一致")
    println("f(25) = ${countByBlockClass(25, 3)}，f(29) = ${countByBlockClass(29, 3)}，f(30) = ${countByBlockClass(30, 3)}")
    println(solveBruteForce())
}
