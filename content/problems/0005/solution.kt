/**
 * Project Euler 005 — Smallest Multiple
 *
 * 优化解：答案即 lcm(1..20)，用 gcd（辗转相除）逐步累乘。
 * lcm(a,b) = a / gcd(a,b) * b（先除后乘防溢出）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun gcd(a0: Long, b0: Long): Long {
    var a = a0
    var b = b0
    while (b != 0L) {
        val t = a % b
        a = b
        b = t
    }
    return a
}

fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b

fun solve(n: Long = 20): Long = (1L..n).reduce(::lcm)

fun main() {
    println(solve())
}
