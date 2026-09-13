/**
 * Project Euler 099 — Largest Exponential
 *
 * 思路：比较 $b^e$ 等价于比较 $e\ln b$（$\ln$ 单调递增）。double 对数的相对误差约
 * 1e-16，但值本身可达 6.9×10^6，误差被放大到 1e-9 量级，不能想当然地直接比大小。
 * 这里给每个值配一个保守的相对误差界，只有当「最大值 − 次大值」超过两者误差界之和时
 * 才接受浮点结论；否则对落在该区间内的候选改用 BigInteger 精确幂比较兜底（本题不触发）。
 * 需从题目目录运行（读取同目录 base_exp.txt）。
 * 复杂度：O(N) 次浮点对数；只有浮点无法分辨时才付出大数幂的代价。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import java.math.BigInteger

private const val P099_REL = 1e-12

private fun p099ExactGreater(b1: Long, e1: Long, b2: Long, e2: Long): Boolean {
    if (b1 == b2) return e1 > e2
    val x = BigInteger.valueOf(b1).pow(e1.toInt())
    val y = BigInteger.valueOf(b2).pow(e2.toInt())
    return x > y
}

fun solve(path: String = "base_exp.txt"): Long {
    val lines = java.io.File(path).readLines().map { it.trim() }.filter { it.isNotEmpty() }
    val n = lines.size
    val bases = LongArray(n)
    val exps = LongArray(n)
    val values = DoubleArray(n)
    val errors = DoubleArray(n)
    var best = -1
    for ((i, line) in lines.withIndex()) {
        val parts = line.split(',')
        val b = parts[0].trim().toLong()
        val e = parts[1].trim().toLong()
        bases[i] = b
        exps[i] = e
        val v = e.toDouble() * Math.log(b.toDouble())
        values[i] = v
        errors[i] = Math.abs(v) * P099_REL + P099_REL
        if (best < 0 || v > values[best]) best = i
    }

    val floorValue = values[best] - errors[best]
    var winner = best
    for (i in 0 until n) {
        if (i == best) continue
        if (values[i] + errors[i] < floorValue) continue
        if (p099ExactGreater(bases[i], exps[i], bases[winner], exps[winner])) winner = i
    }
    return (winner + 1).toLong()
}

fun main() {
    println(solve())
}
