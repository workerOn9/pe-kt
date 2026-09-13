/**
 * Project Euler 002 — Even Fibonacci Numbers
 *
 * 优化解：只枚举偶数项。斐波那契数列奇偶性以 3 为周期（奇、奇、偶），
 * 偶数项满足递推 E(n) = 4·E(n-1) + E(n-2)：2, 8, 34, 144, ...
 * 项数为 O(log limit)，本题范围内仅 11 个偶数项。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(limit: Long = 4_000_000): Long {
    var sum = 0L
    var prev = 2L   // E(1) = 2
    var curr = 8L   // E(2) = 8
    while (prev <= limit) {
        sum += prev
        val next = 4 * curr + prev
        prev = curr
        curr = next
    }
    return sum
}

fun main() {
    println(solve())
}
