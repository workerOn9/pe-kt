/**
 * Project Euler 034 — 暴力解（教学对比用）
 *
 * 每个候选数先把数字转成字符串，再逐位递归重算阶乘，字符串转换与重复计算都计入开销。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    fun fact(i: Int): Int = if (i <= 1) 1 else i * fact(i - 1)
    var sum = 0L
    for (n in 3..2540160) {
        val s = n.toString().map { fact(it - '0') }.sum()
        if (s == n) sum += n
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
