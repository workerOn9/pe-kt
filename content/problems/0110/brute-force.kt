/**
 * Project Euler 110 — 全指数枚举对照（非逐整数暴力）
 *
 * 思路：仅使用「最小解的素因子可压缩为连续小素数」引理，不假设指数单调。
 * 前十五个素数各一次给出固定可行上界 B，枚举这些素数上所有乘积≤B 的指数向量，
 * 包括零指数、空洞和逆序指数。只在完整向量处检查解数，找到的答案不参与剪枝。
 * 因而可独立检验 solution.kt 的指数排序、提前达标返回与动态最优值剪枝。
 * 复杂度：O(W) 时间、O(15) 栈空间，W 是固定上界下的全指数前缀数。
 * 不计算 n²；在 1260 的小样例上直接枚举 x，验证因子计数及最小性。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val primes = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)
    val bound = primes.fold(1L) { product, prime -> product * prime }
    var best = bound

    fun enumerate(index: Int, n: Long, divisors: Long) {
        if (index == primes.size) {
            if ((divisors + 1) / 2 > 4_000_000L && n < best) best = n
            return
        }
        val prime = primes[index]
        var powerProduct = n
        var exponent = 0L
        while (true) {
            enumerate(index + 1, powerProduct, divisors * (2 * exponent + 1))
            if (powerProduct > bound / prime) return
            powerProduct *= prime
            exponent++
        }
    }

    enumerate(0, 1L, 1L)
    return best
}

private fun directCount(n: Long): Int {
    var count = 0
    for (x in n + 1..2 * n) {
        if (n * x % (x - n) == 0L) count++
    }
    return count
}

fun main() {
    check(directCount(4) == 3)
    check(directCount(1260) == 113)
    check((1L until 1260).all { directCount(it) <= 100 })
    val answer = solveBruteForce()
    check(answer == 9_350_130_049_860_600L)
    println(answer)
}
