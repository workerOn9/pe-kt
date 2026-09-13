/**
 * Project Euler 097 — Large Non-Mersenne Prime（暴力解）
 *
 * 思路：完全按字面意思做——把 2 自乘 7830457 次，每一步只保留末十位。
 * 因为两个小于 10^10 的数直接相乘会溢出 Long（上限约 9.2×10^18），
 * 这里把乘数拆成高低各 5 位分别做模乘再拼接。
 * 复杂度：O(e) 次模乘，e = 7830457。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private const val P097_MOD = 10_000_000_000L

private fun p097MulMod(a: Long, b: Long): Long {
    val hi = a / 100_000L
    val lo = a % 100_000L
    return ((hi * b % P097_MOD) * 100_000L % P097_MOD + lo * b % P097_MOD) % P097_MOD
}

fun solveBruteForce(): Long {
    var r = 1L
    repeat(7830457) { r = p097MulMod(r, 2L) }
    r = p097MulMod(r, 28433L)
    return (r + 1) % P097_MOD
}

fun main() {
    println(solveBruteForce())
}
