/**
 * Project Euler 129 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的思路完全不同：优化解先把「n | R(k)」翻译成 9n | 10^k − 1，
 * 再用质因子分解 + Carmichael 函数直接算出 10 模 9n 的乘法阶；
 * 本解不翻译、不分解、不取阶，直接按定义来 —— 令 r_k = R(k) mod n，
 * 用递推 r_k = (10·r_{k−1} + 1) mod n 从 k = 1 一路乘上去，
 * 第一次出现 r_k = 0 的 k 就是 A(n)。两者的差距来源就在这里：
 * 优化解每个候选只做几百次试除 + 几次模幂，暴力解每个候选要做 A(n) 次模乘
 * （最小的那个候选也要 12 次，最大 1000020 次）。
 *
 * 为什么可以从 n = 10^6 + 1 开始找：模 n 的映射 x ↦ (10x + 1) mod n 是双射
 * （gcd(10, n) = 1），余数序列纯周期，轨道只占 n 个剩余类，故 A(n) ≤ n；
 * 要 A(n) > 10^6 就必须 n > 10^6。这是纯计数论证，两条解法共用同一起点，
 * 但 A(n) 的计算方式彼此独立。也正因 A(n) ≤ n，下面的定长循环必然终止。
 *
 * 复杂度：O(候选数 × max A(n)) 次模乘，空间 O(1)；实测见 analysis.md。
 * 与 10 互素的候选只有 10 个（1000001 … 1000023）。
 *
 * 题面样例（A(7) = 6、A(41) = 5、A(n) 首次超过 10 的最小 n 是 17）写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** A(n)：最小的 k 使 n | R(k)，按定义递推 R(k) mod n。gcd(n, 10) = 1 时由 A(n) ≤ n 保证终止。 */
fun repunitLengthBrute(n: Long): Long {
    var r = 0L
    var k = 0L
    while (true) {
        r = (r * 10L + 1L) % n
        k++
        if (r == 0L) return k
    }
}

/** 最小的 n 使 A(n) > threshold；起点 threshold + 1 来自 A(n) ≤ n。 */
fun leastNBrute(threshold: Long): Long {
    var n = threshold + 1L
    while (true) {
        if (n % 2L != 0L && n % 5L != 0L && repunitLengthBrute(n) > threshold) return n
        n++
    }
}

fun solveBruteForce(): Long = leastNBrute(1_000_000L)

fun main() {
    check(repunitLengthBrute(7L) == 6L) { "A(7) 应为 6" }
    check(repunitLengthBrute(41L) == 5L) { "A(41) 应为 5" }
    check(repunitLengthBrute(3L) == 3L) { "A(3) 应为 3" }
    check(repunitLengthBrute(9L) == 9L) { "A(9) 应为 9" }
    check(leastNBrute(10L) == 17L) { "A(n) > 10 的最小 n 应为 17" }
    // 逐位造出 R(6) = 111111，直接验证 7 | R(6) 且 6 是最小：k < 6 时余数不为 0
    var r = 0L
    for (k in 1..6) {
        r = r * 10L + 1L
        check((r % 7L == 0L) == (k == 6)) { "7 | R(k) 应恰在 k = 6 时成立" }
    }

    repeat(5) { solveBruteForce() }              // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
