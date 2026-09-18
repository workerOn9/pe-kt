/**
 * Project Euler 120 — 暴力解（教学对比用）
 *
 * 完全不使用二项式展开的结论：对每个 a，把 n 从 1 开始逐个推进，用「乘一步取一次模」
 * 的方式直接算出 (a−1)^n 与 (a+1)^n 模 a² 的值，取余数最大值。这是题面定义的字面翻译，
 * 除了模运算本身没有任何化简，与 solution.kt 的闭式 r_max = a(a−1)/a(a−2) 是两条
 * 完全不同的推理路径。
 *
 * n 的上界取 4a：余数随 n 的周期整除 2a（模 2a 下 2n 的周期），4a 覆盖两个完整周期，
 * main 里另用 10a 复算一遍并断言结果不变，避免「上界取小了、漏掉真正的最大值」。
 *
 * 题面样例 a = 7 → r_max = 42 作为运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 直接枚举 n = 1..nBound，返回 max r，其中 r = ((a−1)^n + (a+1)^n) mod a²。 */
fun bruteMaxRemainder(a: Long, nBound: Long): Long {
    val modulus = a * a
    var powerMinus = 1L
    var powerPlus = 1L
    var best = 0L
    for (n in 1..nBound) {
        powerMinus = powerMinus * ((a - 1L) % modulus) % modulus
        powerPlus = powerPlus * ((a + 1L) % modulus) % modulus
        val remainder = (powerMinus + powerPlus) % modulus
        if (remainder > best) best = remainder
    }
    return best
}

fun solveBruteForce(): Long = (3L..1000L).sumOf { bruteMaxRemainder(it, 4L * it) }

fun main() {
    check(bruteMaxRemainder(7L, 28L) == 42L) { "题面样例 a = 7 应得 42" }
    for (a in 3L..1000L) {
        val wide = bruteMaxRemainder(a, 10L * a)
        check(wide == bruteMaxRemainder(a, 4L * a)) { "a = $a：n 上界取 4a 与 10a 结果不同" }
    }
    println("n 上界 4a 与 10a 在 a = 3..1000 上结果一致")
    println("暴力解 r_max 之和 = ${solveBruteForce()}")
}
