/**
 * Project Euler 003 — Largest Prime Factor
 *
 * 试除法：从 2 开始整除剥离因子，因子只需枚举到 sqrt(剩余值)。
 * 剥离完后若剩余值 > 1，它本身就是最大质因数。
 * 复杂度 O(sqrt(n))，n = 600851475143 时约 7.7 万次迭代。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun largestPrimeFactor(n0: Long): Long {
    var n = n0
    var candidate = 2L
    var largest = 1L
    while (candidate * candidate <= n) {
        while (n % candidate == 0L) {
            largest = candidate
            n /= candidate
        }
        candidate += if (candidate == 2L) 1L else 2L   // 2 之后只试奇数
    }
    return if (n > 1) n else largest
}

fun main() {
    println(largestPrimeFactor(600_851_475_143L))
}
