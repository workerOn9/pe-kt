/**
 * Project Euler 108 — 暴力解（第二条独立验证路径）
 *
 * 与 solution.kt 共享同一条数学结论（解个数 = (d(n²)+1)/2，由 (x−n)(y−n) = n² 推出），
 * 但**验证策略完全相反**：solution.kt 依赖「指数非递增 + 只取最小素数」这条结构引理做
 * 剪枝搜索，本文件对这条引理一无所知——它从 n = 1 起逐个升序枚举，对每个 n 试除分解出
 * d(n²)，返回第一个解个数超过 1000 的 n。由于枚举是升序且不留空隙，它同时构成最小性的
 * 穷尽证明：在答案之前的每个 n 都被实际算过且不合格。
 *
 * 另加两处独立于质因数分解的核对（solutionCountByEnumeration：直接枚举 n² 的因子 a ≤ n
 * 数解个数，纯取模，不经过任何分解公式）：
 *   1. 题面样例 n = 4 → 公式给 3 个解，直接数 16 的因子也得 3；
 *   2. 答案 180180 → 两条路径都得 1013；顺带报出 n < 180180 时解个数的最大值。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** n² 的正因子个数 d(n²) = ∏(2eᵢ+1)，试除法分解 n。 */
fun squareDivisorCount(n: Long): Long {
    var rest = n
    var total = 1L
    var p = 2L
    while (p * p <= rest) {
        if (rest % p == 0L) {
            var exponent = 0
            do {
                rest /= p
                exponent++
            } while (rest % p == 0L)
            total *= 2L * exponent + 1
        }
        p += if (p == 2L) 1L else 2L
    }
    if (rest > 1L) total *= 3L
    return total
}

/** 解个数 = (d(n²)+1)/2。 */
fun solutionCount(n: Long): Long = (squareDivisorCount(n) + 1L) / 2L

/** 不借助任何公式，直接枚举 n² 的因子 a ≤ n 数解个数（x = n+a, y = n+n²/a）。 */
fun solutionCountByEnumeration(n: Long): Long {
    val square = n * n
    var count = 0L
    var a = 1L
    while (a <= n) {
        if (square % a == 0L) count++
        a++
    }
    return count
}

/** 升序穷举：第一个解个数 > 1000 的 n。 */
fun solveBruteForce(): Long {
    var n = 1L
    while (solutionCount(n) <= 1_000L) n++
    return n
}

fun main() {
    val four = solutionCount(4L)
    println("n = 4 → $four 个解（题面：3）")
    check(four == 3L && solutionCountByEnumeration(4L) == 3L) { "样例自检失败" }

    val answer = solveBruteForce()
    println("升序枚举得到 $answer，解个数 ${solutionCount(answer)}，d(n²) = ${squareDivisorCount(answer)}")
    println("直接枚举 n² 因子复核：${solutionCountByEnumeration(answer)} 个解")

    // 最小性复核：答案之前的所有 n 都算过，最大的解个数不超过 1000
    var maxBelow = 0L
    for (n in 1L until answer) {
        val count = solutionCount(n)
        if (count > maxBelow) maxBelow = count
    }
    println("n < $answer 时最多 $maxBelow 个解（阈值 1000）")
    check(maxBelow < 1_000L && solutionCount(answer) == 1_013L) { "阈值行为与预期不符" }
    println(answer)
}
