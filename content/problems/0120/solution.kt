/**
 * Project Euler 120 — Square Remainders（平方剩余）
 *
 * 思路：把 (a−1)^n 与 (a+1)^n 二项式展开后逐项配对，凡含 a² 及更高次幂的项在
 * mod a² 下全部归零，只剩常数项与一次项，于是
 *     n 为偶数：(a−1)^n + (a+1)^n ≡ 2   (mod a²)
 *     n 为奇数：(a−1)^n + (a+1)^n ≡ 2na (mod a²)
 * 偶数 n 只能给出常数余数 2，所以 r_max 等于 a·max(2n mod a)（n 取遍正奇数）。
 * a 为奇数时，上界为 a−1；取 n=(a−1)/2，若 n 偶则加 a，即取得上界且 n 为奇。
 * a 为偶数时，剩余必为偶数，故上界为 a−2；取奇数 n=a−1，即取得上界。
 * 两种情形都显式构造出了奇数指数，故
 *     r_max(a) = a(a−1)（a 奇）或 a(a−2)（a 偶）。
 * 对 a = 3..1000 求和即可。main 里另用「逐项累乘取模」直接枚举 n 验证 a = 3..60 的
 * 全部 r_max，并把题面样例 a = 7 → 42 写成断言。
 *
 * 复杂度：O(A) 时间、O(1) 空间（A = 998 个 a，每个 3 次 64 位运算）。
 * 数值范围：a ≤ 1000，a² ≤ 10⁶，总和 ≈ 3.3×10⁸，远小于 Long 上限，无 BigInteger 需求。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 闭式：a 为奇数时 r_max = a(a−1)，a 为偶数时 r_max = a(a−2)。 */
fun maxRemainder(a: Long): Long = if (a % 2L != 0L) a * (a - 1L) else a * (a - 2L)

/**
 * 直接枚举：对每个 n = 1..nBound 累乘出 (a−1)^n、(a+1)^n 模 a²，取余数最大值。
 * 只用于小范围的独立复核，不作为主算法。
 */
fun directMaxRemainder(a: Long, nBound: Long): Long {
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

fun solve(): Long = (3L..1000L).sumOf { maxRemainder(it) }

fun main() {
    check(maxRemainder(7L) == 42L) { "题面给出 a = 7 时 r_max = 42，实际 ${maxRemainder(7L)}" }
    for (a in 3L..60L) {
        val direct = directMaxRemainder(a, 4L * a)
        check(direct == maxRemainder(a)) { "a = $a：直接枚举 $direct ≠ 闭式 ${maxRemainder(a)}" }
    }
    println(solve())
}
